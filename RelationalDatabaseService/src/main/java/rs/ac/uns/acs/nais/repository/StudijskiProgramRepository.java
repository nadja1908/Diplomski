package rs.ac.uns.acs.nais.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.ac.uns.acs.nais.domain.StudijskiProgram;

import java.util.List;
import java.util.Optional;

public interface StudijskiProgramRepository extends JpaRepository<StudijskiProgram, Long> {

    @Query("""
            SELECT p FROM StudijskiProgram p
            WHERE p.katedra.id = :katedraId
            ORDER BY p.sifra
            """)
    List<StudijskiProgram> findByKatedraId(@Param("katedraId") long katedraId);

    @Query("""
            SELECT p FROM StudijskiProgram p
            WHERE p.id = :id AND p.katedra.id = :katedraId
            """)
    Optional<StudijskiProgram> findByIdAndKatedraId(@Param("id") long id, @Param("katedraId") long katedraId);
}
