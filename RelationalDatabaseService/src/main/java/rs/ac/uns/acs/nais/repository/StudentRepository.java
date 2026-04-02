package rs.ac.uns.acs.nais.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.ac.uns.acs.nais.domain.Student;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    @Query("""
            SELECT s FROM Student s
            JOIN FETCH s.korisnik
            JOIN FETCH s.studijskiProgram p
            JOIN FETCH p.katedra
            WHERE s.korisnik.id = :korisnikId
            """)
    Optional<Student> findByKorisnikId(@Param("korisnikId") Long korisnikId);

    @Query("""
            SELECT DISTINCT s FROM Student s
            JOIN FETCH s.korisnik
            JOIN FETCH s.studijskiProgram p
            JOIN FETCH p.katedra k
            WHERE k.id = :katedraId
            ORDER BY s.brojIndeksa
            """)
    List<Student> findByKatedraId(@Param("katedraId") Long katedraId);

    @Query("""
            SELECT s FROM Student s
            JOIN FETCH s.korisnik
            WHERE s.studijskiProgram.id = :programId
            ORDER BY s.brojIndeksa
            """)
    List<Student> findByStudijskiProgramIdWithKorisnik(@Param("programId") long programId);
}
