package ai.preferred.cerebro.index.demo;


import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.search.ScoreDoc;
import ai.preferred.cerebro.index.builder.ExtFilter;
import ai.preferred.cerebro.index.builder.LuIndexWriter;
import ai.preferred.cerebro.index.request.LoadSearcherRequest;
import ai.preferred.cerebro.index.request.QueryRequest;
import ai.preferred.cerebro.index.response.QueryResponse;
import ai.preferred.cerebro.index.search.LuIndexSearcher;
import ai.preferred.cerebro.index.utils.IndexConst;
import ai.preferred.cerebro.index.utils.IndexUtils;
import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TestFullFlow {

    //Change the DataDirs to where you store the downloaded data in README file.
    //Make an empty folder where you want to store the index file and modify
    //the IndexDirs to these folder directory.
    //Text and Vec data can share the same Index directory
    //but it is preferable that you seperate the two
    static String textIndexDir = "E:\\Lucene\\Index";;
    static String vecIndexDir = TestConst.DIM_50_PATH + "index_32bits";
    static String textDataDir = "E:\\Lucene\\imdb_data";
    static String vecDataDir = TestConst.DIM_50_PATH + "itemVec_10M.o";

    @org.junit.jupiter.api.Test
    public void createTextIndex() throws Exception {
        //fileExt signify what file extension to read and index
        String fileExt = ".txt";
        LuIndexWriter writer = new LuIndexWriter(textIndexDir, null) {
            @Override
            public void indexFile(File file) throws IOException {
                //build index based on content of the file
                //class FileReader will return the whole content
                //of the file at a lower level Lucene API
                Field contentField = new TextField(IndexConst.CONTENTS,
                        new FileReader(file));
                //store file path
                Field filePathField = new StoredField(TestConst.FilePathField,
                        file.getCanonicalPath());
                //we add all these fields to a document through
                //an intance of PersonalizedDocFactory
                try {
                    docFactory.createTextDoc(writer.numDocs(), contentField, filePathField);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //when using DocFactory always call getDoc()
                //after calling createPersonalizedDoc() to free up the pointer
                writer.addDocument(docFactory.getDoc());
            }

            @Override
            public void indexLatentVectors(Object... params) throws Exception {
            }

            @Override
            public void indexKeyWords(Object... params) throws Exception {
                String fileDir = (String) params[0];
                ExtFilter filter = new ExtFilter((String) params[1]);
                createIndexFromDir(fileDir, filter);
            }
        };
        Assertions.assertNotNull(writer);
        writer.indexKeyWords(textDataDir, fileExt);
    }
    @org.junit.jupiter.api.Test
    public void demoSearchText() throws Exception {
        //main query
        String queryText = "Lord of the Rings";

        //get a searcher through a LoadSearcherRequest
        LoadSearcherRequest loadSearcher = new LoadSearcherRequest(textIndexDir, null, false);
        LuIndexSearcher searcher = (LuIndexSearcher) loadSearcher.getSearcher();
        Assertions.assertNotNull(searcher);
        //carry out searching
        QueryRequest request = new QueryRequest(queryText, QueryRequest.QueryType.KEYWORD, 20);

        QueryResponse<ScoreDoc> res = searcher.query(request);
        Assertions.assertNotNull(res);
        for(ScoreDoc scoreDoc : res.getRankedItemList()) {
            Document doc = searcher.doc(scoreDoc.doc);
            System.out.println("File: " + doc.get(TestConst.FilePathField) + "; DocID:" + scoreDoc.doc);
        }
    }
    @org.junit.jupiter.api.Test
    public void createVecIndex() throws Exception {
        String hashTablePath = TestConst.DIM_50_PATH + "\\splitVec_32bits\\splitVec.o";
        LuIndexWriter writer = new LuIndexWriter(vecIndexDir, hashTablePath) {
            @Override
            public void indexFile(File file) throws IOException {

            }

            @Override
            public void indexLatentVectors(Object... params) throws Exception {
                double[][] itemVec = IndexUtils.readVectors( (String) params[0]);
                createIndexFromVecData(itemVec);
            }

            @Override
            public void indexKeyWords(Object... params) throws Exception {

            }
        };
        writer.indexLatentVectors(vecDataDir);
    }

    @org.junit.jupiter.api.Test
    public void demoSearchVec() throws Exception {
        //get a searcher through a LoadSearcherRequest
        String hashTablePath = TestConst.DIM_50_PATH + "splitVec_32bits\\splitVec.o";
        LoadSearcherRequest loadSearcher = new LoadSearcherRequest(vecIndexDir, hashTablePath, false);
        LuIndexSearcher searcher = (LuIndexSearcher) loadSearcher.getSearcher();
        //load query set
        HashMap<double[], ArrayList<Integer>> queryAndTopK = null;
        try {
            queryAndTopK = IndexUtils.readQueryAndTopK(TestConst.DIM_50_PATH + "query_top20_10M.o");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        //carry out search and measure time
        double totalTime = 0;
        double totalHit = 0;
        int totalMiss = 0;
        Iterator it = queryAndTopK.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry entry = (Map.Entry) it.next();
            //get query vector
            double[] query = (double[]) entry.getKey();
            //creating request
            QueryRequest request = new QueryRequest(query, QueryRequest.QueryType.VECTOR, 20);
            //true top 20 vec
            ArrayList<Integer> setBrute = (ArrayList<Integer>) entry.getValue();

            //start querying
            long startTime = System.currentTimeMillis();
            QueryResponse<ScoreDoc> res = searcher.query(request);
            long endSearchTime = System.currentTimeMillis();

            //print search time and overlap with true top K
            if(res.getRankedItemList() != null){
                System.out.println("Top-20 query time: " +(endSearchTime-startTime)+" ms");
                totalTime += endSearchTime - startTime;
                ArrayList<Integer> setHash = new ArrayList<>();
                for(int i = 0; i < 20; i++){
                    int id = res.getRankedItemList()[i].doc;
                    setHash.add(id);
                }
                if(setHash.retainAll(setBrute)){
                    totalHit += setHash.size();
                    System.out.println("Overlapp between brute and and hash (over top 20) is : " + setHash.size());
                }
                System.out.println(" ");
            }
            else totalMiss++;
        }
        System.out.println("Num of misses : " + totalMiss);
        System.out.println("Average search time :" + totalTime/(1000 - totalMiss));
        System.out.println("Average overlap :" + totalHit/(1000 - totalMiss));
    }
}
