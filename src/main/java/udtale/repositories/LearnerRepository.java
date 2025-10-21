package udtale.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import udtale.models.Learner;

import java.util.Optional;

public interface LearnerRepository extends MongoRepository<Learner, String> {
    Optional<Learner> findUserByUsername(String username);
}
