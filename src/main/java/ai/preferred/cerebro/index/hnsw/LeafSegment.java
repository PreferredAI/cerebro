package ai.preferred.cerebro.index.hnsw;


import ai.preferred.cerebro.index.handler.VecHandler;
import ai.preferred.cerebro.index.hnsw.builder.ConcurrentWriter;
import ai.preferred.cerebro.index.hnsw.structure.*;
import ai.preferred.cerebro.index.hnsw.structure.BitSet;
import ai.preferred.cerebro.index.ids.ExternalID;
import ai.preferred.cerebro.index.utils.IndexUtils;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.stack.mutable.primitive.IntArrayStack;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static ai.preferred.cerebro.index.utils.IndexConst.Sp;

/**
 * Implementation of {@link ConcurrentWriter} that implements the hnsw algorithm.
 *
 //* @param <T> Type of distance between items (expect any numeric type: float, double, int, ..)
 * @see <a href="https://arxiv.org/abs/1603.09320">
 * Efficient and robust approximate nearest neighbor search using Hierarchical Navigable Small World graphs</a>
 */
abstract public class LeafSegment<TVector> {
    //constants
    protected final String LOCAL_CONFIG;
    protected final String LOCAL_DELETED;
    protected final String LOCAL_INCONN;
    protected final String LOCAL_OUTCONN;
    protected final String LOCAL_INVERT;
    protected final String LOCAL_VECS;

    public String getLeafName() {
        return leafName;
    }

    //local
    final protected String leafName;
    protected int baseID;

    protected volatile int nodeCount;
    protected IntArrayStack freedIds;
    protected volatile Node<TVector> entryPoint;
    protected Node<TVector>[] nodes;


    //global - same across all leaves
    protected VecHandler<TVector> handler;
    protected Comparator<Double> distanceComparator;
    protected int m;
    protected int maxM;//number of connections allowed each node in higher layers
    protected int maxM0;//number of connections allowed each node in base layer - default to twice
    protected double levelLambda; //constant involved in nodes randomizing
    protected int ef;
    protected int efConstruction; //the size of the set of closest candidates that the heuristic choose from to connect with the new node
    protected boolean removeEnabled;

    public int getMaxNodeCount() {
        return maxNodeCount;
    }

    protected int maxNodeCount;

    final protected HnswManager parent;

    //<external id, internal id>
    protected ConcurrentHashMap<ExternalID, Integer> lookup;

    //runtime specific
    public enum Mode{
        CREATE,
        MODIFY /*add, update, delete vectors*/,
        SEARCH
    }
    Mode mode;
    private LeafSegment(HnswManager parent, int numName){
        HnswConfiguration configuration = parent.getConfiguration();
        this.maxNodeCount = configuration.maxItemLeaf;
        this.handler = configuration.handler;
        this.distanceComparator = configuration.distanceComparator;
        this.m = configuration.m;
        this.maxM = configuration.m;
        this.maxM0 = configuration.m * 2;
        this.levelLambda = 1 / Math.log(this.m);
        this.efConstruction = Math.max(configuration.efConstruction, m);
        this.ef = configuration.ef;
        this.removeEnabled = configuration.removeEnabled;
        this.parent = parent;
        this.lookup = parent.getLookup();
        this.leafName = numName + "_";

        LOCAL_CONFIG = Sp + leafName + "config.o";
        LOCAL_DELETED = Sp + leafName + "deleted.o";
        LOCAL_INCONN = Sp + leafName + "inconns.o";
        LOCAL_OUTCONN = Sp + leafName + "outconns.o";
        LOCAL_INVERT = Sp + leafName + "invert.o";
        LOCAL_VECS = Sp + leafName + "vecs.o";

    }

    //Creation Constructor
    public LeafSegment(HnswManager parent, int numName, int baseID) {
        this(parent, numName);
        this.nodes = new Node[this.maxNodeCount];
        this.freedIds = new IntArrayStack();
        this.baseID = baseID;
        mode = Mode.CREATE;
    }


    //Load constructor
     public LeafSegment(HnswManager parent, int numName, String idxDir, Mode mode){
        this(parent, numName);
        this.mode = mode;
        load(idxDir);
        /*
        if(mode == Mode.SEARCH)
            this.visitedBitSetPool = new GenericObjectPool<>(() -> new ai.preferred.cerebro.hnsw.BitSet(this.nodeCount), Runtime.getRuntime().availableProcessors());
        else if (mode == Mode.MODIFY)
            this.visitedBitSetPool = new GenericObjectPool<>(() -> new ai.preferred.cerebro.hnsw.BitSet(this.maxNodeCount), Runtime.getRuntime().availableProcessors());
         */
    }

    public int getBaseID() {
        return baseID;
    }

    public int size() {
        synchronized (freedIds) {
            return nodeCount - freedIds.size();
        }
    }

    public Optional<TVector> getVec(int internalID) {
        return Optional.ofNullable(nodes[internalID]).map(Node::vector);
    }

    public Optional<Node<TVector>> getNode(int internalID) {
        return Optional.ofNullable(nodes[internalID]);
    }

    public int getNodeCount() {
        return nodeCount;
    }


