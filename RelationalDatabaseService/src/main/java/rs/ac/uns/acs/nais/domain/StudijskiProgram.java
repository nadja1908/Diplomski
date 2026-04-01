package rs.ac.uns.acs.nais.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "studijski_program")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudijskiProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String sifra;

    @Column(nullable = false)
    private String naziv;

    @Column(nullable = false, length = 50)
    private String stepen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "katedra_id")
    private Katedra katedra;
}
