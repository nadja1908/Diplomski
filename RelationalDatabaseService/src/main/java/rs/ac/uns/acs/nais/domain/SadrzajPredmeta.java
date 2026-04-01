package rs.ac.uns.acs.nais.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sadrzaj_predmeta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SadrzajPredmeta {

    @Id
    @Column(name = "predmet_id")
    private Long predmetId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "predmet_id")
    private Predmet predmet;

    @Column(columnDefinition = "TEXT")
    private String cilj;

    @Column(name = "ishodi_ucenja", columnDefinition = "TEXT")
    private String ishodiUcenja;

    @Column(name = "metode_nastave", columnDefinition = "TEXT")
    private String metodeNastave;

    @Column(name = "teme_kursa", columnDefinition = "TEXT")
    private String temeKursa;
}
