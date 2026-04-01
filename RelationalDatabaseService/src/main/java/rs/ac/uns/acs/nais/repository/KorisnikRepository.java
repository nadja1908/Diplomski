package rs.ac.uns.acs.nais.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.uns.acs.nais.domain.Korisnik;

import java.util.Optional;

public interface KorisnikRepository extends JpaRepository<Korisnik, Long> {
    Optional<Korisnik> findByEmail(String email);
}
