package udtale.models;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class EnglishFeatures {
    private List<String> vowelIssues;
    private List<String> rhoticity;
    private List<String> intonationNotes;
    private List<String> stressPatterns;
}
