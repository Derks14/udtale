package udtale.models;

import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;


@Document
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class Learner extends BaseDocument implements UserDetails {

    private String firstname;
    private String lastname;
    private String username;

    @Pattern( regexp = "^(?:\\+?61|0)4(?:[ -]?[0-9]){8}$", message = "Phone number is invalid")
    private String phone;

    @Indexed(unique = true)
    private String email;

    @Getter(value = AccessLevel.NONE)
    private String password;

    public String fullName() {
        return this.getFirstname() + " " + this.getLastname();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("USER"));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
