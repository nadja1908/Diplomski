package rs.ac.uns.acs.nais.web.dto;

import jakarta.validation.constraints.NotBlank;

public record AssistantRequest(@NotBlank String question) {
}
