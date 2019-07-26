package ai.preferred.cerebro.index.demo;


import ai.preferred.cerebro.core.utils.CommandOptions;
import ai.preferred.cerebro.index.builder.PersonalizedDocFactory;
import ai.preferred.cerebro.index.exception.SameNameException;
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

    //@Test
    public void createIndexTest() throws Exception {
        //fileExt signify what file extension to read and index
        String fileExt = ".txt";
        ExtFilter filter = new ExtFilter(fileExt);

        String indexDir = "E:\\index";
        String dataDir = "E:\\data\\imdb_data";
        String hashTablePath = "E:\\data\\splitVec.o";
        if(dataDir.equals("") || indexDir.equals("") || hashTablePath.equals(""))
            throw new Exception("Not enough param provided");

        LuIndexWriter writer = new LuIndexWriter(indexDir, hashTablePath) {

            @Override
            public void indexFile(File file) throws IOException {
                try{
                    BufferedReader br = new BufferedReader(new FileReader(file));


                    Field filePathField = new StoredField(IndexConst.FilePathField, file.getCanonicalPath());
                    //first line is text
                    String line = br.readLine();
                    Field contentField = new TextField(IndexConst.CONTENTS, line, Field.Store.NO);

                    //second line is vector
                    line = br.readLine();
                    line = line.substring(1, line.length() - 1);
                    String [] doubles = line.split(", ");
                    double[] vec = Arrays.stream(doubles)
                            .mapToDouble(Double::parseDouble)
                            .toArray();

                    docFactory.createPersonalizedDoc(writer.numDocs(), vec);
                    docFactory.addField(filePathField, contentField);
                    //when using DocFactory always call getDoc()
                    //after calling createPersonalizedDoc() to free up the pointer
                    writer.addDocument(docFactory.getDoc());

                }
                catch (FileNotFoundException e){
                    e.printStackTrace();
                }
                catch (IOException e){
                    e.printStackTrace();
                }
                catch (SameNameException e) {
                    e.printStackTrace();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

        };
        //choose the optimal number of segment for the index
        writer.setMaxBufferDocNum((50_000/Runtime.getRuntime().availableProcessors()) + 1);

        System.out.println("\n\nBuilding index, plz wait\n");

        //build index
        writer.createIndexFromDir(dataDir, filter);
        System.out.println("Build index successfully\n");
    }


    //@Test
    public void demoSearchText() throws Exception {
        String indexDir = "E:\\index";
        String queryText = "Cavalry Charge";

        if(indexDir.equals("") || queryText.equals(""))
            throw new Exception("Not enough param provided");


        LoadSearcherRequest loadSearcher = new LoadSearcherRequest(indexDir, null, false, true);
        LuIndexSearcher searcher = (LuIndexSearcher) loadSearcher.getSearcher();

        //carry out searching
        //the default is 20 top
        //results
        QueryRequest request = new QueryRequest(queryText, QueryRequest.QueryType.KEYWORD, 20);
        QueryResponse<ScoreDoc> res = searcher.query(request);
        //print out results
        for(ScoreDoc scoreDoc : res.getRankedItemList()) {
            Document doc = searcher.doc(scoreDoc.doc);
            System.out.println("File: " + doc.get(IndexConst.FilePathField) + "; DocID:" + scoreDoc.doc);
        }
    }

    public static double[][] extract(String dir) throws IOException {
        ExtFilter filter = new ExtFilter("txt");
        File[] files = new File(dir).listFiles();
        double[][] vecs = new double[files.length][];
        for (int i = 0; i < files.length; i++) {
            BufferedReader br = new BufferedReader(new FileReader(files[i]));
            String line = br.readLine();
            Field contentField = new TextField(IndexConst.CONTENTS, line, Field.Store.NO);

            //second line is vector
            line = br.readLine();
            line = line.substring(1, line.length() - 1);
            String [] doubles = line.split(", ");
            vecs[i] = Arrays.stream(doubles)
                    .mapToDouble(Double::parseDouble)
                    .toArray();
        }
        return vecs;
    }

    public void createQuery(){
        double[] vec = IndexUtils.randomizeQueryVector(50);
    }
}
