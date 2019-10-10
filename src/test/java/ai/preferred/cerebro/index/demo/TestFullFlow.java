package ai.preferred.cerebro.index.demo;


import ai.preferred.cerebro.common.ExternalID;
import ai.preferred.cerebro.common.IntID;
import ai.preferred.cerebro.index.builder.LSHIndexWriter;
import ai.preferred.cerebro.index.exception.UnsupportedDataType;
import ai.preferred.cerebro.index.search.FlipBitSearcher;
import ai.preferred.cerebro.index.search.LSHIndexSearcher;
import ai.preferred.cerebro.index.utils.IndexConst;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.FSDirectory;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.Paths;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestFullFlow {

    @BeforeAll
    public void createIndex() throws Exception {
        //fileExt signify what file extension to read and index
        String fileExt = ".txt";
        LSHIndexWriter<double[]> writer = new LSHIndexWriter<double[]>("", TestConst.hashingVecs) {
            @Override
            public void indexFile(File file) throws IOException {

            }
            @Override
            public void indexAsOneDocument(ExternalID ID, double[] personalizedFeatures, String... textualInfo) throws Exception{
                docFactory.createPersonalizedDoc(ID, personalizedFeatures);
                for (String text: textualInfo) {
                    TextField textField = new TextField(IndexConst.CONTENTS, text,Field.Store.NO);
                    docFactory.addField(textField);
                }
                delegate.addDocument(docFactory.getDoc());
            }
        };
        writer.setMaxBufferRAMSize(100);
        writer.setMaxBufferDocNum(3);
        Assertions.assertNotNull(writer);
        writer.indexAsOneDocument(new IntID(1), TestConst.vec1, TestConst.text1);
        writer.indexAsOneDocument(new IntID(2), TestConst.vec1, TestConst.text1);
        writer.indexAsOneDocument(new IntID(3), TestConst.vec1, TestConst.text1);
        writer.close();

    }
    @Test
    public void delete() throws IOException {
        LSHIndexWriter<double[]> writer = new LSHIndexWriter<double[]>("", (String) null) {
            @Override
            public void indexFile(File file) throws IOException {

            }

            @Override
            public void indexAsOneDocument(ExternalID ID, double[] personalizedFeatures, String... textualInfo) throws Exception {

            }
        };
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
        LSHIndexSearcher<double[]> searcher =  new LSHIndexSearcher<>(DirectoryReader.open(indexDirectory), null);
        Assertions.assertNotNull(searcher);
        searcher.setLSH(TestConst.hashingVecs);
        //carry out searching
        //QueryRequest requestText = new QueryRequest(queryText, QueryRequest.QueryType.KEYWORD, 1);
        ScoreDoc[] resText= searcher.keywordSearch(null, queryText, 1);
        Assertions.assertNotNull(resText);

        //QueryRequest requestVec = new QueryRequest(TestConst.vec1, QueryRequest.QueryType.VECTOR, 1);
        //QueryResponse<ScoreDoc> resVec = searcher.query(requestVec);
        ScoreDoc[] resVec = searcher.similaritySearch(TestConst.vec1, 1);
        Assertions.assertNotNull(resVec);

        //flip bit searcher
        FlipBitSearcher<double[]> flipBitSearcher =  new FlipBitSearcher<>(DirectoryReader.open(indexDirectory), null);
        Assertions.assertNotNull(searcher);
        flipBitSearcher.setLSH(TestConst.hashingVecs);
        //carry out searching
        resText = flipBitSearcher.keywordSearch(null, queryText, 1);
        Assertions.assertNotNull(resText);

        resVec = flipBitSearcher.similaritySearch(TestConst.vec1, 1);
        Assertions.assertNotNull(resVec);
    }

}
