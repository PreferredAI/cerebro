package ai.preferred.cerebro.index.search.processor;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.ScoreDoc;

import ai.preferred.cerebro.index.exception.UnsupportedDataType;
import ai.preferred.cerebro.index.request.QueryRequest;
import ai.preferred.cerebro.index.response.QueryResponse;
import ai.preferred.cerebro.index.search.structure.VersatileSearcher;
import ai.preferred.cerebro.index.utils.IndexConst;

/**
 *
 * This class process a {@link QueryRequest} into suitable
 * query for a {@link VersatileSearcher} then pass it to carry
 * out the search and return result.
 *
 * @author hpminh@apcs.vn
 */
public class LuQueryProcessor implements QueryProcessor {
    /**
     * Process both type of query text and vector.
     *
     * @param searcher Searcher loaded with the index to carry search on.
     * @param qRequest Request carrying the query to be executed.
     * @return The wrapper object containing the query's result;
     * @throws Exception
     */
    @Override
    public QueryResponse process(VersatileSearcher searcher, QueryRequest qRequest) throws Exception {
        switch (qRequest.getType()){
            case KEYWORD:
                return new QueryResponse<ScoreDoc>(processKeyword(searcher, qRequest.getQueryData(), qRequest.getTopK()));
            case VECTOR:
                return new QueryResponse<ScoreDoc>(searcher.queryVector((double[])qRequest.getQueryData(), qRequest.getTopK()));
            default:
                throw new UnsupportedDataType();
        }
    }

    /**
     * Handle the case when we want to query a field with a custom name,
     * not the default {@link IndexConst#CONTENTS}.
     *
     * @param searcher
     * @param queryData
     * @param topK
     * @return
     * @throws Exception
     *
     *
     */
    private ScoreDoc[] processKeyword(VersatileSearcher searcher, Object queryData, int topK) throws Exception {
        //assume field name is contents
        if (queryData instanceof String){
            return searcher.queryKeyWord(null, (String) queryData, topK);
        }
        //assume [0] is fieldname, [1] is query string
        else if(queryData instanceof String[]){
            String[] fieldnameAndQuery = (String[])queryData;
            QueryParser parser = new QueryParser(fieldnameAndQuery[0], new StandardAnalyzer());
            return searcher.queryKeyWord(parser, fieldnameAndQuery[1], topK);
        }
        return null;
    }
}
