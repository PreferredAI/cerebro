package ai.preferred.cerebro.webservice;

import ai.preferred.cerebro.webservice.models.Items;

import java.util.List;

/**
 * @author hpminh@apcs.vn
 */
public class ResultAndTime {
    List<Items> items;
    long time;

    public ResultAndTime(List<Items> items, long time) {
        this.items = items;
        this.time = time;
    }

    public List<Items> getItems() {
        return items;
    }

    public void setItems(List<Items> items) {
        this.items = items;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
