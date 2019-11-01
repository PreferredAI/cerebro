package ai.preferred.cerebro.index.hnsw;

import ai.preferred.cerebro.index.hnsw.Item;

import java.io.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Read write K-nearest neighbors search index.
 *
 //* @param <T> Type of distance between items (expect any numeric type: float, double, int, ..)
 *
 * @see <a href="https://en.wikipedia.org/wiki/K-nearest_neighbors_algorithm">k-nearest neighbors algorithm</a>
 */
public interface ConcurrentWriter<TVector> extends Serializable {

    /**
     * By default after indexing this many items progress will be reported to registered progress listeners.
     */
    int DEFAULT_PROGRESS_UPDATE_INTERVAL = 1_000;

    /**
     * Add a new item to the index. If the item already exists in the index the old item will first be removed from the
     * index. for this removes need to be enabled for the index.
     *
     * @param item the item to add to the index
     */
     //boolean add(Item item);

    /**
     * Removes an item from the index.
     *
     * @param id unique identifier or the item to remove
     * @return {@code true} if an item was removed from the index. In case the index does not support removals this will
     *                      always be false
     */
    //boolean remove(int id);

    /**
     * Add multiple items to the index
     *
     * @param items the items to add to the index
     * @throws InterruptedException thrown when the thread doing the indexing is interrupted
     */
    default void addAll(Collection<Item<TVector>> items) throws InterruptedException {
        addAll(items, CLIProgressListener.INSTANCE);
    }

    /**
     * Add multiple items to the index. Reports progress to the passed in implementation of {@link ProgressListener}
     * every {@link ConcurrentWriter#DEFAULT_PROGRESS_UPDATE_INTERVAL} elements indexed.
     *
     * @param items the items to add to the index
     * @param listener listener to report progress to
     * @throws InterruptedException thrown when the thread doing the indexing is interrupted
     */
    default void addAll(Collection<Item<TVector>> items, ProgressListener listener) throws InterruptedException {
        addAll(items, Runtime.getRuntime().availableProcessors(), listener, DEFAULT_PROGRESS_UPDATE_INTERVAL);
    }

    /**
     * Add multiple items to the index. Reports progress to the passed in implementation of {@link ProgressListener}
     * every progressUpdateInterval elements indexed.
     *
     * @param items the items to add to the index
     * @param numThreads number of threads to use for parallel indexing
     * @param listener listener to report progress to
     * @param progressUpdateInterval after indexing this many items progress will be reported
     * @throws InterruptedException thrown when the thread doing the indexing is interrupted
     */
    default void addAll(Collection<Item<TVector>> items, int numThreads, ProgressListener listener, int progressUpdateInterval)
            throws InterruptedException {

        AtomicReference<RuntimeException> throwableHolder = new AtomicReference<>();

        ExecutorService executorService = Executors.newFixedThreadPool(numThreads,
                new NamedThreadFactory("indexer-%d"));

        AtomicInteger workDone = new AtomicInteger();

        try {
            Queue<Item> queue = new LinkedBlockingDeque<>(items);

            CountDownLatch latch = new CountDownLatch(numThreads);

            for (int threadId = 0; threadId < numThreads; threadId++) {

                executorService.submit(() -> {
                    Item item;
                    while(throwableHolder.get() == null && (item = queue.poll()) != null) {
                        try {
                            //add(item);
                            int done = workDone.incrementAndGet();

                            if (done % progressUpdateInterval == 0) {
                                listener.updateProgress(done, items.size());
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

    /**
     * Returns the size of the index.
     *
     * @return size of the index
     */
    //int size();

    /**
     * Returns an item by its identifier.
     *
     * @param id unique identifier or the item to return
     * @return an item
     */
    //Optional<double[]> get(int id);

    /**
     * Find the items closest to the passed in vector.
     *
     * @param vector the vector
     * @param k number of items to return
     * @return the items closest to the passed in vector
     */
    //List<Integer> findNearest(double[] vector, int k);

    /**
     * Find the items closest to the item identified by the passed in id. If the id does not match an item an empty
     * list is returned. the element itself is not included in the response.
     *
     * @param id id of the item to find the neighbors of
     * @param k number of items to return
     * @return the items closest to the item
     */
//    default List<ScoreDoc<T>> findNeighbors(int id, int k) {
//        return get(id).map(item -> findNearest(item, k + 1).stream()
//                .filter(result -> !result.item().id().equals(id))
//                .limit(k)
//                .collect(Collectors.toList()))
//                .orElse(Collections.emptyList());
//    }

    /**
     * Saves the index to an OutputStream. Saving may lock the index for updates.
     *
     * @param out the output stream to write the index to
     * @throws IOException in case of I/O exception
     */
    //void save(OutputStream out) throws IOException;

    /**
     * Saves the index to a directory. Saving may lock the index for updates.
     *
     * @throws IOException in case of I/O exception
     */
     void save() throws IOException;

    /**
     * Saves the index to a path. Saving may lock the index for updates.
     *
     * @param path file to write the index to
     * @throws IOException in case of I/O exception
     */
//    default void save(Path path) throws IOException {
//        save(Files.newOutputStream(path));
//    }

}