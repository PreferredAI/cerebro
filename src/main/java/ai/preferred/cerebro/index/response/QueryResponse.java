package ai.preferred.cerebro.index.response;

import ai.preferred.cerebro.index.search.Searcher;


/**
 * A class to wrap up the result of any query pass to a
 * {@link Searcher}.
 * This is to handle communication between the index and other unreleased
 * Cerebro components. You can Get the result of your queries directly from
 * your searcher if you want to.
 *
 * @author hpminh@apcs.vn
 */
public class QueryResponse<T> {
    /**
     *
     * @param rankedItemList
     */
    public QueryResponse(T[] rankedItemList){
        this.rankedItemList = rankedItemList;
    }

    /**
     *
     * @return
     */
    public T[] getRankedItemList() {
        return rankedItemList;
    }

    private T[] rankedItemList;
}
