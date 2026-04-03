package rs.ac.uns.acs.nais.curriculum;

/**
 * Hard caps za nastavni plan u demo/operativnom modelu (usklađeno sa seed skriptama).
 */
public final class CurriculumConstraints {

    public static final int MAX_SUBJECTS_PER_PROGRAM = 45;
    public static final int MAX_SUBJECTS_PER_CURRICULUM_YEAR = 12;
    public static final int MAX_SUBJECTS_PER_CURRICULUM_SEMESTER = 6;

    private CurriculumConstraints() {
    }
}
