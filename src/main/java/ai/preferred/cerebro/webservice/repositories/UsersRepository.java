package ai.preferred.cerebro.webservice.repositories;


import ai.preferred.cerebro.webservice.models.Users;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UsersRepository extends MongoRepository<Users, ObjectId> {
    Users findBy_id(ObjectId _id);
}
