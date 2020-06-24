package ai.preferred.cerebro.webservice;

import ai.preferred.cerebro.index.hnsw.HnswManager;
import ai.preferred.cerebro.index.hnsw.searcher.HnswIndexSearcher;
import ai.preferred.cerebro.index.ids.StringID;
import ai.preferred.cerebro.webservice.models.Items;
import ai.preferred.cerebro.webservice.models.Users;
import ai.preferred.cerebro.webservice.reponses.ListID;
import ai.preferred.cerebro.webservice.repositories.ItemsRepository;
import ai.preferred.cerebro.webservice.repositories.RatingsRespository;
import ai.preferred.cerebro.webservice.repositories.UsersRespository;
import ai.preferred.cerebro.webservice.requests.TextQuery;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author hpminh@apcs.vn
 */

@RestController
@RequestMapping("/search")
public class RecomController {

    @Autowired
    UsersRespository usersRepository;
    @Autowired
    ItemsRepository itemsRepository;
    @Autowired
    RatingsRespository ratingsRespository;

    public void setTextSearch(IndexSearcher textSearch) {
        this.textSearch = textSearch;
        System.out.println("Switch new text searcher");
    }

    HnswIndexSearcher<double[]> searcher;
    private QueryParser defaultParser;
    IndexSearcher textSearch;
    private final Object dummyLock;
    int embeddingSize;
    int topK;
    String cornacURL;
    RecomController() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Properties properties = new Properties();
        try (InputStream resourceStream = loader.getResourceAsStream("application.properties")) {
            properties.load(resourceStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //checkImportDatabase(properties);
        ControllerHook.getInstance().putRecomController(this);

        dummyLock = new Object();
        embeddingSize = 50;
        topK = 20;
        checkIndex(properties);
    }
    private void checkImportDatabase(Properties properties){

        String host = (String) properties.getOrDefault("spring.data.mongodb.host", "localhost");
        //System.getenv("MONGO_HOST");
        String port =(String) properties.getOrDefault("spring.data.mongodb.port", "27017");
        //System.getenv("MONGO_PORT");
        String db = (String) properties.getOrDefault("spring.data.mongodb.database", "cerebro");
        //System.getenv("MONGO_DATABASE");
        cornacURL = (String) properties.get("cornac.url");

        MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://" + host + ":" + port));
        MongoDatabase database = mongoClient.getDatabase(db);

        //this mean the database is empty -> import from a defined source
        if(!database.listCollectionNames().iterator().hasNext()){
            System.out.println("Found empty database, importing from defined source");
            String importdbUrl = (String) properties.get("importdb.url");
            try {
                importDatabase(importdbUrl, host, port);
            } catch (Exception e) {
                System.out.println("import failed.");
                e.printStackTrace();
            }
        }
    }
    private void importDatabase(String importurl, String dbhost, String dbport) throws IOException, ParseException {
        System.out.println("importing data from: " + dbhost + ":" + dbport);
        //object to hold request body
        JSONObject requestObj = new JSONObject();
        requestObj.put("dbhost", dbhost);
        requestObj.put("dbport", dbport);
        URL obj = new URL(importurl);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; utf-8");

        // For POST only - START
        con.setDoOutput(true);
        OutputStream os = con.getOutputStream();
        os.write(requestObj.toJSONString().getBytes());
        os.flush();
        os.close();
        // For POST only - END

        int responseCode = con.getResponseCode();
        System.out.println("POST Response Code :: " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) {
            System.out.println("Import data successful.");
            UpdateController.tellCornacToUpdate(cornacURL);
        } else {
            System.out.println("POST request not worked, import failed");
        }
    }
    private void checkIndex(Properties properties){
        String idxDir = (String) properties.getOrDefault("idxpath", "./idx");
        Exception error = null;
        try {
            HnswManager.printIndexInfo(idxDir);
        }catch(Exception e){
            error = e;
        }

        if(error == null) {
            searcher = new HnswIndexSearcher(idxDir);
        }
    }
    public void switchSearcher(HnswIndexSearcher<double[]> searcher){
        synchronized (dummyLock){
            this.searcher = searcher;
        }
        System.out.println("new index searcher is ready");
    }
    public ItemsRepository getItemsRepository() {
        return itemsRepository;
    }


    @CrossOrigin
    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public String helloWorld(){
        return "Hello World";
    }


    @CrossOrigin
    @RequestMapping(value = "/getRecom", method = RequestMethod.POST)
    public ListID getRecommendations(@Valid @RequestBody TextQuery id) throws IOException {
        //Document user = ratingCollection.find(new Document("_id", id.getText())).first();
        Users user = usersRepository.findBy_id(id.getText());
        List<Double> vec = user.vec;
        double[] vectorQuery = ArrayUtils.toPrimitive((Double[])(vec.toArray(new Double[embeddingSize])));

        long first = System.nanoTime();
        ArrayList<String> ids = new ArrayList<>(topK);
        synchronized(dummyLock){
            TopDocs res = searcher.search(vectorQuery, topK);

            if(res != null){

                for (int j = 0; j < res.scoreDocs.length; j++) {
                    StringID realId = (StringID) searcher.getExternalID(res.scoreDocs[j].doc);
                    ids.add(realId.getVal());
                }
            }
        }
        long second = System.nanoTime();
        return new ListID(ids);
        /*
        legacy function return type
        ArrayList<Items> result = (ArrayList<Items>) itemsRepository.findAllById(ids);
        return new ResultAndTime(result, (second - first)/1000_000);

         */
    }



    @CrossOrigin
    @RequestMapping(value = "/relatedItems", method = RequestMethod.POST)
    public ListID relatedItems(@Valid @RequestBody TextQuery id) throws IOException{
        Items qItem = itemsRepository.findBy_id(id.getText());
        double[] vectorQuery = ArrayUtils.toPrimitive(qItem.vec.toArray(new Double[embeddingSize]));
        long first = System.nanoTime();
        ArrayList<String> ids = new ArrayList<>(topK + 1);
        synchronized(dummyLock){
            TopDocs res = searcher.search(vectorQuery, topK + 1);
            if(res != null){
                for(int i = 0; i < res.scoreDocs.length; i++){
                    StringID dbId = (StringID) searcher.getExternalID(res.scoreDocs[i].doc);
                    if(dbId.getVal().equals(id.getText()))
                        continue;
                    ids.add(dbId.getVal());
                }
            }
        }

        long second = System.nanoTime();
        return new ListID(ids);
        /*
        legacy function return type
        ArrayList<Items> results = (ArrayList<Items>) itemsRepository.findAllById(ids);
        return new ResultRelated(qItem, rating, results, (second - first) / 1_000_000f);
         */
    }

    /*
    @CrossOrigin
    @RequestMapping(value = "/searchTitle", method = RequestMethod.POST)
    public ResultAndTime searchKeyword(@Valid @RequestBody TextQuery qObject) throws IOException {
        String keyword = qObject.getKeyword();
        Query query = null;
        try {
            query = defaultParser.parse(keyword);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long first = System.nanoTime();
        TopDocs hits = textSearch.search(query, topK);
        ArrayList<String> ids = new ArrayList<>();
        for (ScoreDoc hit: hits.scoreDocs) {
            String ID = textSearch.doc(hit.doc).get("ID");
            ids.add(ID);
        }
        long second = System.nanoTime();
        ArrayList<Items> result = (ArrayList<Items>) itemsRepository.findAllById(ids);
        return new ResultAndTime(result, (second - first) / 1000_000f);
    }

     */

}
