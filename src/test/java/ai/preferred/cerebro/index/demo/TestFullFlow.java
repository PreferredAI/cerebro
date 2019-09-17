package ai.preferred.cerebro.index.demo;


import ai.preferred.cerebro.core.utils.CommandOptions;
import ai.preferred.cerebro.index.builder.PersonalizedDocFactory;
import ai.preferred.cerebro.index.exception.SameNameException;
import ai.preferred.cerebro.index.exception.UnsupportedDataType;
import ai.preferred.cerebro.index.search.FlipBitSearcher;
import ai.preferred.cerebro.index.store.DoubleStoredField;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.ScoreDoc;
import ai.preferred.cerebro.index.builder.ExtFilter;
import ai.preferred.cerebro.index.builder.LuIndexWriter;
import ai.preferred.cerebro.index.request.LoadSearcherRequest;
import ai.preferred.cerebro.index.request.QueryRequest;
import ai.preferred.cerebro.index.response.QueryResponse;
import ai.preferred.cerebro.index.search.LuIndexSearcher;
import ai.preferred.cerebro.index.utils.IndexConst;
import ai.preferred.cerebro.index.utils.IndexUtils;
import org.apache.lucene.store.FSDirectory;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestFullFlow {
    @BeforeAll
    public void createIndex() throws Exception {
        //fileExt signify what file extension to read and index
        String fileExt = ".txt";
        TestIndexWriter writer = new TestIndexWriter("", null);
        writer.setMaxBufferRAMSize(100);
        writer.setMaxBufferDocNum(3);
        Assertions.assertNotNull(writer);
        writer.setDocFactory(new PersonalizedDocFactory(TestConst.hashingVecs));
        writer.indexTest(TestConst.text1, TestConst.vec1);
        writer.indexTest(TestConst.text2, TestConst.vec2);
        writer.indexTest(TestConst.text3, TestConst.vec3);
        writer.close();

    }
    @Test
    public void delete() throws IOException {
        TestIndexWriter writer = new TestIndexWriter("", null);
        try {
            writer.deleteByID(2);
        } catch (UnsupportedDataType unsupportedDataType) {
            unsupportedDataType.printStackTrace();
        }
    }

    @Test
    public void demoSearch() throws Exception {
        //main query
        String queryText = "Command and City Lights";
        FSDirectory indexDirectory = FSDirectory.open(Paths.get(""));
        LuIndexSearcher searcher =  new LuIndexSearcher(DirectoryReader.open(indexDirectory), null);
        Assertions.assertNotNull(searcher);
        searcher.setLSH(TestConst.hashingVecs);
        //carry out searching
        QueryRequest requestText = new QueryRequest(queryText, QueryRequest.QueryType.KEYWORD, 1);
        QueryResponse<ScoreDoc> resText = searcher.query(requestText);
        Assertions.assertNotNull(resText);

        QueryRequest requestVec = new QueryRequest(TestConst.vec1, QueryRequest.QueryType.VECTOR, 1);
        QueryResponse<ScoreDoc> resVec = searcher.query(requestVec);
        Assertions.assertNotNull(resVec);

        //flip bit searcher
        FlipBitSearcher flipBitSearcher =  new FlipBitSearcher(DirectoryReader.open(indexDirectory), null);
        Assertions.assertNotNull(searcher);
        flipBitSearcher.setLSH(TestConst.hashingVecs);
        //carry out searching
        resText = flipBitSearcher.query(requestText);
        Assertions.assertNotNull(resText);

        resVec = flipBitSearcher.query(requestVec);
        Assertions.assertNotNull(resVec);


    }
    @Test
    public void testField(){
        double e = 1.2342;
        DoubleStoredField doubleStoredField = new DoubleStoredField(e);
        e = DoubleStoredField.bytesToDouble(doubleStoredField.binaryValue().bytes);
    }

}
