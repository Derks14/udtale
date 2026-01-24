package udtale.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import udtale.models.IssueType;
import udtale.models.Learner;
import udtale.models.LearnerIssue;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearnerIssueRepository extends MongoRepository<LearnerIssue, String> {
    Optional<LearnerIssue> findByLearnerAndIssueTypeAndDescription(Learner learner, IssueType issueType, String description);

    List<LearnerIssue> findByLearnerAndResolvedFalseOrderByOccurrenceCountDesc(Learner learner);

    List<LearnerIssue> findByLearnerAndResolvedTrueOrderByLastDetectedDesc(Learner learner);




}
