package ai.preferred.cerebro.feedback;

import ai.preferred.cerebro.index.ids.ExternalID;

/**
 * @author hpminh@apcs.vn
 */
public abstract class Interaction {
    String userID;
    String itemID;
    String description;
    float rating;


    abstract public void inferScore();
}
