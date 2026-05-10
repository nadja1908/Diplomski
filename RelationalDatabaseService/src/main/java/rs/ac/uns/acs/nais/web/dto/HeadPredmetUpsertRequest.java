package rs.ac.uns.acs.nais.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record HeadPredmetUpsertRequest(
        @NotBlank @Size(max = 20) String sifra,
        @NotBlank @Size(max = 255) String naziv,
        @NotNull @Positive Integer espb,
        @NotNull Long studijskiProgramId,
        @NotNull @Min(1) @Max(4) Integer kurikulumGodina,
        @NotNull @Min(1) @Max(2) Integer kurikulumSemestar,
        String kratakOpis,
        String cilj,
        String ishodiUcenja,
        String metodeNastave,
        String temeKursa
) {
}
