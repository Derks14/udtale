package udtale.controllers;


import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import udtale.dto.PronunciationCheckResponse;
import udtale.models.Feedback;
import udtale.models.Learner;
import udtale.services.PronunciationService;

import javax.print.attribute.standard.Media;

@RestController
@Slf4j
@RequestMapping("/api/pronunciation")
public class PronunciationController {

    private final PronunciationService pronunciationService;

    public PronunciationController(PronunciationService pronunciationService) {
        this.pronunciationService = pronunciationService;
    }

    @PostMapping(value = "/analyse",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Feedback> analyse( @RequestPart("audio") MultipartFile audio,
                                           @RequestPart("targetText") String targetText,
                                           @AuthenticationPrincipal Learner learner,
                                           HttpServletRequest request
    ) {
        String sessionId = request.getSession().getId();
        log.info("[{}] {} requested to check pronunciation score for audio file", learner,  sessionId);
        Feedback feedback = this.pronunciationService.analysePronunciation(audio, targetText, learner,  sessionId);

        return ResponseEntity.ok(feedback);
    }


}
