package ai.preferred.cerebro.webservice.movieLens.repositories;

import ai.preferred.cerebro.webservice.movieLens.models.Items;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author hpminh@apcs.vn
 */
public interface ItemsRepository extends MongoRepository<Items, ObjectId> {
    Items findBy_id(ObjectId _id);
}
