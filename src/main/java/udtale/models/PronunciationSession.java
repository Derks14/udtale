package udtale.models;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Builder
@AllArgsConstructor
@Setter
@Getter
@NoArgsConstructor
@Document
public class PronunciationSession extends BaseDocument {

    @DocumentReference
    private Learner learner;

    private String targetText;
    private String transcribedIPA;
    private double accuracyScore;
    private LocalDateTime sessionDate;
    private List<Issue> issues = new ArrayList<>();

}
