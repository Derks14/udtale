package udtale.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import udtale.models.Question;

@Repository
public interface QuestionRepository extends MongoRepository<Question, String> {
}
