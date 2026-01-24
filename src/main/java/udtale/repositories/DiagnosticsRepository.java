package udtale.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import udtale.models.Diagnostics;
import udtale.models.Learner;

import java.util.Optional;

public interface DiagnosticsRepository extends MongoRepository<Diagnostics, String> {
    Optional<Diagnostics> findByLearner(Learner learner);
}
