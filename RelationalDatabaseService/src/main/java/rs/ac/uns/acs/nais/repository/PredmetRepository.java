package rs.ac.uns.acs.nais.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.ac.uns.acs.nais.domain.Predmet;

import java.util.List;
import java.util.Optional;

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

    /** Jedinstvenost šifre u celoj bazi; trim + zanemarivanje veličine slova. */
    @Query("""
            SELECT COUNT(p) FROM Predmet p
            WHERE LOWER(TRIM(p.sifra)) = LOWER(TRIM(:sifra))
            """)
    long countByNormalizedSifra(@Param("sifra") String sifra);

    /** Jedinstvenost naziva u celoj bazi (trim + case-insensitive). */
    @Query("""
            SELECT COUNT(p) FROM Predmet p
            WHERE LOWER(TRIM(p.naziv)) = LOWER(TRIM(:naziv))
            """)
    long countByNormalizedNaziv(@Param("naziv") String naziv);

    @Query("""
            SELECT COUNT(p) FROM Predmet p
            WHERE LOWER(TRIM(p.naziv)) = LOWER(TRIM(:naziv))
            AND p.id <> :excludePredmetId
            """)
    long countByNormalizedNazivExcludingPredmet(
            @Param("naziv") String naziv,
            @Param("excludePredmetId") long excludePredmetId);

    @Query("""
            SELECT p FROM Predmet p
            JOIN FETCH p.studijskiProgram
            WHERE p.id = :id AND p.katedra.id = :katedraId
            """)
    Optional<Predmet> findByIdAndKatedraIdFetchProgram(
            @Param("id") long id, @Param("katedraId") long katedraId);
}