package udtale.auth;

import lombok.Builder;
import lombok.Value;
import udtale.models.Learner;

@Builder
@Value
public class AuthResponse {
    Learner learner;
    String accessToken;

}
