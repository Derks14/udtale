package udtale.dto;

import java.util.List;

public record PronunciationCheckResponse (
        String transcript,
        double similarityScore,
        List<PronunciationFinding> findings,
        List<String> drills
) {}
