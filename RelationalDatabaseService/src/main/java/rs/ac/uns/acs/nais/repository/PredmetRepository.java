package rs.ac.uns.acs.nais.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.ac.uns.acs.nais.domain.Predmet;

import java.util.List;

public interface PredmetRepository extends JpaRepository<Predmet, Long> {

    @Query("""
            SELECT p FROM Predmet p
            WHERE p.katedra.id = :katedraId
            ORDER BY p.sifra
            """)
    List<Predmet> findByKatedraId(@Param("katedraId") Long katedraId);

    @Query("SELECT p.id FROM Predmet p WHERE p.katedra.id = :katedraId ORDER BY p.sifra")
    List<Long> findIdsByKatedraId(@Param("katedraId") Long katedraId);

    @Query("SELECT p.id FROM Predmet p WHERE p.studijskiProgram.id = :programId ORDER BY p.sifra")
    List<Long> findIdsByStudijskiProgramId(@Param("programId") Long programId);

    @Query("SELECT p FROM Predmet p WHERE p.studijskiProgram.id = :programId ORDER BY p.sifra ASC")
    List<Predmet> findAllByStudijskiProgramIdOrderBySifraAsc(@Param("programId") Long programId);

    @Query("""
            SELECT p FROM Predmet p
            WHERE p.studijskiProgram.id = :programId
            ORDER BY p.kurikulumGodina ASC, p.kurikulumSemestar ASC, p.sifra ASC
            """)
    List<Predmet> findAllByStudijskiProgramIdOrderByKurikulumSifra(@Param("programId") Long programId);

    long countByStudijskiProgram_Id(Long programId);
}
