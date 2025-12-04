package udtale.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import org.springframework.data.domain.Sort.Direction;

@Setter
@Getter
@Value
@Builder
public class QuestionSearchRequest {
    String search;
    String field;
    String group;
    boolean required;
    int page;
    int size;
    String sortBy;
    Direction sortDirection = Direction.ASC;
}
