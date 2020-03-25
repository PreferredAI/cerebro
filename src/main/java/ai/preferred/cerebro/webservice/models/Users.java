package ai.preferred.cerebro.webservice.models;


import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.List;

public class Users {
    @Id
    public ObjectId _id;
    public List<Double> vec;

    public Users(ObjectId _id, List<Double> vec) {
        this._id = _id;
        this.vec = vec;
    }

    public String get_id() {
        return _id.toHexString();
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public List<Double> getVec() {
        return vec;
    }

    public void setVec(List<Double> vec) {
        this.vec = vec;
    }
}
