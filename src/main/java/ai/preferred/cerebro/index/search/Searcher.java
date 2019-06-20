package ai.preferred.cerebro.index.search;

import ai.preferred.cerebro.index.exception.UnsupportedDataType;
import ai.preferred.cerebro.index.request.QueryRequest;
import ai.preferred.cerebro.index.response.QueryResponse;
import org.apache.lucene.search.ScoreDoc;

/**
 * Common interface for all kind of searchers Cerebro
 * will use.
 * @param <T> the type of item info wrapper to return
 */
public interface Searcher<T> {
    QueryResponse<T> query(QueryRequest request) throws Exception;
}
