package udtale.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import udtale.models.Learner;
import udtale.repositories.LearnerRepository;

@Service
@Slf4j
public class LeanerUserDetailService implements UserDetailsService {

    private final LearnerRepository learnerRepository;

    public LeanerUserDetailService(LearnerRepository learnerRepository) {
        this.learnerRepository = learnerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return learnerRepository.findUserByUsername(username)
                .orElseThrow( () -> {
                    log.error("User with username/email {} cannot be found ", username);
                    return new UsernameNotFoundException("Cannot find user");
                });
    }
}
