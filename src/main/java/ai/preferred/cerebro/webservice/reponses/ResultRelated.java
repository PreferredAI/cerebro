package ai.preferred.cerebro.webservice.reponses;

import ai.preferred.cerebro.webservice.models.Items;

import java.util.List;

/**
 * @author hpminh@apcs.vn
 */
public class ResultRelated {
    Items query;
    Double rating;
    List<Items> items;
    float time;

    public ResultRelated(Items query, Double rating, List<Items> items, float time) {
        this.query = query;
        this.rating = rating;
        this.items = items;
        this.time = time;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
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

    public float getTime() {
        return time;
    }

    public void setTime(float time) {
        this.time = time;
    }
}
