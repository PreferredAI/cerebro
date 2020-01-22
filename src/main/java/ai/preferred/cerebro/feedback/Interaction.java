package ai.preferred.cerebro.feedback;

import ai.preferred.cerebro.index.ids.ExternalID;

/**
 * @author hpminh@apcs.vn
 */
public abstract class Interaction {
    public String userID;
    public String itemID;
    public String description;
    public float rating;


    abstract public void inferScore();
}
