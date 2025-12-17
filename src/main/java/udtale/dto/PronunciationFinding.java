package udtale.dto;

public record PronunciationFinding(
        String type,
        String expected,
        String observed,
        String evidence,
        Severity severity,
        String coachingTip
) {}
