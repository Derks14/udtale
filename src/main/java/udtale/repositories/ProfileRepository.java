package udtale.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import udtale.models.Learner;
import udtale.models.Profile;

import java.util.Optional;

public interface ProfileRepository extends MongoRepository<Profile, String> {
    Optional<Profile> findByLearner(Learner learner);
}
