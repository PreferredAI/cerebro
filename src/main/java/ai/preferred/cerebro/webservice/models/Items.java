package ai.preferred.cerebro.webservice.models;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.List;

public class Items {
    @Id
    public String _id;
    public String title;
    public String genres;
    public List<Double> vec;

    public Items(String _id, String title, String genres, List<Double> vec) {
        this._id = _id;
        this.title = title;
        this.genres = genres;
        this.vec = vec;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public List<Double> getVec() {
        return vec;
    }

    public void setVec(List<Double> vec) {
        this.vec = vec;
    }
}
