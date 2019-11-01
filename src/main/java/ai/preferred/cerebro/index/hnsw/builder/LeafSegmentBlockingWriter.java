package ai.preferred.cerebro.index.hnsw.builder;


import ai.preferred.cerebro.index.hnsw.*;
import ai.preferred.cerebro.index.hnsw.BitSet;
import ai.preferred.cerebro.index.hnsw.Stack;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import java.util.*;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.StampedLock;

//A leaf hnsw index writer can insert around 9M ~ 10M nodes before insert time start to increase tremendously
//Never insert more than 5M nodes into a leaf in one go, it will cost you a lot of time as insert time increase
// exponentially with current number of nodes in the graph.

//Instead use HnswIndexWriter to spread your vectors out in many graphs, the number of graphs to spread out your nodes
//should be the number of cores your CPU has. This will give you both good index building time, good search time and
// nearly double the accuracy (tested with a 6-core CPU).
public class LeafSegmentBlockingWriter<TVector> extends LeafSegmentWriter<TVector> {

    private ReentrantLock globalLock;
    private StampedLock stampedLock;
    private BitSet activeConstruction;
    private AtomicReferenceArray<Node<TVector>> nodes;

    //Create constructor
    public LeafSegmentBlockingWriter(HnswIndexWriter parent, int numName, int base) {
        super(parent, numName, base);
        this.globalLock = new ReentrantLock();
        this.stampedLock = new StampedLock();
        this.activeConstruction = new BitSet(this.maxNodeCount);
        nodes = new AtomicReferenceArray<>(super.nodes);
        super.nodes = null;
    }

    //Load constructor
    public LeafSegmentBlockingWriter(HnswIndexWriter parent, int numName , String idxDir) {
        super(parent, numName, idxDir);

        this.globalLock = new ReentrantLock();
        this.stampedLock = new StampedLock();
        this.activeConstruction = new BitSet(this.maxNodeCount);
        nodes = new AtomicReferenceArray<>(super.nodes);
        super.nodes = null;
    }
    @Override
    public Optional<TVector> getVec(int internalID) {
        return Optional.ofNullable(nodes.get(internalID)).map(Node::vector);
    }
    @Override
    public Optional<Node<TVector>> getNode(int internalID) {
        return Optional.ofNullable(nodes.get(internalID));
    }

    @Override
    public boolean removeOnInternalID(int internalID) {
        if (!removeEnabled) {
            return false;
        }
        globalLock.lock();
        try {

            Node node = nodes.get(internalID);

            for (int level = node.maxLevel(); level >= 0; level--) {
                final int thisLevel = level;
                node.inConns[level].forEach(neighbourId ->
                        nodes.get(neighbourId).outConns[thisLevel].remove(internalID));

                node.outConns[level].forEach(neighbourId ->
                        nodes.get(neighbourId).inConns[thisLevel].remove(internalID));
            }

            // change the entry point to the first outgoing connection at the highest level
            if (entryPoint == node) {
                for (int level = node.maxLevel(); level >= 0; level--) {
                    IntArrayList outgoingConnections = node.outConns[level];
                    if (!outgoingConnections.isEmpty()) {
                        entryPoint = nodes.get(outgoingConnections.getFirst());
                        break;
                    }
                }

            }

            // if we could not change the outgoing connection it means we are the last node
            if (entryPoint == node) {
                entryPoint = null;
            }
            if(lookup.contains(node.item.externalId))
                lookup.remove(node.item.externalId);
            nodes.set(internalID, null);

            //no need to put freedIds inside a synchronized block because
            //other other code sections that write to freedIds are also inside
            //global lock
            freedIds.push(internalID);
        }
        finally { globalLock.unlock(); }

        return true;
    }

    //to be handled by parent


