package ai.preferred.cerebro.index;

import ai.preferred.cerebro.index.lsh.searcher.VectorSimilarity;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.ScoreDoc;

/**
 * @author hpminh@apcs.vn
 * @param <TVector>
 */
public interface Searcher<TVector> {
    /**
     * for LSH use only to comply to certain requirements from Lucene
     * @return
     */
    VectorSimilarity getVectorSimilarity();


    /**
     * Query and return docs on a personalized search
     * @param vQuery The vector query.
     * @param resultSize Top result size.
     * @return id strings of the objects for cross reference with database
     * @throws Exception
     */
    String[] similaritySearch(TVector vQuery, int resultSize) throws Exception;
}
