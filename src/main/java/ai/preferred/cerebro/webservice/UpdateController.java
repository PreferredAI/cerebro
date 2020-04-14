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


    public void setRatingCollection(MongoCollection<Document> ratingCollection) {
        this.ratingCollection = ratingCollection;
    }

    private RecomController recomController;


    UpdateController(){
        cornacURL = "https://localhost:5000/generate";
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
        String idxDir = "./idx"; //whatever fill this in later
        int embeddingSize = 50;//whatever fill this in later
        BuildHNSWIdxTask task = new BuildHNSWIdxTask(idxDir, embeddingSize, recomController);
        Thread t = new Thread(task);
        t.start();
    }

    @CrossOrigin
    @RequestMapping(value = "/textIdx", method = RequestMethod.POST)
    public void buildTxtIdx(){
        String idxDir = "./txtIdx"; //whatever fill this in later
        BuildTxtIdxTask task = new BuildTxtIdxTask(idxDir, recomController);
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
