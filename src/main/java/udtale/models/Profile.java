package udtale.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Document
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class Profile extends BaseDocument {

    @DocumentReference
    private Learner learner;

    @Indexed( unique = true)
    private String primaryGoal;

    @NotBlank
    private String motivationTrigger;

    @NotBlank
    private String motivationType;

    @NotBlank
    private String proficiencyGoal;

    private String nativeLanguage;
    private String nativeRhythm;
    private String englishProficiency;
    private String pastEnglishLessons;

    private String locationRegion;
    private String dailyEnglishFrequency;
    private String nativeSpeakerContactFreq;

    private List<String> primaryContext;

    private String listeningDifficulty;
    private List<String> contentSources;
    private String ambientAccent;
    private String bestUnderstoodAccent;

    private String selfRatedPronunciation;

    private List<String> challengingSounds;
    private List<String> difficultyFocus;
    private String speakingSpeed;
    private String voiceRecordingComfort;

    private String confidenceLevel;
    private String biggestChallenge;
    private String feedbackStyle;

    private String targetAccent;
    private String accentGoal;

    private String dailyPracticeGoal;
    private String weeklyAvailability;

    private String practiceTime;
    private List<String> practiceModes;
    private List<String> preferredTone;


//    RC45911072754
}
