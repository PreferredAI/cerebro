package ai.preferred.webservices;

import ai.preferred.webservices.models.Items;

import java.util.List;

public class ResultRelated {
    Items query;
    List<Items> items;
    long time;

    public ResultRelated(Items query, List<Items> items, long time) {
        this.query = query;
        this.items = items;
        this.time = time;
    }

    public Items getQuery() {
        return query;
    }

    public void setQuery(Items query) {
        this.query = query;
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
