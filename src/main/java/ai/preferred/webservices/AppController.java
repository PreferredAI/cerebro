package ai.preferred.webservices;

import ai.preferred.cerebro.index.request.LoadSearcherRequest;
import ai.preferred.cerebro.index.search.LuIndexSearcher;
import ai.preferred.cerebro.index.utils.IndexConst;
import ai.preferred.webservices.models.Items;
import ai.preferred.webservices.repositories.ItemsRepository;
import ai.preferred.webservices.repositories.UsersRepository;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.ScoreDoc;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

@RestController
@RequestMapping("/indexed/movies")
public class AppController {
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private ItemsRepository itemsRepository;

    private   LuIndexSearcher searcher;
    private double[][] userVecs;
    private QueryParser queryParser;
    private MultiFieldQueryParser relatedParser;
    //start and warm up searcher
    AppController() {
        queryParser = new QueryParser("title", new StandardAnalyzer());
        relatedParser = new MultiFieldQueryParser(new String[] {"title", "genre"}, new StandardAnalyzer());

        String host = System.getenv("MONGO_HOST");
        String port = System.getenv("MONGO_PORT");
        String db = System.getenv("MONGO_DATABASE");
        System.out.println("Read MONGO_HOST: " +  host);
        System.out.println("Read MONGO_PORT: " +  port);
        System.out.println("Read MONGO_DATABASE: " +  db);

        MongoClientURI connectionString = new MongoClientURI("mongodb://" + host + ":" + port);
        MongoClient mongoClient = new MongoClient(connectionString);
        MongoDatabase database = mongoClient.getDatabase(db);
        MongoCollection<org.bson.Document> collection = database.getCollection("users");
        userVecs = new double[(int) collection.countDocuments()][50];
        Iterator<Document> iterator = collection.find().iterator();
        int n = 0;
        while (iterator.hasNext()){
            ArrayList<Double> e = (ArrayList<Double>) iterator.next().get("vec");
            Double[] h = (Double[]) e.toArray(new Double[e.size()]);
            userVecs[n++] = ArrayUtils.toPrimitive(h);
        }

        String idxDir = System.getenv("IDX");

        LoadSearcherRequest request = new LoadSearcherRequest(idxDir, idxDir + "/splitVec.o", false, true);
        try {
            searcher = (LuIndexSearcher) request.getSearcher();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int nWarmupQuery = 1000;
        while (nWarmupQuery-- > 0){
            try {
                searcher.queryVector(userVecs[nWarmupQuery], 10);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @CrossOrigin
    @RequestMapping(value = "/getRecom/{id}", method = RequestMethod.GET)
    public ResultAndTime getRecommendations(@PathVariable("id")Integer orderNum) throws IOException {

        //double[] vecQ = ArrayUtils.toPrimitive((Double[])((List<Double>)user.getVec()).toArray(new Double[50]));
        double[] vecQ = userVecs[orderNum];
        //System.out.println("Vec query: " + Arrays.toString(vecQ));
        ScoreDoc[] sResults = null;
        long first = System.nanoTime();
        try {
            sResults = searcher.queryVector(vecQ, 20);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println("Result size : " + sResults.length);
        ArrayList<ObjectId> ids = new ArrayList<>();
        for (ScoreDoc hit: sResults) {
            String ID = searcher.doc(hit.doc).get(IndexConst.IDFieldName);
            ids.add(new ObjectId(ID));
        }
        ArrayList<Items> result = (ArrayList<Items>) itemsRepository.findAllById(ids);
        long second = System.nanoTime();
        //long third = System.nanoTime();
        //System.out.println("Lucene time: "+ (second - first));
        //System.out.println("DB time: "+ (third - second));
        return new ResultAndTime(result, (second - first)/1000_000);
    }

    @CrossOrigin
    @RequestMapping(value = "/search", method = RequestMethod.PUT)
    public ResultAndTime searchKeyword(@Valid @RequestBody String qObject) throws IOException {
        String keyword = qObject.split(":")[1];
        keyword = keyword.substring(1, keyword.length() - 2).toLowerCase();
        ScoreDoc[] sResults = null;
        long first = System.nanoTime();
        try {
            sResults = searcher.queryKeyWord(queryParser, keyword, 20);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<ObjectId> ids = new ArrayList<>();
        for (ScoreDoc hit: sResults) {
            String ID = searcher.doc(hit.doc).get(IndexConst.IDFieldName);
            ids.add(new ObjectId(ID));
        }
        ArrayList<Items> result = (ArrayList<Items>) itemsRepository.findAllById(ids);
        long second = System.nanoTime();
        //long third = System.nanoTime();
        //System.out.println("Lucene time: "+ (second - first));
        //System.out.println("DB time: "+ (third - second));
        return new ResultAndTime(result, (second - first) / 1000_000);
    }

    @CrossOrigin
    @RequestMapping(value = "/relatedItems/{id}", method = RequestMethod.GET)
    public ResultRelated relatedItems(@PathVariable("id") ObjectId id) throws IOException{
        Items qItem = itemsRepository.findBy_id(id);
        double [] itemVecQuery = ArrayUtils.toPrimitive(qItem.vec.toArray(new Double[50]));
        ScoreDoc[] sResults = null;
        long first = System.nanoTime();
        try {
            sResults = searcher.queryVector(itemVecQuery, 21);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<ObjectId> ids = new ArrayList<>();
        for (ScoreDoc hit: sResults) {
            String ID = searcher.doc(hit.doc).get(IndexConst.IDFieldName);
            if(ID.equals(id.toHexString()))
                continue;
            ids.add(new ObjectId(ID));
        }
        ArrayList<Items> result = (ArrayList<Items>) itemsRepository.findAllById(ids);
        long second = System.nanoTime();
        //long third = System.nanoTime();
        //System.out.println("Lucene time: "+ (second - first));
        //System.out.println("DB time: "+ (third - second));
        return new ResultRelated(qItem, result, (second - first) / 1000_000);
    }
}
