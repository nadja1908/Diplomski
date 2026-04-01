package rs.ac.uns.acs.nais.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.ac.uns.acs.nais.domain.SefKatedre;

import java.util.Optional;

public interface SefKatedreRepository extends JpaRepository<SefKatedre, Long> {
    Optional<SefKatedre> findByKorisnikId(Long korisnikId);

    @Query("""
            SELECT s FROM SefKatedre s
            JOIN FETCH s.katedra
            WHERE s.korisnikId = :korisnikId
            """)
    Optional<SefKatedre> findByKorisnikIdWithKatedra(@Param("korisnikId") Long korisnikId);
}
