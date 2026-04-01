package rs.ac.uns.acs.nais.web.dto;

import java.util.List;

public record AssistantResponse(
        String answer,
        List<String> sources
) {
}
