package udtale.models;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
@Builder
@NoArgsConstructor
@Setter
@Getter
@ToString
public class Question extends BaseDocument {

    @Indexed(unique = true)
    private String field;

    @NotBlank
    private QuestionGroup group;

    @NotBlank
    private String question;
    private String inputType;

    private List<String> options;

    private boolean required;

    private String notes;

    public Question(String field, QuestionGroup group, String question, String inputType, List<String> options, boolean required, String notes) {
        this.field = field;
        this.group = group;
        this.question = question;
        this.inputType = inputType;
        this.options = options;
        this.required = required;
        this.notes = notes;
    }
}
