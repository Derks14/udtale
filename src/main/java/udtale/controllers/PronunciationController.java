package udtale.controllers;


import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import udtale.dto.PronunciationCheckResponse;
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


    @PostMapping(value = "/check",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PronunciationCheckResponse> check(
            @RequestPart("audio") MultipartFile audio,
            @RequestPart("targetText") String targetText,
            HttpServletRequest request) {
        String sessionId = request.getSession().getId();
        log.info("[{}] request to check pronunciation score for audio file", sessionId);
        PronunciationCheckResponse response = this.pronunciationService.check(audio, targetText, sessionId);

        return ResponseEntity.ok(null);
    }


}
