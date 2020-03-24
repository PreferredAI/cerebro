package ai.preferred.cerebro.feedback;

import ai.preferred.cerebro.index.ids.ExternalID;

/**
 * @author hpminh@apcs.vn
 */
public class Interaction {
    public String userID;
    public String itemID;
    public float rating;


    Interaction(){}

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}
 