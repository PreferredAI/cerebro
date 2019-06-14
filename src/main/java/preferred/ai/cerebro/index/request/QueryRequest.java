package preferred.ai.cerebro.index.request;


/**
 * This class is a search request to be passed into
 * {@link preferred.ai.cerebro.index.search.processor.QueryProcessor}
 * and get the result. The extra steps are to handle communication
 * between the index and other unreleased Cerebro components. Create your
 * queries and pass them directly via your searcher if you want to.
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

    public QueryRequest(/*int modelId,*/ Object queryData, QueryType type ,int topK) {
        //check type compatibility
        assert checkType(queryData, type);
        //this.modelId = modelId;
        this.queryData = queryData;
        this.topK  = topK;
        this.type = type;
    }


    public Object getQueryData() {
        return queryData;
    }

    public void setQueryData(Object queryData) {
        //check type compatibility
        assert checkType(queryData, type);
        this.queryData = queryData;
    }

    /*public int getModelId() { return modelId; }
    public void setModelId(int modelId) { this.modelId = modelId; }*/

    public int getTopK() { return topK; }

    private static boolean checkType(Object queryData, QueryType type){
        switch (type){
            case VECTOR:
                return queryData instanceof double[];
            case KEYWORD:
                return queryData instanceof String || queryData instanceof String[];
            default:
                return false;
        }
    }
}
