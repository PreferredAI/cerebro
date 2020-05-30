package ai.preferred.cerebro.webservice.repositories;

import ai.preferred.cerebro.webservice.models.Ratings;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author hpminh@apcs.vn
 */
public interface RatingsRespository  extends MongoRepository<Ratings, ObjectId> {
    Ratings findBy_id(ObjectId _id);
}
