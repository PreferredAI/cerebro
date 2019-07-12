package ai.preferred.cerebro.index.main;


import org.apache.logging.log4j.core.tools.picocli.CommandLine;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.search.ScoreDoc;

import ai.preferred.cerebro.core.utils.CommandOptions;
import ai.preferred.cerebro.index.builder.ExtFilter;
import ai.preferred.cerebro.index.builder.LuIndexWriter;
import ai.preferred.cerebro.index.exception.DocNotClearedException;
import ai.preferred.cerebro.index.exception.SameNameException;
import ai.preferred.cerebro.index.request.LoadSearcherRequest;
import ai.preferred.cerebro.index.request.QueryRequest;
import ai.preferred.cerebro.index.response.QueryResponse;
import ai.preferred.cerebro.index.search.LuIndexSearcher;
import ai.preferred.cerebro.index.utils.IndexConst;
import ai.preferred.cerebro.index.utils.IndexUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Example class to demostrate Cerebro
 * functionality via command line interface.
 *
 * Read the file README first to get the data
 * necessary to carry out these examples
 */
public class Main {

    public static void main(String[] args) throws Exception{
        CommandOptions cmdOptions = new CommandOptions();
        cmdOptions.addOption("op", "Specify which operation you want to run:\n" +
                "\t - 1 build index for text files.\n" +
                "\t - 2 build index for vector search from an object file.\n" +
                "\t - 3 search keyword on a built index.\n" +
                "\t - 4 search vector on a built index.", 0);
        cmdOptions.addOption("idx", "Specify the folder where the index is/will be located\n", "");
        cmdOptions.addOption("data", "Specify the folder where the text data is located\n", "");
        cmdOptions.addOption("dataV", "Specify the file object containing the data vectors\n", "");
        cmdOptions.addOption("hsh", "Specify the file object containing the hashing vectors\n", "");
        cmdOptions.addOption("q", "Enter your text query\n", "");
        cmdOptions.addOption("qV", "Specify the file object containing the query vectors\n", "");
        cmdOptions.parse(args);
        int operationcode    = cmdOptions.getIntegerOption("op");
        switch (operationcode){
            case 1:
                createTextIndex(cmdOptions);
                break;
            case 2:
                createVecIndex(cmdOptions);
                break;
            case 3:
                demoSearchText(cmdOptions);
                break;
            case 4:
                demoSearchVec(cmdOptions);
                break;
            default:
                System.out.println("Not supported operation code, exiting \n");

        }
    }
    public static void createTextIndex(CommandOptions commandOptions) throws Exception {
        //fileExt signify what file extension to read and index
        String fileExt = ".txt";

        String textIndexDir =commandOptions.getStringOption("idx");
        String textDataDir = commandOptions.getStringOption("data");
        if(textDataDir.equals("") || textIndexDir.equals(""))
            throw new Exception("Not enough param provided");

        LuIndexWriter writer = new LuIndexWriter(textIndexDir, null) {
            @Override
            public void indexFile(File file) throws IOException {
                //build index based on content of the file
                //class FileReader will return the whole content
                //of the file at a lower level Lucene API
                Field contentField = new TextField(IndexConst.CONTENTS,
                        new FileReader(file));
                //store file path
                Field filePathField = new StoredField(IndexConst.FilePathField,
                        file.getCanonicalPath());
                //we add all these fields to a document through
                //an intance of PersonalizedDocFactory
                try {
                    docFactory.createTextDoc(writer.numDocs(), contentField, filePathField);
                } catch (SameNameException e) {
                    e.printStackTrace();
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

        System.out.println("\n\nBuilding index, plz wait\n");

        //build index
        writer.indexKeyWords(textDataDir, fileExt);
        System.out.println("Build index for text successfully\n");
    }

    public static void createVecIndex(CommandOptions commandOptions) throws Exception {

        String vecIndexDir = commandOptions.getStringOption("idx");

        String vecDataDir = commandOptions.getStringOption("data");



        String hashTablePath = commandOptions.getStringOption("hsh");

        if(vecIndexDir.equals("") || vecDataDir.equals("") || hashTablePath.equals(""))
            throw new Exception("Not enough param provided");

        //Create (main) index writer from the provided directory.
        LuIndexWriter writer = new LuIndexWriter(vecIndexDir, hashTablePath) {
            @Override
            public void indexFile(File file) throws IOException {

            }

            @Override
            public void indexLatentVectors(Object... params) throws Exception {
                double[][] itemVec = IndexUtils.readVectors( (String)params[0]);
                createIndexFromVecData(itemVec);
            }

            @Override
            public void indexKeyWords(Object... params) throws Exception {

            }
        };

        System.out.println("\n\nBuilding index, plz wait\n");

        writer.indexLatentVectors(vecDataDir);
        System.out.println("Build index for vector successfully\n");
    }

    public static void demoSearchText(CommandOptions commandOptions) throws Exception {
        String textIndexDir = commandOptions.getStringOption("idx");
        String queryText = commandOptions.getStringOption("q");

        if(textIndexDir.equals("") || queryText.equals(""))
            throw new Exception("Not enough param provided");


        LoadSearcherRequest loadSearcher = new LoadSearcherRequest(textIndexDir, null, false);
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



    public static void demoSearchVec(CommandOptions commandOptions) throws Exception {

        String vecIndexDir = commandOptions.getStringOption("idx");
        String hashTablePath = commandOptions.getStringOption("hsh");
        String queryObjectPath = commandOptions.getStringOption("qV");

        if(vecIndexDir.equals("") || queryObjectPath.equals("") || hashTablePath.equals(""))
            throw new Exception("Not enough param provided");

        LoadSearcherRequest loadSearcher = new LoadSearcherRequest(vecIndexDir, hashTablePath, false);
        LuIndexSearcher searcher = (LuIndexSearcher) loadSearcher.getSearcher();




        //load query set
        HashMap<double[], ArrayList<Integer>> queryAndTopK = null;
        try {
            queryAndTopK = IndexUtils.readQueryAndTopK(queryObjectPath);
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
                for(int i = 0; i < res.getRankedItemList().length; i++){
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
