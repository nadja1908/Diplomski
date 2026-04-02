package rs.ac.uns.acs.nais.web.dto;

public record AuthMeResponse(
        long id,
        String email,
        String ime,
        String prezime,
        String uloga
) {
}
