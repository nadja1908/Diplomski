package rs.ac.uns.acs.nais.web.dto;

public record HeadPredmetDetailResponse(
        long id,
        String sifra,
        String naziv,
        int espb,
        long studijskiProgramId,
        String studijskiProgramSifra,
        String studijskiProgramNaziv,
        String kratakOpis,
        int kurikulumGodina,
        int kurikulumSemestar,
        String cilj,
        String ishodiUcenja,
        String metodeNastave,
        String temeKursa
) {
}
