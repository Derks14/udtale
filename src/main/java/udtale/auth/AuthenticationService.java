package udtale.auth;

import com.mongodb.DuplicateKeyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import udtale.config.exceptions.EmailAlreadyExistsException;
import udtale.models.Learner;
import udtale.repositories.LearnerRepository;
import udtale.security.JwtService;

@Slf4j
@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final LearnerRepository learnerRepository;

    public AuthenticationService(AuthenticationManager authenticationManager, JwtService jwtService, LearnerRepository repository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.learnerRepository = repository;
    }

    public AuthResponse authenticate(AuthCredentials authCredentials, String sessionId) {
        Authentication authentication;
        try {
             authentication = new UsernamePasswordAuthenticationToken(authCredentials.getUsername(), authCredentials.getPassword());
            log.info("[{}] generating user authentication token: ", sessionId);

            boolean isAuthenticated  = authenticationManager.authenticate(authentication).isAuthenticated();

            if (isAuthenticated) {
//                update the authentication object again after user is successfully authenticated

                Learner learner = learnerRepository.findUserByUsername(authCredentials.getUsername())
                        .orElseThrow( () -> {
                            log.error("User with username/email {} cannot be found ", authCredentials.getUsername());
                            return new UsernameNotFoundException("Cannot find user");
                        } );

                log.info("[{}] user authentication successful. [user={}]", sessionId, authentication.getPrincipal());

                String token = jwtService.generateJWTToken(learner.getUsername());

                return AuthResponse.builder().accessToken(token).learner(learner).build();
            }

        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username or password might be wrong or doesnt exists");
        }
        return null;
    }

    public AuthResponse register(udtale.auth.RegistrationDetails details, String sessionId) {
        log.info("[{}] encoding new learner password ", sessionId);
        String encodedPassword = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8().encode(details.getPassword());

        Learner learner = Learner.builder().firstname(details.getFirstname()).lastname(details.getLastname())
                .phone(details.getPhone()).email(details.getEmail())
                .username(details.getEmail())
                .password(encodedPassword)
                .build();

        Learner savedLearner = null;
        try {
            log.info("[{}] creating new learner account ", sessionId);
            learnerRepository.save(learner);
        } catch (DuplicateKeyException e) {
            log.warn("Attempt to create learner with duplicate email: {}", details.getEmail());
            throw new EmailAlreadyExistsException("Email already registered: " + details.getEmail());
        }

        log.info("[{}] generating new JWT authentication token ", sessionId);
        String token = jwtService.generateJWTToken(learner.getUsername());

        return AuthResponse.builder().accessToken(token).learner(learner).build();
    }



}
