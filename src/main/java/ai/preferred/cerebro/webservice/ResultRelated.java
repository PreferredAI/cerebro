package ai.preferred.cerebro.webservice;

import ai.preferred.cerebro.webservice.models.Items;

import java.util.List;

/**
 * @author hpminh@apcs.vn
 */
public class ResultRelated {
    Items query;
    float rating;
    List<Items> items;
    long time;

    public ResultRelated(Items query, List<Items> items, long time) {
        this.query = query;
        this.items = items;
        this.time = time;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
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
