package rs.ac.uns.acs.nais.web.dto;

public record LoginResponse(
        String token,
        String role,
        String ime,
        String prezime,
        String email
) {
}