    /**
     * @param item the item to add to the index
     * @return true means item added successfully,
     */
    @Override
    public boolean add(Item<TVector> item) {
        globalLock.lock();
        try {
            Integer globalId = lookup.get(item.externalId);


            //check if there is nodes with similar id in the graph
            if(globalId != null){
                //if there is similar id but index does not support removal then abort operation
                if (!removeEnabled) {
                    return false;
                }
                //if there is already this id in the index, it means this is an update
                //so only handle if this is the leaf that the id was already residing
                if(globalId >= baseID && globalId < baseID + maxNodeCount){
                    Node<TVector> node = nodes.get(globalId - baseID);
                    if (Objects.deepEquals(node.vector(), item.vector)) {
                        //object already added
                        return true;
                    } else {
                        //similar id but different vector means different object
                        //so remove the object to insert the current new one
                        removeOnInternalID(item.externalId);
                    }
                }
                else
                    return false;

            }
            int internalId;
            //try to use used id of deleted node to assign to this new node
            //if not available use a new id (unconsumed) for this node
            if (freedIds.isEmpty()) {
                if (nodeCount >= this.maxNodeCount) {
                    return false;
                }
                internalId = nodeCount++;
            } else {
                internalId = freedIds.pop();
            }

            //randomize level
            int randomLevel = assignLevel(item.externalId, this.levelLambda);

            IntArrayList[] outConns = new IntArrayList[randomLevel + 1];

            for (int level = 0; level <= randomLevel; level++) {
                int levelM = randomLevel == 0 ? maxM0 : maxM;
                outConns[level] = new IntArrayList(levelM);
            }

            IntArrayList[] inConns = removeEnabled ? new IntArrayList[randomLevel + 1] : null;
            if (removeEnabled) {
                for (int level = 0; level <= randomLevel; level++) {
                    int levelM = randomLevel == 0 ? maxM0 : maxM;
                    inConns[level] = new IntArrayList(levelM);
                }
            }

            Node<TVector> entryPointCopy = entryPoint;

            if (entryPoint != null && randomLevel <= entryPoint.maxLevel()) {
                globalLock.unlock();
            }

            //if the global lock is released above,
            //then basically at this point forward
            //there is no lock at all, since this is a read lock.
            //Unless a write lock has been called somewhere.
            long stamp = stampedLock.readLock();

            try {

                //the bitset is shared across all threads to signal
                //setting aside nodes that are being inserted

                synchronized (activeConstruction) {

                    activeConstruction.flipTrue(internalId);
                }

                Node<TVector> newNode = new Node<>(internalId, outConns, inConns, item);
                nodes.set(internalId, newNode);
                lookup.put(item.externalId, internalId + baseID);

                Node<TVector> curNode = entryPointCopy;

                //entry point is null if this is the first node inserted into the graph
                if (curNode != null) {

                    //if no layer added
                    if (newNode.maxLevel() < entryPointCopy.maxLevel()) {

                        double curDist = handler.distance(newNode.vector(), curNode.vector());
                        //sequentially zoom in until reach the layer next to
                        // the highest layer that the new node has to be inserted
                        for (int curLevel = entryPointCopy.maxLevel(); curLevel > newNode.maxLevel(); curLevel--) {

                            boolean changed = true;
                            while (changed){
                                changed = false;
                                IntArrayList candidateConns = curNode.outConns[curLevel];
                                synchronized (candidateConns) {


                                    for (int i = 0; i < candidateConns.size(); i++) {

                                        int candidateId = candidateConns.get(i);

                                        Node<TVector> candidateNode = nodes.get(candidateId);

                                        double candidateDistance = handler.distance(newNode.vector(), candidateNode.vector());

                                        //updating the starting node to be used at lower level
                                        if (candidateDistance < curDist) {
                                            curDist = candidateDistance;
                                            curNode = candidateNode;
                                            changed = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    //insert the new node starting from its highest layer by setting up connections
                    for (int level = Math.min(randomLevel, entryPointCopy.maxLevel()); level >= 0; level--) {
                        BoundedMaxHeap topCandidates = searchLayer(curNode, newNode.vector(), efConstruction, level);
                        synchronized (newNode) {
                            mutuallyConnectNewElement(newNode, topCandidates, level);
                        }

                    }
                }

                // if this is the first node inserted or its highest layer is higher than that
                // of the current entry node, then we have to update the entry node
                if (entryPoint == null || newNode.maxLevel() > entryPointCopy.maxLevel()) {
                    // this is thread safe because we get the global lock when we add a level
                    this.entryPoint = newNode;
                }
            } finally {
                //upon insert completion signal that the node is ready
                //to take part in the insertion of other nodes

                synchronized (activeConstruction) {
                    activeConstruction.flipFalse(internalId);
                }

                stampedLock.unlockRead(stamp);
                return true;
            }
        } finally {
            //this code section is called when this node insertion
            //has updated the entry node of the graph.
            if (globalLock.isHeldByCurrentThread()) {
                globalLock.unlock();
            }
        }
    }

    @Override
    protected void mutuallyConnectNewElement(Node<TVector> newNode,
                                             BoundedMaxHeap topCandidates,
                                             int level) {

        int bestN = level == 0 ? this.maxM0 : this.maxM;

        int newNodeId = newNode.internalId;
        TVector newNodeVector = newNode.vector();
        IntArrayList outNewNodeConns = newNode.outConns[level];

        Iterator<Candidate> iteratorSelected = getNeighborsByHeuristic2(topCandidates, null, bestN);

        while (iteratorSelected.hasNext()) {
            int selectedNeighbourId = iteratorSelected.next().nodeId;

            synchronized (activeConstruction) {
                if (activeConstruction.isTrue(selectedNeighbourId)) {
                    continue;
                }
            }

            outNewNodeConns.add(selectedNeighbourId);

            Node<TVector> neighbourNode = nodes.get(selectedNeighbourId);

            synchronized (neighbourNode) {

                if (removeEnabled) {
                    neighbourNode.inConns[level].add(newNodeId);
                }

                TVector neighbourVector = neighbourNode.vector();

                IntArrayList outNeighbourConnsAtLevel = neighbourNode.outConns[level];

                if (outNeighbourConnsAtLevel.size() < bestN) {

                    if (removeEnabled) {
                        newNode.inConns[level].add(selectedNeighbourId);
                    }

                    outNeighbourConnsAtLevel.add(newNodeId);
                } else {
                    // finding the "weakest" element to replace it with the new one

                    double dMax = handler.distance(newNodeVector, neighbourNode.vector());

                    BoundedMaxHeap candidates = new BoundedMaxHeap(bestN + 1, ()-> null);
                    candidates.add(new Candidate(newNodeId, dMax, distanceComparator));

                    outNeighbourConnsAtLevel.forEach(id -> {
                        double dist = handler.distance(neighbourVector, nodes.get(id).vector());
                        candidates.add(new Candidate(id, dist, distanceComparator));
                    });

                    MutableIntList prunedConnections = removeEnabled ? new IntArrayList() : null;

                    Iterator<Candidate> selecteds = getNeighborsByHeuristic2(candidates, prunedConnections, bestN);

                    if (removeEnabled) {
                        newNode.inConns[level].add(selectedNeighbourId);
                    }

                    outNeighbourConnsAtLevel.clear();
                    while (selecteds.hasNext()) {
                        outNeighbourConnsAtLevel.add(selecteds.next().nodeId);
                    }

                    if (removeEnabled) {
                        prunedConnections.forEach(id -> {
                            Node node = nodes.get(id);
                            synchronized (node.inConns) {
                                node.inConns[level].remove(selectedNeighbourId);
                            }
                        });
                    }
                }
            }
        }
    }

    @Override
    protected Iterator<Candidate> getNeighborsByHeuristic2(BoundedMaxHeap topCandidates,
                                                           MutableIntList prunedConnections,
                                                           int m) {

        if (topCandidates.size() < m) {
            topCandidates.iterator();
        }

        Stack<Candidate> queueClosest = new Stack<Candidate>(topCandidates.size());
        List<Candidate> returnList = new ArrayList<>();

        while (topCandidates.size() != 0) {
            queueClosest.push(topCandidates.pop());
        }

        while (!queueClosest.isEmpty()) {
            Candidate currentPair = queueClosest.pop();

            boolean good;
            if (returnList.size() >= m) {
                good = false;
            } else {
                double distToQuery = currentPair.distance;

                good = true;
                for (Candidate secondPair : returnList) {

                    double curdist = handler.distance(
                            nodes.get(secondPair.nodeId).vector(),
                            nodes.get(currentPair.nodeId).vector()
                    );

                    if (curdist < distToQuery) {
                        good = false;
                        break;
                    }

                }
            }
            if (good) {
                returnList.add(currentPair);
            } else {
                if (prunedConnections != null) {
                    prunedConnections.add(currentPair.nodeId);
                }
            }
        }

        return returnList.iterator();
    }

    @Override
    protected BoundedMaxHeap searchLayer(
            Node<TVector> entryPointNode, TVector destination, int k, int layer) {

        BitSet visitedBitSet = parent.getBitsetFromPool();

        try {
            BoundedMaxHeap topCandidates = new BoundedMaxHeap(k, ()-> null);
            PriorityQueue<Candidate> checkNeighborSet = new PriorityQueue<>();

            double distance = handler.distance(destination, entryPointNode.vector());

            Candidate firstCandidade = new Candidate(entryPointNode.internalId, distance, distanceComparator);

            topCandidates.add(firstCandidade);
            checkNeighborSet.add(firstCandidade);
            visitedBitSet.flipTrue(entryPointNode.internalId);

            double lowerBound = distance;

            while (!checkNeighborSet.isEmpty()) {

                Candidate nodeWithNeighbors = checkNeighborSet.poll();

                if (nodeWithNeighbors.distance > lowerBound) {
                    break;
                }

                Node node = nodes.get(nodeWithNeighbors.nodeId);

                synchronized (node) {

                    MutableIntList candidates = node.outConns[layer];

                    for (int i = 0; i < candidates.size(); i++) {

                        int candidateId = candidates.get(i);

                        if (!visitedBitSet.isTrue(candidateId)) {

                            visitedBitSet.flipTrue(candidateId);

                            double candidateDistance = handler.distance(destination,
                                    nodes.get(candidateId).vector());

                            if (topCandidates.top().distance > candidateDistance || topCandidates.size() < k) {

                                Candidate newCandidate = new Candidate(candidateId, candidateDistance, distanceComparator);
                                checkNeighborSet.add(newCandidate);
                                if (topCandidates.size() == k)
                                    topCandidates.updateTop(newCandidate);
                                else
                                    topCandidates.add(newCandidate);

                                lowerBound = topCandidates.top().distance;
                            }
                        }
                    }

                }
            }

            return topCandidates;
        } finally {
            visitedBitSet.clear();
            parent.returnBitsetToPool(visitedBitSet);
        }
    }



    @Override
    protected void saveVecs(String dirPath)  {
        synchronized(nodes){
            handler.saveNodesBlocking(dirPath + LOCAL_VECS, this.nodes, nodeCount);
        }
    }
    @Override
    protected void saveOutConns(String dirPath) {
        synchronized(nodes){
            int[][][] outConns = new int[nodeCount][][];
            Node t;
            for (int i = 0; i < nodeCount; i++) {
                t = this.nodes.get(i);
                if(t != null){
                    outConns[i] = new int[t.outConns.length][];
                    for (int j = 0; j < t.outConns.length; j++) {
                        outConns[i][j] = t.outConns[j].toArray();
                    }
                }
                else outConns[i] = null;
            }
            Kryo kryo = new Kryo();
            kryo.register(int[].class);
            kryo.register(int[][].class);
            kryo.register(int[][][].class);
            try {
                Output outputOutconns = new Output(new FileOutputStream(dirPath + LOCAL_OUTCONN));
                kryo.writeObject(outputOutconns, outConns);
                outputOutconns.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    protected void saveInConns(String dirPath) {
        synchronized(nodes){
            int[][][] inConns = new int[nodeCount][][];
            Node t;
            for (int i = 0; i < nodeCount; i++) {
                t = this.nodes.get(i);
                if(t != null){
                    inConns[i] = new int[t.inConns.length][];
                    for (int j = 0; j < t.inConns.length; j++) {
                        inConns[i][j] = t.inConns[j].toArray();
                    }
                }
                else inConns[i] = null;
            }
            Kryo kryo = new Kryo();
            kryo.register(int[].class);
            kryo.register(int[][].class);
            kryo.register(int[][][].class);
            try {
                Output outputInconns = new Output(new FileOutputStream(dirPath + LOCAL_INCONN));
                kryo.writeObject(outputInconns, inConns);
                outputInconns.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    protected void saveInvertLookUp(String dirPath){
        synchronized (nodes){
            int[] invertLookUp = new int[nodeCount];
            for (int i = 0; i < nodeCount; i++) {
                invertLookUp[i] = nodes.get(i).item.externalId;
            }
            Kryo kryo = new Kryo();
            kryo.register(int[].class);
            try {
                Output outputInvert = new Output(new FileOutputStream(dirPath + LOCAL_INVERT));
                kryo.writeObject(outputInvert, invertLookUp);
                outputInvert.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
