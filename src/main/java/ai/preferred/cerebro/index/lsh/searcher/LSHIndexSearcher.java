package ai.preferred.cerebro.index.lsh.searcher;

import ai.preferred.cerebro.index.Searcher;
import ai.preferred.cerebro.index.common.VecHandler;
import ai.preferred.cerebro.index.lsh.DirectoryFactory;
import ai.preferred.cerebro.index.lsh.builder.LocalitySensitiveHash;
import ai.preferred.cerebro.index.utils.IndexConst;
import ai.preferred.cerebro.index.utils.IndexUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.NIOFSDirectory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

import static ai.preferred.cerebro.index.utils.IndexConst.Sp;

/**
 * Inherited from Lucene's IndexSearcher, this class extends
 * Lucene's traditional full-text search to vector similarity
 * search also.
 * <p>
 * As such it shares almost all of Lucene's IndexSearcher; from
 * thread-safety to I/O speed. Use it as you would use a Lucene
 * searcher. Plus, it now supports vector similarity search via
 * {@link #personalizedSearch(TVector, int)}.
 *
 * @author hpminh@apcs.vn
 */
public class LSHIndexSearcher<TVector> extends IndexSearcher implements Searcher<TVector> {
    protected final ExecutorService executor;
    protected final LeafSlice[] leafSlices;
    protected IndexReader reader;
    private QueryParser defaultParser;
    protected LocalitySensitiveHash<TVector>[] lshs;
    private VectorSimilarity vectorSimilarity;


    /**
     * Create a searcher from the provided index and set of hashing vectors.
     */
    public LSHIndexSearcher(String indexDirectory, boolean useRAM) throws IOException {
        this(indexDirectory, useRAM, null);
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
    public LSHIndexSearcher(String indexDirectory, boolean useRAM, ExecutorService executor) throws IOException {
        super(DirectoryReader.open(DirectoryFactory.getDirectory(indexDirectory, useRAM)).getContext(), executor);
        this.executor = executor;
        this.reader = super.getIndexReader();
        this.defaultParser = new QueryParser(IndexConst.CONTENTS, new StandardAnalyzer());
        this.leafSlices = executor == null ? null : slices(leafContexts);

        VecHandler handler = IndexUtils.loadVectorHandler(indexDirectory + Sp + IndexConst.VECHANDLERFILE);
        this.vectorSimilarity = new VectorSimilarity<>(handler);

        File vecFile = new File(indexDirectory + Sp + IndexConst.HASHVECFILE);

        if(IndexUtils.checkFileExist(vecFile)) {
            TVector[][] splitVecs = (TVector[][]) handler.load(vecFile);
            lshs = new LocalitySensitiveHash[splitVecs.length];
            for (int i = 0; i < splitVecs.length; i++) {
                lshs[i] = new LocalitySensitiveHash<>(handler, splitVecs[i]);
            }
        }
        else {
            IndexUtils.notifyLazyImplementation("LSHIndexSearcher: implement when hash is null");
        }
    }

    /**
     * Set the hashing vectors outside of constructor
     * @param hashingVecs hashing vectors
     */
    public void setLSH(VecHandler<TVector> handler, TVector[][] hashingVecs){
        lshs = new LocalitySensitiveHash[hashingVecs.length];
        for (int i = 0; i < hashingVecs.length; i++) {
            lshs[i] = new LocalitySensitiveHash<>(handler, hashingVecs[i]);
        }
    }


    public TopDocs personalizedSearch(TVector vQuery, int topK)
            throws Exception{
        if(lshs == null)
            throw new Exception("LocalitySensitiveHash not initialized");
        if(lshs.length > 1){
            TopDocs[] subs = new TopDocs[lshs.length];
            for (int i = 0; i < lshs.length; i++) {
                Term t = new Term(IndexConst.HashFieldName + i, lshs[i].getHashBit(vQuery));
                VectorQuery<TVector> query = new VectorQuery<>(vQuery, t);
                int count = count(query);
                subs[i] =  search(query, Math.min(topK, count));
            }
            return TopDocs.merge(topK, subs);
        }
        else{
            Term t = new Term(IndexConst.HashFieldName + 0, lshs[0].getHashBit(vQuery));
            VectorQuery<TVector> query = new VectorQuery<>(vQuery, t);
            int count = count(query);
            if (count == 0)
                return null;
            return search(query, Math.min(topK, count));
        }

    }

    @Override
    public VectorSimilarity getVectorSimilarity() {
        return vectorSimilarity;
    }


    /**
     * Query and return docs on a keyword search
     * @param queryParser if null the searcher will by default carry search on
     *                    the field named {@link IndexConst#CONTENTS}.
     * @param sQuery String query.
     * @param resultSize Top result size.
     * @return A set of {@link ScoreDoc} of Document matching with the query.
     * @throws Exception
     */
    public ScoreDoc[] keywordSearch(QueryParser queryParser, String sQuery, int resultSize) throws Exception {
        Query query = null;
        if(queryParser == null)
            query = defaultParser.parse(sQuery);
        else
            query = queryParser.parse(sQuery);
        TopDocs hits = search(query, resultSize);
        return hits == null ? null : hits.scoreDocs;
    }


    @Override
    public String[] similaritySearch(TVector vQuery, int resultSize) throws Exception{
        TopDocs hits = personalizedSearch(vQuery, resultSize);
        String[] ret = new String[hits.scoreDocs.length];
        int i = 0;
        for (ScoreDoc docId: hits.scoreDocs) {
            ret[i++] = this.doc(docId.doc).get(IndexConst.IDFieldName);
        }
        return ret;
    }

    /*
    garbage API to be changed soon
    @Override
    public QueryResponse<ScoreDoc> query(QueryRequest qRequest) throws Exception {
        switch (qRequest.getType()){
            case KEYWORD:
                Object queryData = qRequest.getQueryData();
                int k = qRequest.getTopK();
                if (queryData instanceof String){
                    return keywordSearch(null, (String) queryData, k);
                }
                return new QueryResponse<ScoreDoc>(keywordSearch(, ));
            case VECTOR:
                return new QueryResponse<ScoreDoc>(similaritySearch((TVector) qRequest.getQueryData(), qRequest.getTopK()));
            default:
                throw new UnsupportedDataType();
        }
    }

     */

    /*
    private ScoreDoc[] processKeyword(Object queryData, int topK) throws Exception {
        //assume field name is contents
        if (queryData instanceof String){
            return keywordSearch(null, (String) queryData, topK);
        }
        //assume [0] is fieldname, [1] is query string
        else if(queryData instanceof String[]){
            String[] fieldnameAndQuery = (String[])queryData;
            QueryParser parser = new QueryParser(fieldnameAndQuery[0], new StandardAnalyzer());
            return keywordSearch(parser, fieldnameAndQuery[1], topK);
        }
        throw new UnsupportedDataType(queryData.getClass(), String.class, String[].class);
    }

    private ScoreDoc[] processVec(Object queryData, int topK) throws Exception {
        //assume field name is contents
        if (queryData instanceof double[]){
            return similaritySearch((double[]) queryData, topK);
        }
        //assume [0] is fieldname, [1] is query string
        else if(queryData instanceof DenseVector){
            double[] vec = ((DenseVector) queryData).getElements();
            return similaritySearch(vec, topK);
        }
        throw new UnsupportedDataType(queryData.getClass(), double[].class, DenseVector.class);
    }
     */
    public void close(){
        if(executor != null){
            executor.shutdown();
        }
    }
}

