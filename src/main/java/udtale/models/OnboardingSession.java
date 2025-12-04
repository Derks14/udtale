package udtale.models;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "onboarding_sessions")
@CompoundIndex(name = "learner_session_index", def = "{'learner': 1, 'session_start': -1}")
public class OnboardingSession {

    @DocumentReference
    private Learner learner;

    private int sessionNumber;
    private LocalDateTime sessionStart;
    private LocalDateTime sessionEnd;
    private boolean completed;
    private List<Response> responses;
}
