package udtale.models;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.List;

@Builder
@Setter
@Getter
@Document
@ToString
public class Diagnostics extends BaseDocument {
    @DocumentReference
    @Indexed( unique = true)
    private Learner learner;

    private List<Diagnostic> diagnostics;

}
