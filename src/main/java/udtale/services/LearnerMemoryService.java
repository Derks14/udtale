package udtale.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import udtale.models.*;
import udtale.repositories.LearnerIssueRepository;
import udtale.repositories.LearnerRepository;
import udtale.repositories.ProfileRepository;
import udtale.repositories.PronunciationSessionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class LearnerMemoryService {

    private final LearnerRepository learnerRepository;
    private final PronunciationSessionRepository sessionRepository;
    private final LearnerIssueRepository issueRepository;
    private final ProfileRepository profileRepository;
    private final LearnerService learnerService;

    public LearnerMemoryService(LearnerRepository learnerRepository,
                                PronunciationSessionRepository pronunciationSessionRepository,
                                LearnerIssueRepository learnerIssueRepository,
                                ProfileRepository profileRepository,
                                LearnerService learnerService
    ) {
        this.learnerRepository = learnerRepository;
        this.profileRepository = profileRepository;
        this.sessionRepository = pronunciationSessionRepository;
        this.issueRepository = learnerIssueRepository;
        this.learnerService = learnerService;
    }

    public Profile updateLearnerProfile(Profile profile, Learner learner, String sessionId) {
        log.info("[{}] saving updated user profile data", sessionId);
        profile.setLearner(learner);

        Profile savedProfile = profileRepository.save(profile);

        learnerService.generateInitialExamination(savedProfile, learner, sessionId);

        return savedProfile;

    }


    @Transactional
    public void saveSession(Learner learner, String targetText, String ipa, double accuracyScore, List<Issue> issues, String sessionId) {
        PronunciationSession session = PronunciationSession.builder()
                .learner(learner)
                .targetText(targetText)
                .transcribedIPA(ipa)
                .accuracyScore(accuracyScore)
                .sessionDate(LocalDateTime.now())
                .build();
        log.info("[{}] building up pronunciation session : {}", sessionId, session);

        session = sessionRepository.save(session);

        for (Issue issue: issues) {
            issue.setSession(session);
        }
        session.setIssues(issues);

        log.info("[{}] updating user activity ", sessionId);

        learner.setLastActivityAt(LocalDateTime.now());
        learnerRepository.save(learner);

        this.updateLearnerIssues(learner, issues);

        sessionRepository.save(session);
    }


    @Transactional
    public void updateLearnerIssues(Learner learner, List<Issue> newIssues) {
        LocalDateTime now = LocalDateTime.now();

        for(Issue detectedIssue: newIssues) {
            String issueKey = detectedIssue.getIssueType() + ":" + detectedIssue.getPhonetic();

            Optional<LearnerIssue> existingIssue = issueRepository.findByLearnerAndIssueTypeAndDescription(learner, detectedIssue.getIssueType(), issueKey);

            if (existingIssue.isPresent()) {
                LearnerIssue issue = existingIssue.get();
                issue.setOccurrenceCount(issue.getOccurrenceCount() + 1);
                issue.setLastDetected(now);
                issue.setResolved(false);
                issueRepository.save(issue);
            } else {
                LearnerIssue newIssue = LearnerIssue.builder()
                        .learner(learner)
                        .issueType(detectedIssue.getIssueType())
                        .description(issueKey)
                        .occurrenceCount(1)
                        .firstDetected(now)
                        .lastDetected(now)
                        .resolved(false)
                        .build();

                issueRepository.save(newIssue);
            }
        }
    }

    @Transactional
    protected void checkForResolvedIssues(Learner learner) {
        List<PronunciationSession> recentSessions = sessionRepository.findByLearnerAndSessionDateGreaterThanEqualOrderBySessionDateDesc(learner, LocalDateTime.now().minusDays(7));
        if (recentSessions.size() >= 5) {
            List<String> recentIssueDescriptions = recentSessions.stream()
                    .flatMap(s -> s.getIssues().stream())
                    .map( i -> i.getIssueType() + ":" + i.getPhonetic())
                    .distinct()
                    .toList();

            List<LearnerIssue> allIssues = issueRepository.findByLearnerAndResolvedFalseOrderByOccurrenceCountDesc(learner);


            for (LearnerIssue issue: allIssues) {
                if (!recentIssueDescriptions.contains(issue.getDescription())) {
                    issue.setResolved(true);
                    issueRepository.save(issue);
                }
            }
        }
    }

    public List<LearnerIssue> getRecurringIssues(Learner learner) {
        return issueRepository.findByLearnerAndResolvedFalseOrderByOccurrenceCountDesc(learner);
    }

    public List<LearnerIssue> getImprovedAreas(Learner learner) {
        return issueRepository.findByLearnerAndResolvedTrueOrderByLastDetectedDesc(learner);
    }


    public Map<String, Number> getLearnerStatistics(Learner learner) {
        List<PronunciationSession> allSessions = sessionRepository.findByLearnerOrderBySessionDateDesc(learner);
        Double avgAccuracy = sessionRepository.getAverageAccuracy(learner);

        double progress = 0.0;

        if (allSessions.size() >= 2) {
            double firstFiveAvg = allSessions.stream()
                    .skip(Math.max(0, allSessions.size() - 5))
                    .mapToDouble(PronunciationSession::getAccuracyScore)
                    .average()
                    .orElse(0.0);

            double lastFiveAvg = allSessions.stream()
                    .limit(5)
                    .mapToDouble(PronunciationSession::getAccuracyScore)
                    .average()
                    .orElse(0.0);

            progress = ((lastFiveAvg - firstFiveAvg) / firstFiveAvg ) * 100;
        }

        return Map.of(
                "totalSessions", allSessions.size(),
                "averageAccuracy", avgAccuracy != null ? avgAccuracy : 0.0,
                "progress", progress
        );
    }

}