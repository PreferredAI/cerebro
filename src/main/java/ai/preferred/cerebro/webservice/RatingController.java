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
@RequestMapping("/")
public class RatingController {

    MongoCollection<Document> ratingCollection;
    String cornacURL;

    RatingController(){

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Properties properties = new Properties();
        try (InputStream resourceStream = loader.getResourceAsStream("application.properties")) {
            properties.load(resourceStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String host = (String) properties.getOrDefault("MONGO_HOST", "localhost");
        //System.getenv("MONGO_HOST");
        String port =(String) properties.getOrDefault("MONGO_PORT", "27017");
        //System.getenv("MONGO_PORT");
        String db = (String) properties.getOrDefault("MONGO_DATABASE", "movieLens");
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

    }


    @CrossOrigin
    @RequestMapping(value="/feedback", method = RequestMethod.POST)
    public void receiveUpdate(@Valid @RequestBody Interaction interaction){
        ratingCollection.updateOne(new Document("_id", interaction.userID),
                new Document("$set", new Document(interaction.itemID, interaction.rating)));
    }

    @CrossOrigin
    @RequestMapping(value="/update", method = RequestMethod.GET)
    public void update(){
        try {
            tellCornacToUpdate();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // update index after this
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
