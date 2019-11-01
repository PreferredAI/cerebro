package ai.preferred.cerebro.index.lsh.request;


import ai.preferred.cerebro.core.entity.DenseVector;
import ai.preferred.cerebro.index.lsh.search.Searcher;

/**
 *
 * This class is a search request to be passed into
 * {@link Searcher}
 * and get the result. The extra steps are to handle communication
 * between the index and other unreleased Cerebro components. Create your
 * queries and pass them directly via your searcher if you want to.
 *
 * @author hpminh@apcs.vn
 */
@Deprecated
/*
    This is garbage API. To be changes or removed soon.
    Call keywordSearch() or similarSearch() from searcher directly
 */
public class QueryRequest {
    //private int modelId;
    private Object queryData;
    private int topK;

    public QueryType getType() {
        return type;
    }


    private QueryType type;

    /**
     * Decide how {@link QueryRequest#queryData} will be processed
     * and the kind of functionality to support.
     */
    public enum QueryType{
        KEYWORD,
        VECTOR
    }

    /**
     * Create a query with type info, top result size and data to be queried.
     *
     * @param queryData data to be queried.
     * @param type query's type.
     * @param topK top result size.
     */
    public QueryRequest(/*int modelId,*/ Object queryData, QueryType type ,int topK) {
        //check type compatibility
        assert checkType(queryData, type);
        //this.modelId = modelId;
        this.queryData = queryData;
        this.topK  = topK;
        this.type = type;
    }

    /**
     * Get this request query's data.
     * @return this request query's data.
     */
    public Object getQueryData() {
        return queryData;
    }

    /*public int getModelId() { return modelId; }
    public void setModelId(int modelId) { this.modelId = modelId; }*/

    /**
     * Get this request top result size.
     * @return The result size.
     */
    public int getTopK() { return topK; }

    /**
     * Check the consistency between the type and the actual query data passed in.
     *
     * @param queryData query data
     * @param type query type
     * @return false if the type and the actual data don't match. True otherwise.
     */
    private static boolean checkType(Object queryData, QueryType type){
        switch (type){
            case VECTOR:
                return queryData instanceof DenseVector || queryData instanceof double[];
            case KEYWORD:
                return queryData instanceof String || queryData instanceof String[];
            default:
                return false;
        }
    }
}
