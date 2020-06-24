package ai.preferred.cerebro.webservice;

import ai.preferred.cerebro.feedback.Rating;
import ai.preferred.cerebro.webservice.models.Items;
import ai.preferred.cerebro.webservice.models.Ratings;
import ai.preferred.cerebro.webservice.models.Users;
import ai.preferred.cerebro.webservice.repositories.ItemsRepository;
import ai.preferred.cerebro.webservice.repositories.RatingsRespository;
import ai.preferred.cerebro.webservice.repositories.UsersRespository;
import ai.preferred.cerebro.webservice.tasks.BuildHNSWIdxTask;
import ai.preferred.cerebro.webservice.tasks.BuildTxtIdxTask;
import com.mongodb.client.MongoCollection;


import org.bson.Document;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

/**
 * @author hpminh@apcs.vn
 */

@RestController
@RequestMapping("/update")
public class UpdateController {
    private RecomController recomController;
    RatingsRespository ratingRespo;
    UsersRespository usersRespository;
    ItemsRepository itemsRepository;
    int newRating = 0;
    int updateThreshold;
    String cornacURL;



    UpdateController(){
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Properties properties = new Properties();
        try (InputStream resourceStream = loader.getResourceAsStream("application.properties")) {
            properties.load(resourceStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateThreshold = (int) properties.get("newRatingsTillUpdate");
        cornacURL = (String) properties.get("cornac.url");
        ControllerHook.getInstance().putUpdateController(this);
    }

    public void setParams(RatingsRespository ratingRespo,
                          UsersRespository usersRespository,
                          ItemsRepository itemsRepository,
                          RecomController recomController ,
                          String cornacURL){
        this.ratingRespo = ratingRespo;
        this.usersRespository = usersRespository;
        this.itemsRepository = itemsRepository;
        this.cornacURL = cornacURL;
        this.recomController = recomController;
    }


    @CrossOrigin
    @RequestMapping(value="/feedback", method = RequestMethod.POST)
    public void receiveUpdate(@Valid @RequestBody Ratings rating){
        ratingRespo.insert(rating);
        if(++newRating == updateThreshold){
            try {
                tellCornacToUpdate(cornacURL);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @CrossOrigin
    @RequestMapping(value="/addUser", method = RequestMethod.POST)
    public void addUser(@Valid @RequestBody Users user){
        usersRespository.insert(user);
    }

    @CrossOrigin
    @RequestMapping(value="/addItem", method = RequestMethod.POST)
    public void addUser(@Valid @RequestBody Items item){
        itemsRepository.insert(item);
    }

    @CrossOrigin
    @RequestMapping(value="/invoke", method = RequestMethod.GET)
    public void update(){
        try {
            tellCornacToUpdate(cornacURL);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // update index after this
    }

    @CrossOrigin
    @RequestMapping(value = "/buildIdx", method = RequestMethod.POST)
    public String buildIdx(){
        String idxDir = "./idx"; //whatever fill this in later
        File file = new File(idxDir);
        if(!file.exists() || !file.isDirectory()){
            file.mkdir();
        }
        int embeddingSize = 50;//whatever fill this in later
        BuildHNSWIdxTask task = new BuildHNSWIdxTask(idxDir, embeddingSize, recomController);
        Thread t = new Thread(task);
        t.start();
        return "Building index";
    }

    @CrossOrigin
    @RequestMapping(value = "/textIdx", method = RequestMethod.POST)
    public void buildTxtIdx(){
        String idxDir = "./txtIdx"; //whatever fill this in later
        BuildTxtIdxTask task = new BuildTxtIdxTask(idxDir, recomController);
        Thread t = new Thread(task);
        t.start();
    }

    public static void tellCornacToUpdate(String urlstr) throws IOException {
        URL url = new URL(urlstr);
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
