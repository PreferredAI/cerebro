package ai.preferred.cerebro.index.main;


import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.search.ScoreDoc;
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
import ai.preferred.cerebro.core.util.CommandOptions;

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
    byte q = (byte) 0xff;
    static Scanner scanner = new Scanner(System.in);
    public static void main(String[] args) throws Exception{
        CommandOptions cmdOptions = new CommandOptions();
        cmdOptions.addOption("op", "Specify which operation you want to run:\n" +
                "\t - 1 build index for text files.\n" +
                "\t - 2 build index for vector search from an object file.\n" +
                "\t - 3 search keyword on a built index.\n" +
                "\t - 4 search vector on a built index.", 0);
        cmdOptions.parse(args);
        int operationcode    = cmdOptions.getIntegerOption("op");
        switch (operationcode){
            case 1:
                createTextIndex();
                break;
            case 2:
                createVecIndex();
                break;
            case 3:
                demoSearchText();
                break;
            case 4:
                demoSearchVec();
                break;
            default:
                System.out.println("Not supported operation code, exiting \n");

        }
    }
    public static void createTextIndex() throws Exception {
        //fileExt signify what file extension to read and index
        String fileExt = ".txt";

        //prompt to enter path to (empty and created) index folder
        System.out.println("Plz enter the path to the folder where you want to put the index:\n");

        //read from cmd ln input
        String textIndexDir = scanner.nextLine();
        /**
         * Create (main) index writer from the provided directory.
         * If provided with the param splitVecPath (left null here)
         * a LuIndexWriter can index both text objects and latent vectors.\n
         * You should modify the way how {@link LuIndexWriter#indexFile(File)},
         * {@link LuIndexWriter#indexLatentVectors(Object...)} and
         * {@link LuIndexWriter#indexKeyWords(Object...)} is implemented here to
         * suit your own needs better. These functions are made abstract for the
         * purpose of being customizable.
         */
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

        //prompt to enter path to the data folder
        System.out.println("Plz enter the path to the folder where you put the data to be indexed:\n");
        String textDataDir = scanner.nextLine();

        //build index
        writer.indexKeyWords(textDataDir, fileExt);
        System.out.println("Build index for text successfully\n");
    }

    public static void createVecIndex() throws Exception {
        //prompt to enter path to (empty and created) index folder
        System.out.println("Plz enter the path to the folder where you want to put the index:\n");

        //read from cmd ln input
        String vecIndexDir = scanner.nextLine();

        //prompt to enter path to the data folder
        System.out.println("Plz enter the path to the file object containing the data to be indexed:\n");
        String vecDataDir = scanner.nextLine();

        /**
         * Prompt to enter path to the file object
         * that contains to hashing vectors.
         * With indexing vectors you have to provide
         * a set of predefined vectors at act as hashing
         * function.
         * Note textIndexDir and textDataDir you only
         * need provide path to the folder only but this
         * requires path to a specific file.
         *
         * Note {@link LuIndexWriter} use the function
         * {@link IndexUtils#readVectors(String)}
         * to load objects containing a set of vectors.
         * So if you want to save a set of hashing vector
         * to hard disk you should use
         * {@link IndexUtils#saveVectors(double[][], String)}
         * for the sake of compatibility.
         */
        System.out.println("Plz enter the path to the folder where you put the file object that " +
                "contains hashing vectors:\n");
        String hashTablePath = scanner.nextLine();

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

        //build index
        writer.indexLatentVectors(vecDataDir);
        System.out.println("Build index for vector successfully\n");
    }

    public static void demoSearchText() throws Exception {
        //prompt to enter path to (built) index folder
        System.out.println("Plz enter the path to the folder where you put the index files:\n");

        //read from cmd ln input
        String textIndexDir = scanner.nextLine();

        //prompt to enter the query string
        System.out.println("Plz enter your query:\n");
        //main query
        String queryText = "Lord of the Rings";
        queryText = scanner.nextLine();

        /**
         * Get a searcher through a LoadSearcherRequest.\n
         *
         * Note, the same thing is true with
         * {@link preferred.ai.cerebro.index.search.structure.VersatileSearcher}
         * as it is with {@link LuIndexWriter} - when dealing with text only it's
         * fine not specifying a path to the hashing vectors object, but if you
         * expect the Writer and Searcher to also support vector indexing and searching,
         * you must always provide a set of hashing vectors.
         */
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



    public static void demoSearchVec() throws Exception {
        //prompt to enter path to (built) index folder
        System.out.println("Plz enter the path to the folder where you put the index files:\n");

        //read from cmd ln input
        String vecIndexDir = scanner.nextLine();
        System.out.println("Plz enter the path to the folder where you put the file object that " +
                "contains hashing vectors:\n");
        String hashTablePath = scanner.nextLine();

        //Get a searcher through a LoadSearcherRequest.
        LoadSearcherRequest loadSearcher = new LoadSearcherRequest(vecIndexDir, hashTablePath, false);
        LuIndexSearcher searcher = (LuIndexSearcher) loadSearcher.getSearcher();

        //prompt to enter path to query vectors file object
        System.out.println("Plz enter the path to the query vectors file object:\n");
        String queryObjectPath = scanner.nextLine();

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
