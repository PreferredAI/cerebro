package ai.preferred.cerebro.index.response;

import org.apache.lucene.search.ScoreDoc;

import ai.preferred.cerebro.core.entity.TopKItem;

import java.util.ArrayList;
import java.util.List;


/**
 * A class to wrap up the result of any query pass to a
 * {@link ai.preferred.cerebro.index.search.processor.QueryProcessor}.
 * This is to handle communication between the index and other unreleased
 * Cerebro components. You can Get the result of your queries directly from
 * your searcher if you want to.
 */
public class QueryResponse<T> {
    public QueryResponse(T[] rankedItemList){
        this.rankedItemList = rankedItemList;
    }
    public T[] getRankedItemList() {
        return rankedItemList;
    }

    private T[] rankedItemList;
}
