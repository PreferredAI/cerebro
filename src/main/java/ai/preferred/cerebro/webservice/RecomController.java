package ai.preferred.cerebro.webservice;

import ai.preferred.cerebro.index.hnsw.HnswManager;
import ai.preferred.cerebro.index.hnsw.searcher.HnswIndexSearcher;
import ai.preferred.cerebro.index.ids.StringID;
import ai.preferred.cerebro.webservice.models.Items;
import ai.preferred.cerebro.webservice.repositories.ItemsRepository;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;

/**
 * @author hpminh@apcs.vn
 */

@RestController
@RequestMapping("/search")
public class RecomController {

    //@Autowired
    //private UsersRepository usersRepository;
    MongoCollection<Document> ratingCollection;

    @Autowired
    private ItemsRepository itemsRepository;

    public void setTextSearch(IndexSearcher textSearch) {
        this.textSearch = textSearch;
        System.out.println("Switch new text searcher");
    }

    HnswIndexSearcher<float[]> searcher;
    private QueryParser defaultParser;
    IndexSearcher textSearch;
    private final Object dummyLock;
    int embeddingSize;
    int topK;
    RecomController() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Properties properties = new Properties();
        try (InputStream resourceStream = loader.getResourceAsStream("application.properties")) {
            properties.load(resourceStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String host = (String) properties.getOrDefault("spring.data.mongodb.host", "localhost");
        //System.getenv("MONGO_HOST");
        String port =(String) properties.getOrDefault("spring.data.mongodb.port", "27017");
        //System.getenv("MONGO_PORT");
        String db = (String) properties.getOrDefault("spring.data.mongodb.database", "movieLens");
        //System.getenv("MONGO_DATABASE");
        String collectionName = (String) properties.getOrDefault("RATING_COLLECTION", "user_rating");
        //System.getenv("RATING_COLLECTION");


        System.out.println("Read MONGO_HOST: " +  host);
        System.out.println("Read MONGO_PORT: " +  port);
        System.out.println("Read MONGO_DATABASE: " +  db);
        System.out.println("Read RATING_COLLECTION: " +  collectionName);

        MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://" + host + ":" + port));
        MongoDatabase database = mongoClient.getDatabase(db);
        ratingCollection = database.getCollection(collectionName);
        ControllerHook.getInstance().putRecomController(this);


        String idxDir = "./idx";//System.getenv("IDX");
        dummyLock = new Object();
        embeddingSize = 50;
        topK = 20;
        Throwable error = null;
        try {
            HnswManager.printIndexInfo(idxDir);
        }catch(Exception e){
            error = e;
        }

        if(error != null) {
            searcher = new HnswIndexSearcher(idxDir);
        }
        String txtIdx = "./txtIdx";
        this.defaultParser = new QueryParser("title", new StandardAnalyzer());
        IndexReader reader = null;
        try{
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(txtIdx)));
        }
        catch(IOException e){
            error = e;
        }
        if(error != null)
            textSearch = new IndexSearcher(reader , Executors.newFixedThreadPool(2));

    }

    public void switchSearcher(HnswIndexSearcher<float[]> searcher){
        synchronized (dummyLock){
            this.searcher = searcher;
        }
        System.out.println("Switch new searcher");
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
    @RequestMapping(value = "/getRecom/{id}", method = RequestMethod.GET)
    public ResultAndTime getRecommendations(@PathVariable("id")ObjectId id) throws IOException {
        Document user = ratingCollection.find(new Document("_id", id)).first();
        List<Float> vec = (List<Float>) user.get("vec");
        float[] vectorQuery = ArrayUtils.toPrimitive((Float[])(vec.toArray(new Float[embeddingSize])));

        long first = System.nanoTime();
        ArrayList<ObjectId> ids = new ArrayList<>(topK);
        synchronized(dummyLock){
            TopDocs res = searcher.search(vectorQuery, topK);

            if(res != null){

                for (int j = 0; j < res.scoreDocs.length; j++) {
                    StringID realId = (StringID) searcher.getExternalID(res.scoreDocs[j].doc);
                    ids.add(new ObjectId(realId.getVal()));
                }
            }
        }
        long second = System.nanoTime();

        ArrayList<Items> result = (ArrayList<Items>) itemsRepository.findAllById(ids);
        //long third = System.nanoTime();
        //System.out.println("Lucene time: "+ (second - first));
        //System.out.println("DB time: "+ (third - second));
        return new ResultAndTime(result, (second - first)/1000_000);
    }



    @CrossOrigin
    @RequestMapping(value = "/relatedItems/{id}", method = RequestMethod.GET)
    public ResultRelated relatedItems(@PathVariable("id") ObjectId id) throws IOException{
        Items qItem = itemsRepository.findBy_id(id);
        float[] vectorQuery = ArrayUtils.toPrimitive(qItem.vec.toArray(new Float[embeddingSize]));
        long first = System.nanoTime();
        ArrayList<ObjectId> ids = new ArrayList<>(topK + 1);

        synchronized(dummyLock){
            TopDocs res = searcher.search(vectorQuery, topK + 1);
            if(res != null){
                for(int i = 0; i < res.scoreDocs.length; i++){
                    StringID dbId = (StringID) searcher.getExternalID(res.scoreDocs[i].doc);
                    if(dbId.getVal().equals(id.toString()))
                        continue;
                    ids.add(new ObjectId(dbId.getVal()));
                }
            }
        }

        long second = System.nanoTime();
        ArrayList<Items> results = (ArrayList<Items>) itemsRepository.findAllById(ids);
        return new ResultRelated(qItem, results, (second - first) / 1_000_000);
    }

    @CrossOrigin
    @RequestMapping(value = "/searchTitle", method = RequestMethod.POST)
    public ResultAndTime searchKeyword(@Valid @RequestBody String qObject) throws IOException {
        String keyword = qObject.split(":")[1];
        keyword = keyword.substring(1, keyword.length() - 2).toLowerCase();
        Query query = null;
        try {
            query = defaultParser.parse(keyword);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long first = System.nanoTime();
        TopDocs hits = textSearch.search(query, topK);
        ArrayList<ObjectId> ids = new ArrayList<>();
        for (ScoreDoc hit: hits.scoreDocs) {
            String ID = textSearch.doc(hit.doc).get("ID");
            ids.add(new ObjectId(ID));
        }
        long second = System.nanoTime();
        ArrayList<Items> result = (ArrayList<Items>) itemsRepository.findAllById(ids);
        return new ResultAndTime(result, (second - first) / 1000_000);
    }

}
