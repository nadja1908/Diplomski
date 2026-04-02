package rs.ac.uns.acs.nais.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.ac.uns.acs.nais.domain.Ocena;

import java.util.List;

public interface OcenaRepository extends JpaRepository<Ocena, Long> {

    @Query("""
            SELECT o FROM Ocena o
            JOIN FETCH o.ispitniTermin t
            JOIN FETCH t.predmet p
            WHERE o.student.id = :studentId
            ORDER BY t.datumVreme DESC
            """)
    List<Ocena> findByStudentIdWithDetails(@Param("studentId") Long studentId);

    @Query("""
            SELECT o FROM Ocena o
            JOIN o.ispitniTermin t
            JOIN t.predmet p
            JOIN p.katedra k
            WHERE k.id = :katedraId
            """)
    List<Ocena> findByKatedraId(@Param("katedraId") Long katedraId);

    @Query("""
            SELECT DISTINCT p.id FROM Ocena o
            JOIN o.ispitniTermin t
            JOIN t.predmet p
            JOIN o.student s
            WHERE s.korisnik.id = :korisnikId
            """)
    List<Long> findDistinctPredmetIdsByStudentKorisnikId(@Param("korisnikId") Long korisnikId);

    @Query("""
            SELECT o FROM Ocena o
            JOIN FETCH o.student s
            JOIN FETCH o.ispitniTermin t
            JOIN FETCH t.predmet p
            WHERE s.studijskiProgram.id = :programId
            """)
    List<Ocena> findAllByStudijskiProgramId(@Param("programId") long programId);
}
