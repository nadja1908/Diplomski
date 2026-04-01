package rs.ac.uns.acs.nais.web;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.uns.acs.nais.domain.Korisnik;
import rs.ac.uns.acs.nais.service.AcademicQueryService;

@RestController
@RequestMapping("/api/head")
@PreAuthorize("hasAuthority('ROLE_SEF_KATEDRE')")
@RequiredArgsConstructor
public class HeadController {

    private final AcademicQueryService academicQueryService;

    @GetMapping("/students")
    public Object students(@AuthenticationPrincipal Korisnik korisnik) {
        return academicQueryService.studentsForHead(korisnik);
    }
}
