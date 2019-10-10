package ai.preferred.cerebro.index.search;

import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.ScoreDoc;

public interface Searcher<TVector> {
    ScoreDoc[] keywordSearch(QueryParser queryParser, String sQuery, int resultSize) throws Exception;
    ScoreDoc[] similaritySearch(TVector vQuery, int resultSize) throws Exception;
}
