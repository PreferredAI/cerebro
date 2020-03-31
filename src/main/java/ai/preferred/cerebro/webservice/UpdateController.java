package ai.preferred.cerebro.webservice;

import ai.preferred.cerebro.feedback.Interaction;
import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;


import org.bson.Document;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Iterator;
import java.util.Properties;

/**
 * @author hpminh@apcs.vn
 */

@RestController
@RequestMapping("/update")
public class UpdateController {

    MongoCollection<Document> ratingCollection;
    String cornacURL;


    private RecomController recomController;


    UpdateController(){

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
        cornacURL = (String) properties.getOrDefault("CORNAC", "https://localhost:5000/generate");

        System.out.println("Read MONGO_HOST: " +  host);
        System.out.println("Read MONGO_PORT: " +  port);
        System.out.println("Read MONGO_DATABASE: " +  db);
        System.out.println("Read RATING_COLLECTION: " +  collectionName);

        MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://" + host + ":" + port));
        MongoDatabase database = mongoClient.getDatabase(db);
        ratingCollection = database.getCollection(collectionName);

        ControllerHook.getInstance().putUpdateController(this);
    }

    public void setRecomController(RecomController recomController) {
        this.recomController = recomController;
    }

    @CrossOrigin
    @RequestMapping(value="/feedback", method = RequestMethod.POST)
    public void receiveUpdate(@Valid @RequestBody Interaction interaction){
        ratingCollection.updateOne(new Document("_id", interaction.userID),
                new Document("$set", new Document(interaction.itemID, interaction.rating)));
    }



    @CrossOrigin
    @RequestMapping(value="/invoke", method = RequestMethod.POST)
    public void update(){
        try {
            tellCornacToUpdate();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // update index after this
    }

    @CrossOrigin
    @RequestMapping(value = "/buildIdx", method = RequestMethod.POST)
    public void buildIdx(){
        String idxDir = ""; //whatever fill this in later
        int embeddingSize = 50;//whatever fill this in later
        BuildHNSWIdxTask task = new BuildHNSWIdxTask(idxDir, embeddingSize, recomController);
        Thread t = new Thread(task);
        t.start();
    }

    public void tellCornacToUpdate() throws IOException {
        URL url = new URL(cornacURL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Cerebro");
        int responseCode = con.getResponseCode();
        System.out.println("GET Response Code :: " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            // print result
            System.out.println("Update request received, generating new vectors");
        } else {
            System.out.println("Update request failed");
        }
    }
}
