package udtale.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Insights {
    private int totalSessions;
    private double averageAccuracy;
    private double progressPercentage;
    private List<String> recurringIssues;
    private List<String> improvedAreas;
    private List<String> focusAreas;
    private String motivationalMessage;
}
