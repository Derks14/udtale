package udtale.models;

public enum QuestionGroup {

    MOTIVATION_AND_GOALS("Motivation & Goals"),
    LINGUISTIC_BACKGROUND("Linguistic Background"),
    EXPOSURE_AND_ENVIRONMENT("Exposure & Environment"),
    SPEAKING_CONTEXT("Speaking Context"),
    LISTENING_AND_PERCEPTION("Listening & perception"),
    ARTICULATION_AND_AWARENESS("Articulation & Awareness"),
    CONFIDENCE_AND_COMMUNICATION("Confidence & Communication"),
    ACCENT_AND_GOAL_PREFERENCES("Accent & Goal Preferences"),
    PRACTICE_SETUP_AND_PREFERENCES("Practice Setup & Preferences");

    private final String label;

    QuestionGroup(String label) {
        this.label = label;
    }

    public String label() {
        return this.label;
    }

}
