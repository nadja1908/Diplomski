package rs.ac.uns.acs.nais.web;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.uns.acs.nais.service.AcademicQueryService;

@RestController
@RequestMapping("/api/head")
@RequiredArgsConstructor
public class HeadController {

    private final AcademicQueryService academicQueryService;

    /**
     * Bez parametra: lista studijskih programa katedre + studenti (radi i kroz stariji gateway koji nema /api/head/programs).
     * Sa programPregledId: pun pregled jednog programa (isti odgovor kao /api/head/program/{id}/pregled).
     */
    @GetMapping("/students")
    public Object students(
            @AuthenticationPrincipal Long korisnikId,
            @RequestParam(value = "programPregledId", required = false) Long programPregledId,
            @RequestParam(value = "statistikaGodinaUpisa", required = false) Integer statistikaGodinaUpisa,
            @RequestParam(value = "statistikaCeoProgram", required = false) Boolean statistikaCeoProgram) {
        if (programPregledId != null) {
            return academicQueryService.programPregledForHead(
                    korisnikId, programPregledId, statistikaGodinaUpisa, statistikaCeoProgram);
        }
        return academicQueryService.headStudentsBundle(korisnikId);
    }

    @GetMapping("/programs")
    public Object studyPrograms(@AuthenticationPrincipal Long korisnikId) {
        return academicQueryService.studyProgramsForHead(korisnikId);
    }

    @GetMapping("/program/{programId}/pregled")
    public Object programPregled(
            @AuthenticationPrincipal Long korisnikId,
            @PathVariable long programId,
            @RequestParam(value = "statistikaGodinaUpisa", required = false) Integer statistikaGodinaUpisa,
            @RequestParam(value = "statistikaCeoProgram", required = false) Boolean statistikaCeoProgram) {
        return academicQueryService.programPregledForHead(
                korisnikId, programId, statistikaGodinaUpisa, statistikaCeoProgram);
    }
}
