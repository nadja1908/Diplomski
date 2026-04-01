package rs.ac.uns.acs.nais.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "korisnik")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Korisnik implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "lozinka_hash", nullable = false)
    private String lozinkaHash;

    @Column(nullable = false)
    private String ime;

    @Column(nullable = false)
    private String prezime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private KorisnikUloga uloga;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + uloga.name()));
    }

    @Override
    public String getPassword() {
        return lozinkaHash;
    }

    @Override
    public String getUsername() {
        return email;
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
