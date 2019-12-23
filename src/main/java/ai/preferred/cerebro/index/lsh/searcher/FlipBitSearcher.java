package ai.preferred.cerebro.index.lsh.searcher;
import ai.preferred.cerebro.index.utils.IndexConst;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.ThreadInterruptedException;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;


/**
 * class to perform search like {@link LSHIndexSearcher}, but also carry out search on the nearest sub-space determined
 * by flipping the nearest hash-bit
 *
 * @author hpminh@apcs.vn
 */
public class FlipBitSearcher<TVector> extends LSHIndexSearcher<TVector> {
    public FlipBitSearcher(String indexDirectory, boolean useRAM) throws IOException {
        super(indexDirectory, useRAM);
    }

    public FlipBitSearcher(String indexDirectory, boolean useRAM, ExecutorService executor) throws IOException {
        super(indexDirectory, useRAM, executor);
    }

    @Override
    public TopDocs personalizedSearch(TVector vQuery, int topK)throws Exception{
        if(lshs == null)
            throw new Exception("LocalitySensitiveHash not initialized");
        BytesRef[] hashcodes = lshs[0].getFlipHashBit(vQuery);
        LinkedList<Weight> weights = new LinkedList<>();

        final int cappedNumHits = topK;
        final CollectorManager<TopScoreDocCollector, TopDocs> manager = new CollectorManager<TopScoreDocCollector, TopDocs>() {

            @Override
            public TopScoreDocCollector newCollector() throws IOException {
                return TopScoreDocCollector.create(cappedNumHits, null);
            }

            @Override
            public TopDocs reduce(Collection<TopScoreDocCollector> collectors) throws IOException {
                final TopDocs[] topDocs = new TopDocs[collectors.size()];
                int i = 0;
                for (TopScoreDocCollector collector : collectors) {
                    topDocs[i++] = collector.topDocs();
                }
                return TopDocs.merge(0, cappedNumHits, topDocs, true);
            }

        };
        final LinkedList<Collector> collectors = new LinkedList<>();
        for (int i = 0; i < leafSlices.length * hashcodes.length; ++i) {
            final Collector collector = manager.newCollector();
            collectors.add(collector);
        }
        final List<Future<Collector>> topDocsFutures = new ArrayList<>(leafSlices.length * hashcodes.length);

        for (int i = 0; i < hashcodes.length; i++) {
            Term term = new Term(IndexConst.HashFieldName, hashcodes[i]);
            for (LeafReaderContext leaf : reader.leaves()){
                if(leaf.reader().docFreq(term) > 0){
                    Collector collector = collectors.pollFirst();
                    VectorQuery<TVector> query = new VectorQuery<>(vQuery, term);
                    query = (VectorQuery<TVector>) rewrite(query);
                    final Weight weight = createWeight(query, true, 1);
                    topDocsFutures.add(executor.submit(new Callable<Collector>() {
                        @Override
                        public Collector call() throws Exception {
                            search(Arrays.asList(leaf), weight, collector);
                            return collector;
                        }
                    }));
                }
            }

        }
        final List<TopScoreDocCollector> collectedCollectors = new ArrayList<>();
        for (Future<Collector> future : topDocsFutures) {
            try {
                collectedCollectors.add((TopScoreDocCollector) future.get());
            } catch (InterruptedException e) {
                throw new ThreadInterruptedException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        return manager.reduce(collectedCollectors);
    }


    /*
    public TopDocs searchAfter(ScoreDoc after, Query query, Query flip, int numHits) throws IOException {
        final int limit = Math.max(1, reader.maxDoc());
        if (after != null && after.doc >= limit) {
            throw new IllegalArgumentException("after.doc exceeds the number of documents in the reader: after.doc="
                    + after.doc + " limit=" + limit);
        }

        final int cappedNumHits = Math.min(numHits, limit);

        final CollectorManager<TopScoreDocCollector, TopDocs> manager = new CollectorManager<TopScoreDocCollector, TopDocs>() {

            @Override
            public TopScoreDocCollector newCollector() throws IOException {
                return TopScoreDocCollector.create(cappedNumHits, after);
            }

            @Override
            public TopDocs reduce(Collection<TopScoreDocCollector> collectors) throws IOException {
                final TopDocs[] topDocs = new TopDocs[collectors.size()];
                int i = 0;
                for (TopScoreDocCollector collector : collectors) {
                    topDocs[i++] = collector.topDocs();
                }
                return TopDocs.merge(0, cappedNumHits, topDocs, true);
            }

        };
        return search(query, flip, manager);
    }


    public <C extends Collector, T> T search(Query query, Query flip, CollectorManager<C, T> collectorManager) throws IOException {
        if (executor == null) {
            final C collector = collectorManager.newCollector();
            search(query, collector);
            return collectorManager.reduce(Collections.singletonList(collector));
        } else {
            final List<C> collectors = new ArrayList<>(leafSlices.length);
            boolean needsScores = false;
            for (int i = 0; i < leafSlices.length; ++i) {
                final C collector = collectorManager.newCollector();
                collectors.add(collector);
                needsScores |= collector.needsScores();
            }

            query = rewrite(query);
            flip = rewrite(flip);
            final Weight weight = createWeight(query, needsScores, 1);
            final Weight flipWeight = createWeight(flip, needsScores, 1);
            final List<Future<C>> topDocsFutures = new ArrayList<>(leafSlices.length);
            for (int i = 0; i < leafSlices.length; ++i) {
                final LeafReaderContext[] leaves = leafSlices[i].leaves;
                final C collector = collectors.get(i);
                topDocsFutures.add(executor.submit(new Callable<C>() {
                    @Override
                    public C call() throws Exception {
                        search(Arrays.asList(leaves), weight, collector);
                        return collector;
                    }
                }));
                topDocsFutures.add(executor.submit(new Callable<C>() {
                    @Override
                    public C call() throws Exception {
                        search(Arrays.asList(leaves), flipWeight, collector);
                        return collector;
                    }
                }));
            }

            final List<C> collectedCollectors = new ArrayList<>();
            for (Future<C> future : topDocsFutures) {
                try {
                    collectedCollectors.add(future.get());
                } catch (InterruptedException e) {
                    throw new ThreadInterruptedException(e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
            return collectorManager.reduce(collectors);
        }
    }
*/

}
