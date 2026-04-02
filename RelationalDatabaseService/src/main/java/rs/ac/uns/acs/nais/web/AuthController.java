package rs.ac.uns.acs.nais.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import rs.ac.uns.acs.nais.repository.KorisnikRepository;
import rs.ac.uns.acs.nais.service.AuthService;
import rs.ac.uns.acs.nais.web.dto.AuthMeResponse;
import rs.ac.uns.acs.nais.web.dto.LoginRequest;
import rs.ac.uns.acs.nais.web.dto.LoginResponse;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final KorisnikRepository korisnikRepository;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(authService.login(request.email(), request.password()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /** Ko si po JWT + bazi (za dijagnostiku kad head API vrati 403). */
    @GetMapping("/me")
    public AuthMeResponse me(@AuthenticationPrincipal Long korisnikId) {
        if (korisnikId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return korisnikRepository.findById(korisnikId)
                .map(k -> new AuthMeResponse(
                        k.getId(),
                        k.getEmail(),
                        k.getIme(),
                        k.getPrezime(),
                        k.getUloga().name()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
