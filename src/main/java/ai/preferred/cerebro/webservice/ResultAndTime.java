package ai.preferred.cerebro.webservice;

import ai.preferred.cerebro.webservice.models.Items;

import java.util.List;

/**
 * @author hpminh@apcs.vn
 */
public class ResultAndTime {
    List<Items> items;
    float time;

    public ResultAndTime(List<Items> items, float time) {
        this.items = items;
        this.time = time;
    }

    public List<Items> getItems() {
        return items;
    }

    public void setItems(List<Items> items) {
        this.items = items;
    }

    public float getTime() {
        return time;
    }

    public void setTime(float time) {
        this.time = time;
    }
}
