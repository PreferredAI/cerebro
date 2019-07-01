package ai.preferred.cerebro.index.demo;


import ai.preferred.cerebro.index.builder.PersonalizedDocFactory;
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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestFullFlow {
    @BeforeAll
    public void createIndex() throws Exception {
        //fileExt signify what file extension to read and index
        String fileExt = ".txt";
        TestIndexWriter writer = new TestIndexWriter("", null);
        Assertions.assertNotNull(writer);
        writer.setDocFactory(new PersonalizedDocFactory(TestConst.hashingVecs));
        writer.indexTest(TestConst.text1, TestConst.vec1);
        writer.indexTest(TestConst.text2, TestConst.vec2);
        writer.indexTest(TestConst.text3, TestConst.vec3);
        writer.close();

    }
    @org.junit.jupiter.api.Test
    public void demoSearch() throws Exception {
        //main query
        String queryText = "Command and City Lights";
        FSDirectory indexDirectory = FSDirectory.open(Paths.get(""));
        TestIndexSearcher searcher =  new TestIndexSearcher(DirectoryReader.open(indexDirectory), null);
        Assertions.assertNotNull(searcher);
        searcher.setLSH(TestConst.hashingVecs);
        //carry out searching
        QueryRequest requestText = new QueryRequest(queryText, QueryRequest.QueryType.KEYWORD, 1);
        QueryResponse<ScoreDoc> resText = searcher.query(requestText);
        Assertions.assertNotNull(resText);

        QueryRequest requestVec = new QueryRequest(TestConst.vec1, QueryRequest.QueryType.VECTOR, 1);
        QueryResponse<ScoreDoc> resVec = searcher.query(requestVec);
        Assertions.assertNotNull(resVec);

    }

//    @AfterAll
//    public void tearDown(){
//        HashSet<String> fileSet = new HashSet<String>();
//        fileSet.add("_0.cfe");
//        fileSet.add("_0.si");
//        fileSet.add("segments_1");
//        fileSet.add("write.lock");
//        File[] files = new File("").listFiles();
//
//        for (File file : files) {
//            if(fileSet.contains(file.getName()))
//                file.delete();
//        }
//
//    }



}
