package rs.ac.uns.acs.nais.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sef_katedre")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SefKatedre {

    @Id
    @Column(name = "korisnik_id")
    private Long korisnikId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "korisnik_id")
    private Korisnik korisnik;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "katedra_id")
    private Katedra katedra;
}
