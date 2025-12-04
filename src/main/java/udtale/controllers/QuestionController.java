package udtale.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import udtale.models.Question;
import udtale.services.QuestionService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/question")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    public ResponseEntity<List<Question>> fetchQuestions(HttpServletRequest request) {
        String sessionId = request.getSession().getId();

        log.info("[{}] new request to get session ", sessionId);
        return  null;
    }
}
