package ai.preferred.cerebro.index.main;


import ai.preferred.cerebro.index.search.LSHIndexSearcher;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.search.ScoreDoc;

import ai.preferred.cerebro.core.utils.CommandOptions;
import ai.preferred.cerebro.index.builder.ExtFilter;
import ai.preferred.cerebro.index.builder.LSHIndexWriter;
import ai.preferred.cerebro.index.exception.SameNameException;
import ai.preferred.cerebro.index.request.LoadSearcherRequest;
import ai.preferred.cerebro.index.request.QueryRequest;
import ai.preferred.cerebro.index.response.QueryResponse;
import ai.preferred.cerebro.index.utils.IndexConst;
import ai.preferred.cerebro.index.utils.IndexUtils;

import java.io.*;
import java.util.*;

/**
 * Example class to demonstrate Cerebro
 * functionality via command line interface.
 *
 * Read the file README first to get the data
 * necessary to carry out these examples
 */
public class Main {
    /*
    public static void main(String[] args) throws Exception{
        CommandOptions cmdOptions = new CommandOptions();
        cmdOptions.addOption("op", "Specify which operation you want to run:\n" +
                "\t - build, build index from data.\n" +
                "\t - sText, search keyword on a built index.\n" +
                "\t - sVec, search vector on a built index.", "");
        cmdOptions.addOption("idx", "Specify the folder where the index is/will be located\n", "");
        cmdOptions.addOption("data", "Specify the folder where the data is located\n", "");
        cmdOptions.addOption("hsh", "Specify the file object containing the hashing vectors\n", "");
        cmdOptions.addOption("q", "Enter your text query\n", "");
        cmdOptions.addOption("qV", "Specify the file object containing the query vectors\n", "");
        cmdOptions.parse(args);
        String option = cmdOptions.getStringOption("op");
        switch (option){
            case "build":
                createIndex(cmdOptions);
                break;
            case "sText":
                demoSearchText(cmdOptions);
                break;
            case "sVec":
                demoSearchVec(cmdOptions);
                break;
            default:
                System.out.println("Not supported operation, exiting \n");

        }
    }

    public static void createIndex(CommandOptions commandOptions) throws Exception {
        //fileExt signify what file extension to read and index
        String fileExt = ".txt";
        ExtFilter filter = new ExtFilter(fileExt);

        String indexDir =commandOptions.getStringOption("idx");
        String dataDir = commandOptions.getStringOption("data");
        String hashTablePath = commandOptions.getStringOption("hsh");
        if(dataDir.equals("") || indexDir.equals("") || hashTablePath.equals(""))
            throw new Exception("Not enough param provided");

        LSHIndexWriter writer = new LSHIndexWriter(indexDir, hashTablePath) {
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

                    docFactory.createPersonalizedDoc(delegate.numDocs(), vec);
                    docFactory.addField(filePathField, contentField);
                    //when using DocFactory always call getDoc()
                    //after calling createPersonalizedDoc() to free up the pointer
                    delegate.addDocument(docFactory.getDoc());

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

    public static void demoSearchText(CommandOptions commandOptions) throws Exception {
        String textIndexDir = commandOptions.getStringOption("idx");
        String queryText = commandOptions.getStringOption("q");

        if(textIndexDir.equals("") || queryText.equals(""))
            throw new Exception("Not enough param provided");


        LoadSearcherRequest loadSearcher = new LoadSearcherRequest(textIndexDir, null, false, true);
        LSHIndexSearcher searcher = (LSHIndexSearcher) loadSearcher.getSearcher();

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
        searcher.close();
    }



    public static void demoSearchVec(CommandOptions commandOptions) throws Exception {

        String vecIndexDir = commandOptions.getStringOption("idx");
        String hashTablePath = commandOptions.getStringOption("hsh");
        String queryObjectPath = commandOptions.getStringOption("qV");

        if(vecIndexDir.equals("") || queryObjectPath.equals("") || hashTablePath.equals(""))
            throw new Exception("Not enough param provided");

        LoadSearcherRequest loadSearcher = new LoadSearcherRequest(vecIndexDir, hashTablePath, false, true);
        LSHIndexSearcher searcher = (LSHIndexSearcher) loadSearcher.getSearcher();


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
                    System.out.println("Overlapp between truth and and result (over top 20) is : " + setHash.size());
                }
                System.out.println(" ");
            }
        }
        System.out.println("Average search time :" + totalTime/queryAndTopK.size());
        System.out.println("Average overlap :" + totalHit/queryAndTopK.size());
        searcher.close();
    }

     */
}
