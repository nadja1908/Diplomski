package rs.ac.uns.acs.nais.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.uns.acs.nais.service.AssistantService;
import rs.ac.uns.acs.nais.web.dto.AssistantRequest;
import rs.ac.uns.acs.nais.web.dto.AssistantResponse;

@RestController
@RequestMapping("/api/assistant")
@PreAuthorize("hasAuthority('ROLE_STUDENT')")
@RequiredArgsConstructor
public class AssistantController {

    private final AssistantService assistantService;

    @PostMapping("/query")
    public AssistantResponse query(
            @AuthenticationPrincipal Long korisnikId,
            @Valid @RequestBody AssistantRequest request
    ) {
        return assistantService.answerForStudent(request.question(), korisnikId);
    }
}
