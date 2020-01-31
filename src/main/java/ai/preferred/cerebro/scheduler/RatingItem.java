package ai.preferred.cerebro.scheduler;

/**
 * @author hpminh@apcs.vn
 */
public class RatingItem {
    String userID;
    String itemID;
    float rating;

    public RatingItem(String userID, String itemID, float rating) {
        this.userID = userID;
        this.itemID = itemID;
        this.rating = rating;
    }

    public RatingItem(){

    }
}
