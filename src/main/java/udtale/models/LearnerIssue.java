package udtale.models;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.LocalDateTime;

@Document
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class LearnerIssue extends BaseDocument {
    @DocumentReference
    private Learner learner;

    private IssueType issueType;
    private String description;
    private int occurrenceCount;
    private LocalDateTime firstDetected;
    private LocalDateTime lastDetected;
    private boolean resolved;
}
