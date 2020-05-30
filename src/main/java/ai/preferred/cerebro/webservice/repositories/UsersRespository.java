package ai.preferred.cerebro.webservice.repositories;

import ai.preferred.cerebro.webservice.models.Users;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author hpminh@apcs.vn
 */
public interface UsersRespository  extends MongoRepository<Users, String> {
    Users findBy_id(String _id);
}
