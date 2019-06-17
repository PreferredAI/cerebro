package ai.preferred.cerebro.index.search.processor;

import ai.preferred.cerebro.index.request.QueryRequest;
import ai.preferred.cerebro.index.response.QueryResponse;
import ai.preferred.cerebro.index.search.structure.VersatileSearcher;

/**
 *
 * An interface to enforce the functionality of any type
 * of Processor used to handled queries.
 *
 * @author hpminh@apcs.vn
 */
public interface QueryProcessor {
    public QueryResponse process(VersatileSearcher searcher, QueryRequest qRequest) throws Exception;
}
