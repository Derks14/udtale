package udtale.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import udtale.models.Diagnostics;
import udtale.models.Learner;
import udtale.models.Profile;
import udtale.services.LearnerMemoryService;
import udtale.services.LearnerService;

@RestController
@Slf4j
@RequestMapping("/api/learner")
public class LearnerController {

    private final LearnerMemoryService learnerMemoryService;
    private final LearnerService learnerService;

    public LearnerController(LearnerMemoryService learnerMemoryService, LearnerService learnerService) {
        this.learnerMemoryService = learnerMemoryService;
        this.learnerService = learnerService;
    }

    @GetMapping("/diagnostics")
    public ResponseEntity<Diagnostics> diagnostics(@AuthenticationPrincipal Learner learner,
                                                   HttpServletRequest request) {
        String sessionId = request.getSession().getId();
        log.info("[{}] new request to generate diagnostics for learner: {}", sessionId, learner);
        Diagnostics diagnostics = learnerService.fetchDiagnostics(learner, sessionId);
        return ResponseEntity.ok(diagnostics);
    }

    @PostMapping("/profile")
    public ResponseEntity<Profile> saveLearnerProfile(@RequestBody Profile profile,
                                                     @AuthenticationPrincipal Learner learner,
                                                     HttpServletRequest request) {
        String sessionId = request.getSession().getId();
        log.info("[{}] session ", sessionId);

        Profile savedProfile = learnerMemoryService.updateLearnerProfile(profile, learner, sessionId);
        return ResponseEntity.ok(savedProfile);
    }

    @GetMapping("/profile")
    public ResponseEntity<Profile> fetchLearnerProfile(HttpServletRequest request) {
        String sessionId = request.getSession().getId();
        log.info("[{}] new request to get user profile data", sessionId);
        return ResponseEntity.ok(null);
    }



}
