package udtale.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

@Value
@Builder
@Getter
@Setter
public class RegistrationDetails {

    @NotBlank
    String firstname;

    @NotBlank
    String lastname;

    @Email
    String email;

    @Pattern( regexp = "^(?:\\+?61|0)4(?:[ -]?[0-9]){8}$", message = "Phone number is invalid")
    String phone;

    @Pattern( regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$", message = "Password.( digit, lowercase, upper, nospace, symbol)")
    String password;

}
