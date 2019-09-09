package ai.preferred.webservices.repositories;

import ai.preferred.webservices.models.Items;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ItemsRepository extends MongoRepository<Items, ObjectId> {
    Items findBy_id(ObjectId _id);
}
