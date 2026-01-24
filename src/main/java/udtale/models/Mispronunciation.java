package udtale.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import udtale.dto.Severity;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mispronunciation {
    private String word;
    private int position;
    private String expected;
    private String actual;
    private String phonetic;
    private String phoneticRespelling;
    private String correction;
    private String tip;
    private Severity severity;
}
