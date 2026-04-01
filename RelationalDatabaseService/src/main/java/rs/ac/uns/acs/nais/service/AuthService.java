package rs.ac.uns.acs.nais.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import rs.ac.uns.acs.nais.domain.Korisnik;
import rs.ac.uns.acs.nais.repository.KorisnikRepository;
import rs.ac.uns.acs.nais.security.JwtService;
import rs.ac.uns.acs.nais.web.dto.LoginResponse;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final KorisnikRepository korisnikRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(String email, String password) {
        Korisnik k = korisnikRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Neispravni podaci za prijavu"));
        if (!passwordEncoder.matches(password, k.getLozinkaHash())) {
            throw new IllegalArgumentException("Neispravni podaci za prijavu");
        }
        String token = jwtService.generateToken(k);
        return new LoginResponse(
                token,
                k.getUloga().name(),
                k.getIme(),
                k.getPrezime(),
                k.getEmail()
        );
    }
}
