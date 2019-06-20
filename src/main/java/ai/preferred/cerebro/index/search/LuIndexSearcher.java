package ai.preferred.cerebro.index.search;


import ai.preferred.cerebro.index.exception.UnsupportedDataType;
import ai.preferred.cerebro.index.request.QueryRequest;
import ai.preferred.cerebro.index.response.QueryResponse;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.ThreadInterruptedException;

import ai.preferred.cerebro.index.builder.LocalitySensitiveHash;
import ai.preferred.cerebro.index.utils.IndexConst;
import ai.preferred.cerebro.index.utils.IndexUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Inherited from Lucene's IndexSearcher, this class extends
 * Lucene's traditional full-text search to vector similarity
 * search also.
 * <p>
 * As such it shares almost all of Lucene's IndexSearcher; from
 * thread-safety to I/O speed. Use it as you would use a Lucene
 * searcher. Plus, it now supports vector similarity search via
 * {@link #queryVector(double[], int)}.
 *
 * @author hpminh@apcs.vn
 */
public class LuIndexSearcher extends IndexSearcher implements Searcher<ScoreDoc> {
    protected final ExecutorService executor;
    protected final LeafSlice[] leafSlices;
    protected IndexReader reader;
    QueryParser defaultParser;
    private LocalitySensitiveHash lsh;


    /**
     * Create a searcher from the provided index and set of hashing vectors.
     */
    public LuIndexSearcher(IndexReader r, String splitVecPath) throws IOException {
        this(r.getContext(), null, splitVecPath);
    }

    /** Runs searches for each segment separately, using the
     *  provided ExecutorService.  IndexSearcher will not
     *  close/awaitTermination this ExecutorService on
     *  close; you must do so, eventually, on your own.  NOTE:
     *  if you are using {@link NIOFSDirectory}, do not use
     *  the shutdownNow method of ExecutorService as this uses
     *  Thread.interrupt under-the-hood which can silently
     *  close file descriptors (see <a
     *  href="https://issues.apache.org/jira/browse/LUCENE-2239">LUCENE-2239</a>).
     */
    public LuIndexSearcher(IndexReader r, ExecutorService executor, String splitVecPath) throws IOException {
        this(r.getContext(), executor, splitVecPath);
    }

    public LuIndexSearcher(IndexReaderContext context, ExecutorService executor, String splitVecPath) throws IOException {
        super(context, executor);
        this.executor = executor;
        this.reader = context.reader();
        this.defaultParser = new QueryParser(IndexConst.CONTENTS, new StandardAnalyzer());
        this.leafSlices = executor == null ? null : slices(leafContexts);
        if(splitVecPath != null){
            double[][] splitVecs = IndexUtils.readVectors(splitVecPath);
            lsh = new LocalitySensitiveHash(splitVecs);
        }
    }

    private TopDocs personalizedSearch(double [] vQuery, int topK)
            throws Exception{
        if(lsh == null)
            throw new Exception("LocalitySensitiveHash not initialized");
        Term t = new Term(IndexConst.HashFieldName, lsh.getHashBit(vQuery));
        // count the number of document that matches with this hashcode
        int count = 0;
        for (LeafReaderContext leaf : reader.leaves())
            count += leaf.reader().docFreq(t);
        if(count == 0){
            return null;
        }
        LatentVectorQuery query = new LatentVectorQuery(vQuery, t);
        return search(query, topK < count ? topK : count);
        //return pSearch(query, count, topK);
    }

    private TopDocs pSearch(Query query, int count, int topK)
            throws IOException {
        return pSearchAfter(null, query, count, topK);
    }

    private TopDocs pSearchAfter(ScoreDoc after, Query query, int count, int topK) throws IOException {
        if(after != null){
            IndexUtils.notifyLazyImplementation("LuIndexSearcher / pSearchAfter");
        }
        final int limit = Math.max(1, reader.maxDoc());
        if (after != null && after.doc >= limit) {
            throw new IllegalArgumentException("after.doc exceeds the number of documents in the reader: after.doc="
                    + after.doc + " limit=" + limit);
        }

        final int cappedNumHits = Math.min(count, limit);

        final CollectorManager<CeTopScoreDocCollector, TopDocs> manager = new CollectorManager<CeTopScoreDocCollector, TopDocs>() {

            @Override
            public CeTopScoreDocCollector newCollector() throws IOException {
                return new CeTopScoreDocCollector(cappedNumHits, topK);
            }

            @Override
            public TopDocs reduce(Collection<CeTopScoreDocCollector> collectors) throws IOException {
                final TopDocs[] topDocs = new TopDocs[collectors.size()];
                int i = 0;
                for (CeTopScoreDocCollector collector : collectors) {
                    collector.pullTopK();
                    topDocs[i++] = collector.topDocs();
                }
                return TopDocs.merge(0, cappedNumHits, topDocs, true);
            }

        };

        return pSearch(query, manager);
    }

    private <C extends Collector, T> T pSearch(Query query, CollectorManager<C, T> collectorManager) throws IOException {
        if (executor == null) {
            final C collector = collectorManager.newCollector();
            pSearch(query, collector);
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
            final Weight weight = createWeight(query, needsScores, 1);
            final List<Future<C>> topDocsFutures = new ArrayList<>(leafSlices.length);
            for (int i = 0; i < leafSlices.length; ++i) {
                final LeafReaderContext[] leaves = leafSlices[i].leaves;
                final C collector = collectors.get(i);
                topDocsFutures.add(executor.submit(new Callable<C>() {
                    @Override
                    public C call() throws Exception {
                        pSearch(Arrays.asList(leaves), weight, collector);
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

    private void pSearch(Query query, Collector results)
            throws IOException {
        query = rewrite(query);
        pSearch(leafContexts, createWeight(query, results.needsScores(), 1), results);
    }

    private void pSearch(List<LeafReaderContext> leaves, Weight weight, Collector collector)
            throws IOException {

        // TODO: should we make this
        // threaded...?  the Collector could be sync'd?
        // always use single thread:
        for (LeafReaderContext ctx : leaves) { // search each subreader
            final LeafCollector leafCollector;
            try {
                leafCollector = collector.getLeafCollector(ctx);
            } catch (CollectionTerminatedException e) {
                // there is no doc of interest in this reader context
                // continue with the following leaf
                continue;
            }
            BulkScorer scorer = weight.bulkScorer(ctx);
            if (scorer != null) {
                try {
                    scorer.score(leafCollector, ctx.reader().getLiveDocs());

                } catch (CollectionTerminatedException e) {
                    // collection was terminated prematurely
                    // continue with the following leaf
                }
            }
        }
    }

    /**
     * @param queryParser if null the searcher will by default carry search on
     *                    the field named {@link IndexConst#CONTENTS}.
     * @param sQuery String query.
     * @param resultSize Top result size.
     * @return A set of {@link ScoreDoc} of Document matching with the query.
     * @throws Exception
     */
    public ScoreDoc[] queryKeyWord(QueryParser queryParser, String sQuery, int resultSize) throws Exception {
        Query query = null;
        if(queryParser == null)
            query = defaultParser.parse(sQuery);
        else
            query = queryParser.parse(sQuery);
        TopDocs hits = search(query, resultSize);
        return hits == null ? null : hits.scoreDocs;
    }

    /**
     *
     * @param vQuery The vector query.
     * @param resultSize Top result size.
     * @return A set of {@link ScoreDoc} of Document having latent vector producing.
     * the highest inner product with the query vector.
     * @throws Exception
     */
    public ScoreDoc[] queryVector(double[] vQuery, int resultSize) throws Exception {
        TopDocs hits = personalizedSearch(vQuery, resultSize);
        return hits == null ? null : hits.scoreDocs;
    }

    /**
     * Process both type of query text and vector
     * and carry out searching.
     * @param qRequest
     * @return
     * @throws Exception
     */
    @Override
    public QueryResponse<ScoreDoc> query(QueryRequest qRequest) throws Exception {
        switch (qRequest.getType()){
            case KEYWORD:
                return new QueryResponse<ScoreDoc>(processKeyword(qRequest.getQueryData(), qRequest.getTopK()));
            case VECTOR:
                return new QueryResponse<ScoreDoc>(queryVector((double[])qRequest.getQueryData(), qRequest.getTopK()));
            default:
                throw new UnsupportedDataType();
        }
    }

    /**
     * Handle the case when we want to query a field with a custom name,
     * not the default {@link IndexConst#CONTENTS}.
     * @param queryData
     * @param topK
     * @return
     * @throws Exception
     */
    private ScoreDoc[] processKeyword(Object queryData, int topK) throws Exception {
        //assume field name is contents
        if (queryData instanceof String){
            return queryKeyWord(null, (String) queryData, topK);
        }
        //assume [0] is fieldname, [1] is query string
        else if(queryData instanceof String[]){
            String[] fieldnameAndQuery = (String[])queryData;
            QueryParser parser = new QueryParser(fieldnameAndQuery[0], new StandardAnalyzer());
            return queryKeyWord(parser, fieldnameAndQuery[1], topK);
        }
        throw new UnsupportedDataType();
    }
}

