package ai.preferred.cerebro.index.hnsw.builder;

import ai.preferred.cerebro.index.hnsw.*;
import ai.preferred.cerebro.index.hnsw.notify.CLIProgressListener;
import ai.preferred.cerebro.index.hnsw.notify.ProgressListener;
import ai.preferred.cerebro.index.common.BitSet;
import ai.preferred.cerebro.index.hnsw.Item;
import ai.preferred.cerebro.index.ids.ExternalID;
import ai.preferred.cerebro.index.utils.IndexUtils;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

import java.io.*;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author hpminh@apcs.vn
 */
public final class HnswIndexWriter<TVector> extends HnswManager<TVector>
        implements ConcurrentWriter<TVector> {
    private final int DEFAULT_INITIAL_MAX_NUM_LEAVES  = 16;
    private final int OPTIMAL_NUM_LEAVES;

    //Create Constructor
    public HnswIndexWriter(HnswConfiguration configuration, String dir) {
        if (!isSafeToCreate(dir)){
            throw new IllegalArgumentException("An index has already resided in this directory. Can only modify.");
        }
        this.configuration = configuration;
        this.idxDir = dir;
        OPTIMAL_NUM_LEAVES = Runtime.getRuntime().availableProcessors();

        this.visitedBitSetPool = new GenericObjectPool<>(() -> new BitSet(configuration.getMaxItemLeaf()), Math.max(OPTIMAL_NUM_LEAVES, nleaves));


        if (configuration.isLowMemoryMode())
            nleaves = 1;
        //not sure whether to make this a feature, decide later. For now this is only for experiment.
        //In case we don't want to commit every threads available to the first insertion, but still want
        //to adopt the segment-wise approach
        else if(OPTIMAL_NUM_LEAVES > DEFAULT_INITIAL_MAX_NUM_LEAVES)
            nleaves = DEFAULT_INITIAL_MAX_NUM_LEAVES;
        else
            //Initialize all leaves with default max num of nodes
            nleaves = OPTIMAL_NUM_LEAVES;
        lookup = new ConcurrentHashMap<>(nleaves * configuration.getMaxItemLeaf());

        leaves = new LeafSegmentWriter[nleaves];
        int baseNewLeaf = 0;
        for (int i = 0; i < nleaves; i++) {
            if (configuration.isLowMemoryMode())
                leaves[i] = new LeafSegmentBlockingWriter<>(this, i, baseNewLeaf);
            else
                leaves[i] = new LeafSegmentWriter<>(this, i, baseNewLeaf);
            baseNewLeaf += configuration.getMaxItemLeaf();
        }

    }

    //Load up an already created index for modifying
    public HnswIndexWriter(String dir){
        super(dir);
        OPTIMAL_NUM_LEAVES = Runtime.getRuntime().availableProcessors();
        this.visitedBitSetPool = new GenericObjectPool<>(() -> new BitSet(configuration.getMaxItemLeaf()), nleaves);
        //load all leaves
        for (int i = 0; i < nleaves; i++) {
            leaves[i] = new LeafSegmentWriter<>(this, i, idxDir);
        }
    }

    private boolean isSafeToCreate(String idxDir){
        File file = new File(idxDir + globalConfigFileName);
        return !IndexUtils.checkFileExist(file);
    }


    private synchronized LeafSegmentWriter growNewLeaf(String leafInAction, boolean isLeafBlocking){
        if (leafInAction.compareTo(leaves[nleaves - 1].getLeafName()) == 0) {
            System.out.println("Current segment reached maximum capacity, creating and switching to use a new segment.");
            if (leaves.length == nleaves){
                LeafSegment<TVector>[] hold = leaves;
                leaves = new LeafSegmentWriter[nleaves + 5];
                System.arraycopy(hold, 0, leaves, 0, hold.length);
            }
            if (isLeafBlocking)
                leaves[nleaves] = new LeafSegmentBlockingWriter<>(this, nleaves,configuration.getMaxItemLeaf() * nleaves++);
            else
                leaves[nleaves] = new LeafSegmentWriter<>(this, nleaves,configuration.getMaxItemLeaf() * nleaves++);
            return (LeafSegmentWriter) leaves[nleaves - 1];
        }
        else if(leafInAction.compareTo(leaves[nleaves - 1].getLeafName()) < 0){
            return (LeafSegmentWriter) leaves[nleaves - 1];
        }
        else
            throw new IllegalArgumentException("In-action leaf's index should not be greater than the number of leaves minus one");
    }
/*
    private synchronized SynchronizedLeafHnswWriter chooseLeaf(){
        if (leaves[nleaves - 1].size() >= configuration.maxItemLeaf){
            if(nleaves < OPTIMAL_NUM_LEAVES){
                return createLeaf();
            }else {
                for (int i = 0; i < OPTIMAL_NUM_LEAVES; i++) {
                    if(leaves[i].size() < leaves[i].maxNodeCount)
                        return leaves[i];
                }
                throw new IllegalArgumentException("Some errors occur when checking capacity");
            }
        }
        else
            return leaves[nleaves - 1];
    }
     */


    public void removeByExternalID(ExternalID externalID) {
        int globalID = lookup.getOrDefault(externalID, -1);
        if(globalID >= 0){
            int leafNum = globalID / configuration.getMaxItemLeaf();
            int internalID = globalID % configuration.getMaxItemLeaf();
            lookup.remove(externalID);
            ((LeafSegmentWriter)leaves[leafNum]).removeOnInternalID(internalID);
        }
    }


    private String checkCapacity(int amountToInsert){
        int remainingSlots = 0;
        for (int i = 0; i < nleaves; i++) {
            remainingSlots += leaves[i].getMaxNodeCount() - leaves[i].size();
        }
        if(nleaves < OPTIMAL_NUM_LEAVES){
            remainingSlots += (OPTIMAL_NUM_LEAVES - nleaves) * configuration.getMaxItemLeaf();
        }
        if (remainingSlots >= amountToInsert)
            return null;
        else
            return "Not enough space, call expand() before add. Operation failed." +
                    "\nSpace needed: " + amountToInsert + ", Space had: " + remainingSlots;
    }

    public int size() {
        int size = 0;
        for (int i = 0; i < nleaves; i++) {
            size += leaves[i].size();
        }
        return size;
    }


    @Override
    public void addAll(Collection<Item<TVector>> items) throws InterruptedException {
        String message = checkCapacity(items.size());
        if(message == null){
            if (configuration.isLowMemoryMode())
                singleSegmentAddAll(items, OPTIMAL_NUM_LEAVES, CLIProgressListener.INSTANCE, DEFAULT_PROGRESS_UPDATE_INTERVAL);
            else
                addAll(items, nleaves, CLIProgressListener.INSTANCE, DEFAULT_PROGRESS_UPDATE_INTERVAL);
        }

        else
            throw new IllegalArgumentException(message);
    }

    @Override
    public void addAll(Collection<Item<TVector>> items, int numThreads, ProgressListener listener, int progressUpdateInterval) throws InterruptedException {
        AtomicReference<RuntimeException> throwableHolder = new AtomicReference<>();

        ExecutorService executorService = Executors.newFixedThreadPool(numThreads, new NamedThreadFactory("indexer-%d"));

        AtomicInteger workDone = new AtomicInteger();

        try{
            Queue<Item> queue = new LinkedBlockingDeque<>(items);
            CountDownLatch latch = new CountDownLatch(numThreads);
            int maxsize = items.size();
            for (int threadId = 0; threadId < numThreads; threadId++)
                executorService.submit(
                        new InsertItemTask((LeafSegmentWriter)leaves[threadId],
                                queue,
                                maxsize,
                                throwableHolder,
                                workDone,
                                latch,
                                listener));

            latch.await();

            RuntimeException throwable = throwableHolder.get();
            if (throwable != null)
                throw throwable;
            }
        finally {
            executorService.shutdown();
        }
    }

    public void singleSegmentAddAll(Collection<Item<TVector>> items, int numThreads, ProgressListener listener, int progressUpdateInterval) throws InterruptedException {
        AtomicReference<RuntimeException> throwableHolder = new AtomicReference<>();

        ExecutorService executorService = Executors.newFixedThreadPool(numThreads,
                new NamedThreadFactory("indexer-%d"));

        AtomicInteger workDone = new AtomicInteger();

        try {
            Queue<Item<TVector>> queue = new LinkedBlockingDeque<>(items);

            CountDownLatch latch = new CountDownLatch(numThreads);
            //final AtomicInteger idxleafInAction = new AtomicInteger(nleaves - 1);
            LeafSegmentBlockingWriter<TVector> leafInAction = (LeafSegmentBlockingWriter<TVector>) leaves[nleaves - 1];
            for (int threadId = 0; threadId < numThreads; threadId++) {

                executorService.submit(() -> {
                    LeafSegmentBlockingWriter<TVector> leaf = leafInAction;
                    Item<TVector> item;
                    while(throwableHolder.get() == null && (item = queue.poll()) != null) {
                        try {
                            boolean signal = leaf.add(item);
                            if (signal){
                                int done = workDone.incrementAndGet();

                                if (done % progressUpdateInterval == 0) {
                                    listener.updateProgress(done, items.size());
                                }
                            }
                            //here we assume that add(item) return false when
                            //the segment when reached its maximum capacity
                            else {
                                leaf = (LeafSegmentBlockingWriter<TVector>) growNewLeaf(leaf.getLeafName(), true);
                                leaf.add(item);
                            }
                        } catch (RuntimeException t) {
                            throwableHolder.set(t);
                        }
                    }

                    latch.countDown();
                });
            }

            latch.await();

            RuntimeException throwable = throwableHolder.get();

            if (throwable != null) {
                throw throwable;
            }

        } finally {
            executorService.shutdown();
        }
    }

    @Override
    public void save() throws IOException {
        synchronized (configuration){
            Kryo kryo = new Kryo();
            kryo.register(String.class);
            try {
                Output output = new Output(new FileOutputStream(idxDir + globalConfigFileName));
                kryo.writeObject(output, configuration.getHandler().getClass().getCanonicalName());
                kryo.writeObject(output, configuration.getM());
                kryo.writeObject(output, configuration.getEf());
                kryo.writeObject(output, configuration.getEfConstruction());
                kryo.writeObject(output, configuration.isRemoveEnabled());
                kryo.writeObject(output, configuration.isLowMemoryMode());
                kryo.writeObject(output, configuration.getMaxItemLeaf());
                kryo.writeObject(output, nleaves);
                output.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < nleaves; i++) {
            ((LeafSegmentWriter)leaves[i]).save(idxDir);
        }
    }

    static class InsertItemTask implements Runnable{
        final static int DEFAULT_PROGRESS_UPDATE_INTERVAL = 1_000;
        final private Queue<Item> itemQueue;
        final private AtomicReference<RuntimeException> throwableHolder;
        final private AtomicInteger workDone;
        final private CountDownLatch latch;
        final private ProgressListener listener;
        private LeafSegmentWriter leaf;
        final private int max;
        InsertItemTask(LeafSegmentWriter leaf,
                       Queue<Item> itemQueue,
                       int maxsize,
                       AtomicReference<RuntimeException> throwableHolder,
                       AtomicInteger workDone,
                       CountDownLatch latch,
                       ProgressListener listener){
            this.leaf = leaf;
            this.itemQueue = itemQueue;
            this.throwableHolder = throwableHolder;
            this.workDone = workDone;
            this.latch = latch;
            this.listener = listener;
            max = maxsize;
        }
        @Override
        public void run() {
            Item item;
            while(throwableHolder.get() == null && (item = itemQueue.poll()) != null) {
                try {
                    if (leaf.add(item)){
                        int done = workDone.incrementAndGet();
                        if (done % DEFAULT_PROGRESS_UPDATE_INTERVAL == 0) {
                            listener.updateProgress(done, max);
                        }
                    }
                    else
                        itemQueue.add(item);

                } catch (RuntimeException t) {
                    throwableHolder.set(t);
                }
            }
            latch.countDown();
        }
    }
}
