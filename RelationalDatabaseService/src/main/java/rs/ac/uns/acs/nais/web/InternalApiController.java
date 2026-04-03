package rs.ac.uns.acs.nais.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import rs.ac.uns.acs.nais.config.NaisProperties;
import rs.ac.uns.acs.nais.internal.dto.StatisticsAggregatesResponse;
import rs.ac.uns.acs.nais.internal.dto.StudentProgramPredmetMin;
import rs.ac.uns.acs.nais.academic.AcademicProgressionRules;
import rs.ac.uns.acs.nais.repository.OcenaRepository;
import rs.ac.uns.acs.nais.repository.PredmetRepository;
import rs.ac.uns.acs.nais.repository.SefKatedreRepository;
import rs.ac.uns.acs.nais.repository.StudentRepository;
import rs.ac.uns.acs.nais.service.InternalStatisticsService;

import java.util.List;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalApiController {

    private final NaisProperties naisProperties;
    private final InternalStatisticsService internalStatisticsService;
    private final OcenaRepository ocenaRepository;
    private final SefKatedreRepository sefKatedreRepository;
    private final PredmetRepository predmetRepository;
    private final StudentRepository studentRepository;

    private void verifyKey(String key) {
        String expected = naisProperties.getInternalApiSecret();
        if (expected == null || expected.isEmpty() || !expected.equals(key)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/statistics/aggregates")
    public StatisticsAggregatesResponse aggregates(@RequestHeader("X-Internal-Key") String key) {
        verifyKey(key);
        return internalStatisticsService.computeAggregates();
    }

    @GetMapping("/student/korisnik/{korisnikId}/predmet-ids")
    public List<Long> predmetIdsZaStudenta(
            @PathVariable Long korisnikId,
            @RequestHeader("X-Internal-Key") String key
    ) {
        verifyKey(key);
        var student = studentRepository.findByKorisnikId(korisnikId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student nije pronađen"));
        return ocenaRepository.findByStudentIdWithDetails(student.getId()).stream()
                .filter(o -> AcademicProgressionRules.ocenaJeDozvoljenaEvidencija(student, o))
                .map(o -> o.getIspitniTermin().getPredmet().getId())
                .distinct()
                .toList();
    }

    /**
     * Svi predmeti studijskog programa studenta (isti kurikulum), za Cassandrinu statistiku položeno/palo na nivou predmeta.
     */
    @GetMapping("/student/korisnik/{korisnikId}/program-predmeti")
    public List<StudentProgramPredmetMin> programPredmetiZaStudenta(
            @PathVariable Long korisnikId,
            @RequestHeader("X-Internal-Key") String key
    ) {
        verifyKey(key);
        var student = studentRepository.findByKorisnikId(korisnikId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student nije pronađen"));
        long programId = student.getStudijskiProgram().getId();
        return predmetRepository.findAllByStudijskiProgramIdOrderBySifraAsc(programId).stream()
                .map(p -> new StudentProgramPredmetMin(p.getId(), p.getSifra(), p.getNaziv()))
                .toList();
    }

    @GetMapping("/sef/korisnik/{korisnikId}/katedra-id")
    public Long katedraZaSefa(@PathVariable Long korisnikId, @RequestHeader("X-Internal-Key") String key) {
        verifyKey(key);
        return sefKatedreRepository.findByKorisnikIdWithKatedra(korisnikId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND))
                .getKatedra()
                .getId();
    }

    @GetMapping("/katedra/{katedraId}/predmet-ids")
    public List<Long> predmetIdsZaKatedru(@PathVariable Long katedraId, @RequestHeader("X-Internal-Key") String key) {
        verifyKey(key);
        return predmetRepository.findIdsByKatedraId(katedraId);
    }
}
