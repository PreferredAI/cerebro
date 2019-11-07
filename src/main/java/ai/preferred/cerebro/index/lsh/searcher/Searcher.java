package ai.preferred.cerebro.index.lsh.searcher;

import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.ScoreDoc;

public interface Searcher<TVector> {
    VectorSimilarity getVectorSimilarity();
    //void setVectorScoringFunction(VectorSimilarity similarity);
    ScoreDoc[] keywordSearch(QueryParser queryParser, String sQuery, int resultSize) throws Exception;
    ScoreDoc[] similaritySearch(TVector vQuery, int resultSize) throws Exception;
}
