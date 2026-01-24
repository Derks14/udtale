package udtale.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback {
    private String transcribedText;
    private double overallAccuracy;
    private List<Mispronunciation> mispronunciations;
    private List<String> generalTips;
    private EnglishFeatures australianEnglishFeatures;
}
