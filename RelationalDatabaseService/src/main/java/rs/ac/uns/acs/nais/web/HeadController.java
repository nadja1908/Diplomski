package rs.ac.uns.acs.nais.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.uns.acs.nais.service.AcademicQueryService;
import rs.ac.uns.acs.nais.service.HeadPredmetManagementService;
import rs.ac.uns.acs.nais.web.dto.HeadPredmetUpsertRequest;

@RestController
@RequestMapping("/api/head")
@RequiredArgsConstructor
public class HeadController {

    private final AcademicQueryService academicQueryService;
    private final HeadPredmetManagementService headPredmetManagementService;

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

    @GetMapping("/subjects")
    public Object headSubjects(@AuthenticationPrincipal Long korisnikId) {
        return headPredmetManagementService.listForHead(korisnikId);
    }

    @PostMapping("/subjects")
    public Object createHeadSubject(
            @AuthenticationPrincipal Long korisnikId,
            @Valid @RequestBody HeadPredmetUpsertRequest body) {
        return headPredmetManagementService.create(korisnikId, body);
    }

    @PutMapping("/subjects/{id}")
    public Object updateHeadSubject(
            @AuthenticationPrincipal Long korisnikId,
            @PathVariable long id,
            @Valid @RequestBody HeadPredmetUpsertRequest body) {
        return headPredmetManagementService.update(korisnikId, id, body);
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