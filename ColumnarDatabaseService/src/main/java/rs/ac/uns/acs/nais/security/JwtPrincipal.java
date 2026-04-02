package rs.ac.uns.acs.nais.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class JwtPrincipal implements UserDetails {

    private final Long korisnikId;
    private final Collection<? extends GrantedAuthority> authorities;

    public JwtPrincipal(Long korisnikId, String uloga) {
        this.korisnikId = korisnikId;
        String u = uloga != null ? uloga.trim() : "";
        if (u.startsWith("ROLE_")) {
            u = u.substring(5);
        }
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + u));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return String.valueOf(korisnikId);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
