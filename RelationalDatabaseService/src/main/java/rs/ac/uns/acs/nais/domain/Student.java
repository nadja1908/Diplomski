package rs.ac.uns.acs.nais.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "student")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "korisnik_id", unique = true)
    private Korisnik korisnik;

    @Column(name = "broj_indeksa", nullable = false, unique = true, length = 30)
    private String brojIndeksa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "studijski_program_id")
    private StudijskiProgram studijskiProgram;

    @Column(name = "godina_upisa", nullable = false)
    private Integer godinaUpisa;
}
