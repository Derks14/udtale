package udtale.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class HomeController {

    @GetMapping
    public ResponseEntity<String> home(HttpServletRequest request) {
        String sessionId = request.getSession().getId();
        log.info("[{}] testing application uptime 1, 2, 3 ....", sessionId);

        return ResponseEntity.ok("Welcome to udtale ...");
    }
}
