package udtale.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthCredentials credentials, HttpServletRequest request) {
        String sessionId = request.getSession().getId();
        log.info("[{}] request to login user ", sessionId);
        AuthResponse authResponse = authenticationService.authenticate(credentials, sessionId);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegistrationDetails details, HttpServletRequest request) {
        String sessionId = request.getSession().getId();
        log.info("[{}] request to register new user", sessionId);
        AuthResponse response = authenticationService.register(details, sessionId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }




}
