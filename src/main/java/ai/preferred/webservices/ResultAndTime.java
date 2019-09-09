package ai.preferred.webservices;

import ai.preferred.webservices.models.Items;

import java.util.List;

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
