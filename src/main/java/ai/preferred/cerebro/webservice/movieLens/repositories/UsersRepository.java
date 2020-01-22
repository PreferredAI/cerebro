package ai.preferred.cerebro.webservice.movieLens.repositories;

import ai.preferred.cerebro.webservice.movieLens.models.Users;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author hpminh@apcs.vn
 */
public interface UsersRepository extends MongoRepository<Users, ObjectId> {
    Users findBy_id(ObjectId _id);
}
