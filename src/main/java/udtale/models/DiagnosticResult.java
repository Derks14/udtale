package udtale.models;


import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Document
public class DiagnosticResult extends BaseDocument {
    private Learner learner;
    private Diagnostics diagnostics;
    private String ipa;
    private String fileUrl;

}
