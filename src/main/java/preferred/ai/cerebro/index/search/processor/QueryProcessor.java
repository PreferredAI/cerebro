package preferred.ai.cerebro.index.search.processor;

import preferred.ai.cerebro.index.request.QueryRequest;
import preferred.ai.cerebro.index.response.QueryResponse;
import preferred.ai.cerebro.index.search.structure.VersatileSearcher;

/**
 * An interface to enforce the functionality of any type
 * of Processor used to handled queries.
 */
public interface QueryProcessor {
    public QueryResponse process(VersatileSearcher searcher, QueryRequest qRequest) throws Exception;
}
