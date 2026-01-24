package udtale.models;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import udtale.dto.Severity;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
@Data
public class Issue extends BaseDocument{

    @DocumentReference
    private PronunciationSession session;
    private String word;
    private IssueType issueType;
    private Severity severity;
    private String phonetic;
    private String correction;
}
