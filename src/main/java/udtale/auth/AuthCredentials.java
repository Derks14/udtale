package udtale.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

@AllArgsConstructor
@Value
public class AuthCredentials {
    @Getter
    @NotBlank
    String email;

    @Pattern( regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$", message = "Password.( digit, lowercase, upper, nospace, symbol)")
    String password;


    String getUsername() {
        return this.email;
    }
}
