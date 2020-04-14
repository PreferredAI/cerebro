package ai.preferred.cerebro.webservice.models;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.List;

public class Items {
    @Id
    public ObjectId _id;
    public String title;
    public String genre;
    public List<Float> vec;

    public Items(ObjectId _id, String title, String genre, List<Float> vec) {
        this._id = _id;
        this.title = title;
        this.genre = genre;
        this.vec = vec;
    }

    public String get_id() {
        return _id.toHexString();
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public List<Float> getVec() {
        return vec;
    }

    public void setVec(List<Float> vec) {
        this.vec = vec;
    }
}
