package ai.preferred.cerebro.webservice.requests;

/**
 * @author hpminh@apcs.vn
 */
public class PairIds {
    String userId;
    String itemId;

    public PairIds() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }
}
