package udtale.repositories;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import udtale.models.Learner;
import udtale.models.PronunciationSession;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PronunciationSessionRepository extends MongoRepository<PronunciationSession, String> {
    List<PronunciationSession> findByLearnerOrderBySessionDateDesc(Learner learner);

//    @Query("{'user': ?0, 'sessionDate': { $gte: ?1 }}")
//    List<PronunciationSession> findRecentSessions(Learner user, LocalDateTime since);
    List<PronunciationSession> findByLearnerAndSessionDateGreaterThanEqualOrderBySessionDateDesc(Learner learner, LocalDateTime since);


    @Aggregation(pipeline = {
            "{ $match: { learner: ?0 } }",
            "{ $group: { _id: null, avgAccuracy: { $avg: \"$accuracyScore\" } } }"
    })
    Double getAverageAccuracy(Learner learner);




}
