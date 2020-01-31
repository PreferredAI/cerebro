package ai.preferred.cerebro.scheduler;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * @author hpminh@apcs.vn
 */
public class MongoRatingRetriever implements RatingRetriever {
    MongoCollection<Document> collection;

    public MongoRatingRetriever(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    public ArrayList<RatingItem> retrieve(){
        ArrayList<RatingItem> ratingList = new ArrayList<>();
        MongoCursor iterator = collection.find().iterator();
        while(iterator.hasNext()){
            Document doc = (Document) iterator.next();
            String userID = doc.getString("_id");
            Set<Map.Entry<String, Object>> attributes = doc.entrySet();
            for (Map.Entry<String, Object> rating: attributes) {
                if(rating.getKey().compareTo("_id") != 0){
                    ratingList.add(new RatingItem(userID, rating.getKey(), (Float)rating.getValue()));
                }
            }
        }
        return ratingList;
    }
}
