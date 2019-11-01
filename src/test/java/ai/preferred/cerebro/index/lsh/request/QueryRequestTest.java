package ai.preferred.cerebro.index.lsh.request;

import ai.preferred.cerebro.index.demo.TestConst;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class QueryRequestTest {
    @Test
    void testValidRequest(){
        Assertions.assertDoesNotThrow(()->{
            QueryRequest qRequest = new QueryRequest(new double[]{1.2, 2.3}, QueryRequest.QueryType.VECTOR, 20);
            qRequest.getTopK();
            qRequest.getType();
            qRequest.getQueryData();
        });
    }

    @Test
    void testErrorRequest(){
        Assertions.assertThrows(AssertionError.class, ()->{
            QueryRequest qRequest = new QueryRequest(new double[]{1.2, 2.3}, QueryRequest.QueryType.KEYWORD, 20);
        });
        Assertions.assertDoesNotThrow(()->{
            QueryRequest qRequest = new QueryRequest(TestConst.vec1, QueryRequest.QueryType.VECTOR, 20);
            qRequest.getTopK();
            qRequest.getType();
            qRequest.getQueryData();
        });
    }

}