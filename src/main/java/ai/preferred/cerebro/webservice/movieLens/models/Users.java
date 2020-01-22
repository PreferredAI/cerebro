package ai.preferred.cerebro.webservice.movieLens.models;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.List;

/**
 * @author hpminh@apcs.vn
 */
public class Users {
    @Id
    public ObjectId _id;
    public List<Float> vec;

    public Users(ObjectId _id, List<Float> vec) {
        this._id = _id;
        this.vec = vec;
    }

    public String get_id() {
        return _id.toHexString();
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public List<Float> getVec() {
        return vec;
    }

    public void setVec(List<Float> vec) {
        this.vec = vec;
    }
}
