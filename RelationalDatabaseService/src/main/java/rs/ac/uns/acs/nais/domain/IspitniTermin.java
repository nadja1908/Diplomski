package rs.ac.uns.acs.nais.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "ispitni_termin")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IspitniTermin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ispitni_rok_id")
    private IspitniRok ispitniRok;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "predmet_id")
    private Predmet predmet;

    @Column(name = "datum_vreme", nullable = false)
    private Instant datumVreme;

    @Column(length = 50)
    private String sala;
}
