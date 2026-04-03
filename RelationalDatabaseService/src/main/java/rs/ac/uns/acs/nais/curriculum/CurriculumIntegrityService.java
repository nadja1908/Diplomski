package rs.ac.uns.acs.nais.curriculum;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import rs.ac.uns.acs.nais.domain.Predmet;
import rs.ac.uns.acs.nais.repository.PredmetRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Odbija podatke koji krše pragove kurikuluma (po programu, godini i semestru).
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
@RequiredArgsConstructor
public class CurriculumIntegrityService implements ApplicationRunner {

    private final PredmetRepository predmetRepository;

    @Override
    public void run(ApplicationArguments args) {
        validateAllOrThrow();
    }

    public void validateAllOrThrow() {
        List<Predmet> all = predmetRepository.findAll();
        Map<Long, Integer> perProgram = new HashMap<>();
        Map<String, Integer> perYear = new HashMap<>();
        Map<String, Integer> perSem = new HashMap<>();

        for (Predmet p : all) {
            long pid = p.getStudijskiProgram().getId();
            int y = p.getKurikulumGodina();
            int s = p.getKurikulumSemestar();
            if (y < 1 || y > 4 || s < 1 || s > 2) {
                throw new IllegalStateException(
                        "Predmet id=" + p.getId() + " ima neispravan kurikulum: godina=" + y + ", semestar=" + s);
            }
            perProgram.merge(pid, 1, Integer::sum);
            perYear.merge(pid + ":" + y, 1, Integer::sum);
            perSem.merge(pid + ":" + y + ":" + s, 1, Integer::sum);
        }

        for (Map.Entry<Long, Integer> e : perProgram.entrySet()) {
            if (e.getValue() > CurriculumConstraints.MAX_SUBJECTS_PER_PROGRAM) {
                throw new IllegalStateException(
                        "Studijski program " + e.getKey() + " ima " + e.getValue() + " predmeta (maksimum "
                                + CurriculumConstraints.MAX_SUBJECTS_PER_PROGRAM + ").");
            }
        }
        for (Map.Entry<String, Integer> e : perYear.entrySet()) {
            if (e.getValue() > CurriculumConstraints.MAX_SUBJECTS_PER_CURRICULUM_YEAR) {
                throw new IllegalStateException(
                        "Program / godina kurikuluma " + e.getKey() + ": " + e.getValue() + " predmeta (maksimum "
                                + CurriculumConstraints.MAX_SUBJECTS_PER_CURRICULUM_YEAR + ").");
            }
        }
        for (Map.Entry<String, Integer> e : perSem.entrySet()) {
            if (e.getValue() > CurriculumConstraints.MAX_SUBJECTS_PER_CURRICULUM_SEMESTER) {
                throw new IllegalStateException(
                        "Program / godina / semestar " + e.getKey() + ": " + e.getValue() + " predmeta (maksimum "
                                + CurriculumConstraints.MAX_SUBJECTS_PER_CURRICULUM_SEMESTER + ").");
            }
        }
    }

    /**
     * Za REST / buduće mutacije kurikuluma: provera jednog programa.
     */
    public void validateProgramStateOrThrow(long studijskiProgramId, List<Predmet> subjectsOnProgram) {
        Objects.requireNonNull(subjectsOnProgram, "subjectsOnProgram");
        if (subjectsOnProgram.size() > CurriculumConstraints.MAX_SUBJECTS_PER_PROGRAM) {
            throw new IllegalStateException("Previše predmeta na programu " + studijskiProgramId + ".");
        }
        int[] year = new int[5];
        int[][] ysem = new int[5][3];
        for (Predmet p : subjectsOnProgram) {
            int y = p.getKurikulumGodina();
            int s = p.getKurikulumSemestar();
            year[y]++;
            ysem[y][s]++;
        }
        for (int y = 1; y <= 4; y++) {
            if (year[y] > CurriculumConstraints.MAX_SUBJECTS_PER_CURRICULUM_YEAR) {
                throw new IllegalStateException("Godina " + y + " kurikuluma preopterećena na programu "
                        + studijskiProgramId + ".");
            }
            for (int s = 1; s <= 2; s++) {
                if (ysem[y][s] > CurriculumConstraints.MAX_SUBJECTS_PER_CURRICULUM_SEMESTER) {
                    throw new IllegalStateException("Semestar " + s + " u " + y + ". godini preopterećen na programu "
                            + studijskiProgramId + ".");
                }
            }
        }
    }
}
