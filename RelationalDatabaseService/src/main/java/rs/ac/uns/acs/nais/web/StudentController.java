package rs.ac.uns.acs.nais.web;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.uns.acs.nais.service.AcademicQueryService;

@RestController
@RequestMapping("/api/student/me")
@PreAuthorize("hasAuthority('ROLE_STUDENT')")
@RequiredArgsConstructor
public class StudentController {

    private final AcademicQueryService academicQueryService;

    @GetMapping("/profile")
    public AcademicQueryService.StudentProfileDto profile(@AuthenticationPrincipal Long korisnikId) {
        return academicQueryService.studentProfile(korisnikId);
    }

    @GetMapping("/subjects-grades")
    public Object subjectsGrades(@AuthenticationPrincipal Long korisnikId) {
        return academicQueryService.subjectsAndGrades(korisnikId);
    }

    @GetMapping("/gpa")
    public AcademicQueryService.GpaDto gpa(@AuthenticationPrincipal Long korisnikId) {
        return academicQueryService.gpa(korisnikId);
    }
}
