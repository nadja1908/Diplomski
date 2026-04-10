package rs.ac.uns.acs.nais.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import rs.ac.uns.acs.nais.service.AssistantService;
import rs.ac.uns.acs.nais.web.dto.AssistantRequest;
import rs.ac.uns.acs.nais.web.dto.AssistantResponse;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/assistant")
@PreAuthorize("hasRole('STUDENT')")
@RequiredArgsConstructor
public class AssistantController {

    private final AssistantService assistantService;

    @PostMapping("/query")
    public AssistantResponse query(
            @AuthenticationPrincipal Long korisnikId,
            @Valid @RequestBody AssistantRequest request
    ) {
        try {
            return assistantService.answerForStudent(request.question(), korisnikId);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Assistant query failed", e);
            String detail = e.getMessage();
            if (detail == null || detail.isBlank()) {
                detail = e.getClass().getSimpleName();
            }
            if (detail.length() > 380) {
                detail = detail.substring(0, 377) + "...";
            }
            return new AssistantResponse(
                    "Asistent je naišao na grešku. Možeš probati ponovo za trenutak. "
                            + "Ako se ponavlja, proveri da li su podignuti PostgreSQL, Qdrant i vector-database-service. "
                            + "Tehnički detalj: "
                            + detail,
                    List.of(),
                    "Greška u asistentu");
        }
    }
}
