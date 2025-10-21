package udtale.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import udtale.models.Learner;
import udtale.security.JwtService;

@Slf4j
@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthenticationService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthResponse authenticate(AuthCredentials authCredentials, String sessionId) {
        Authentication authentication;
        try {
             authentication = new UsernamePasswordAuthenticationToken(authCredentials.getUsername(), authCredentials.getPassword());
            log.info("[{}] generating user authentication token: ", sessionId);

            boolean isAuthenticated  = authenticationManager.authenticate(authentication).isAuthenticated();

            if (isAuthenticated) {
                log.info("[{}] user authentication successful. [user={}]", sessionId, authentication.getPrincipal());

                String token = jwtService.generateJWTToken(authCredentials.getUsername());

                return AuthResponse.builder().accessToken(token).learner((Learner) authentication.getPrincipal()).build();
            }

        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username or password might be wrong or doesnt exists");
        }
        return null;
    }

    public AuthResponse register(RegistrationDetails details, String sessionId) {
        log.info("[{}]", sessionId);
        return AuthResponse.builder().build();
    }



}
