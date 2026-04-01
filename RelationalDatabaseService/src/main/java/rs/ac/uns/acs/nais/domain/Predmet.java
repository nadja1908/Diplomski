package rs.ac.uns.acs.nais.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "predmet")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Predmet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String sifra;

    @Column(nullable = false)
    private String naziv;

    @Column(nullable = false)
    private Integer espb;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "studijski_program_id")
    private StudijskiProgram studijskiProgram;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "katedra_id")
    private Katedra katedra;

    @Column(name = "kratak_opis", columnDefinition = "TEXT")
    private String kratakOpis;
}