    protected BoundedMaxHeap searchLayer(Node<TVector> entryPointNode, TVector destination, int k, int layer){
        BitSet visitedBitSet = parent.getBitsetFromPool();
        try {
            //a priority queue which can not grow past the initial capacity
            BoundedMaxHeap topCandidates =
                    new BoundedMaxHeap(k, ()-> null);

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

                MutableIntList candidates = nodes[nodeWithNeighbors.nodeId].outConns[layer];

                for (int i = 0; i < candidates.size(); i++) {

                    int candidateId = candidates.get(i);

                    if (!visitedBitSet.isTrue(candidateId)) {

                        visitedBitSet.flipTrue(candidateId);

                        double candidateDistance = handler.distance(destination,
                                nodes[candidateId].vector());

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
            return topCandidates;
        } finally {
            visitedBitSet.clear();
            parent.returnBitsetToPool(visitedBitSet);
        }
    }

    private boolean checkCorruptedIndex(File configFile, File deletedIdFile,
                                        File inConnectionFile, File outConnectionFile,
                                        File vecsFile, File invertLookUp){
        if(!(IndexUtils.checkFileExist(configFile)
                && IndexUtils.checkFileExist(vecsFile)
                && IndexUtils.checkFileExist(outConnectionFile)
                && IndexUtils.checkFileExist(invertLookUp))) {
            return true;
        }
        if(removeEnabled){
            if(!(IndexUtils.checkFileExist(deletedIdFile) && IndexUtils.checkFileExist(inConnectionFile)))
                return true;
        }
        return false;
    }

    private void load(String dir){
        File configFile = new File(dir + LOCAL_CONFIG);
        File deletedIdFile = new File(dir + LOCAL_DELETED);
        File inConnectionFile = new File(dir + LOCAL_INCONN);
        File outConnectionFile = new File(dir + LOCAL_OUTCONN);
        File vecsFile = new File(dir + LOCAL_VECS);
        File invertLookUpFile = new File(dir + LOCAL_INVERT);

        if (checkCorruptedIndex(configFile, deletedIdFile,
                inConnectionFile, outConnectionFile,
                vecsFile, invertLookUpFile))
            throw new IllegalArgumentException("Index is corrupted");


        int entryID = loadConfig(configFile);
        TVector[] vecs = handler.load(vecsFile);
        IntArrayList[][] outConns = loadConns(outConnectionFile);
        IntArrayList[][] inConns = null;

        int numToLoad = nodeCount;


        if(mode == Mode.MODIFY){
            loadDeletedId(deletedIdFile);
            inConns = loadConns(inConnectionFile);
            numToLoad = maxNodeCount;
        }

        ExternalID [] invertLookUp = loadLookup(invertLookUpFile);
        this.nodes = new Node[numToLoad];

        for (int i = 0; i < nodeCount; i++) {
            IntArrayList[] inconn = null;
            if(removeEnabled && mode == Mode.MODIFY)
                inconn = inConns[i];
            if(vecs[i] != null){
                this.nodes[i] = new Node<>(i,
                                outConns[i],
                                inconn,
                                new Item<>(invertLookUp[i], vecs[i]));
            }
        }
        this.entryPoint = nodes[entryID];
    }

    //To be handled by parent
    private ExternalID[] loadLookup(File lookupFile) {
        Kryo kryo = new Kryo();
        kryo.register(byte[].class);
        kryo.register(byte[][].class);
        try (Input input = new Input(new FileInputStream(lookupFile));){
            String externalID_classname = kryo.readObject(input, String.class);
            Class<?> clazz = Class.forName(externalID_classname);
            Class<?> clazzArray = clazz.arrayType();
            kryo.register(clazz);
            kryo.register(clazzArray);
            return (ExternalID[]) kryo.readObject(input, clazzArray);
        } catch (FileNotFoundException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private IntArrayList[][] loadConns(File connFile) {
        IntArrayList[][] conns = null;
        int[][][] data = null;

        Kryo kryo = new Kryo();
        kryo.register(int[].class);
        kryo.register(int[][].class);
        kryo.register(int[][][].class);
        try{
            Input input = new Input(new FileInputStream(connFile));
            data = kryo.readObject(input, int[][][].class);
            input.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        assert nodeCount == data.length;
        conns = new IntArrayList[data.length][];
        for (int i = 0; i < data.length; i++) {
            conns[i] = new IntArrayList[data[i].length];
            for (int j = 0; j < data[i].length; j++) {
                conns[i][j] = new IntArrayList(data[i][j]);
            }
        }
        return conns;
    }

    private void loadDeletedId(File deletedIdFile) {
        Kryo kryo = new Kryo();
        kryo.register(int[].class);
        kryo.register(IntArrayList.class);
        kryo.register(IntArrayStack.class);
        try {
            Input input = new Input(new FileInputStream(deletedIdFile));
            freedIds = kryo.readObject(input, IntArrayStack.class);
            input.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    //To be handle by parent
    private int loadConfig(File configFile) {
        Kryo kryo = new Kryo();
        Input input = null;
        try {
            input = new Input(new FileInputStream(configFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        baseID = kryo.readObject(input, int.class);
        nodeCount = kryo.readObject(input, int.class);
        //Save the id of entry node
        int entryId = kryo.readObject(input, int.class);
        input.close();
        return entryId;
    }

    static public String capacityInfo(int numName, int maxNodeCount, String idxDir){
        String leafname = numName + "_";
        File configFile = new File(idxDir + Sp + leafname + "config.o");
        File deletedIdFile = new File(idxDir + Sp + leafname + "deleted.o");

        Kryo kryo = new Kryo();
        Input input = null;
        try {
            input = new Input(new FileInputStream(configFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        kryo.readObject(input, int.class);
        int nodeCount = kryo.readObject(input, int.class);
        //Save the id of entry node
        kryo.readObject(input, int.class);
        input.close();

        kryo.register(int[].class);
        kryo.register(IntArrayList.class);
        kryo.register(IntArrayStack.class);
        IntArrayStack deletedIds = null;
        try {
            input = new Input(new FileInputStream(deletedIdFile));
            deletedIds = kryo.readObject(input, IntArrayStack.class);
            input.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return "\tSegment " + numName + ":\n\t\t" + "capacity: " + (nodeCount - deletedIds.size()) + "/" + maxNodeCount;
    }
}
