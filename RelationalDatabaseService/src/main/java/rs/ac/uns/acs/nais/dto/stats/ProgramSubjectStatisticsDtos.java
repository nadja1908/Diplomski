package rs.ac.uns.acs.nais.dto.stats;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Statistika predmeta po studijskom programu, računata isključivo iz redova tabele {@code ocena}
 * za studente na tom programu. Generacije (upis) se agregiraju u jedan uzorak osim ako je zadata
 * konkretna godina upisa.
 */
public final class ProgramSubjectStatisticsDtos {

    private ProgramSubjectStatisticsDtos() {
    }

    public record ProgramSummary(long id, String sifra, String naziv) {
    }

    /**
     * Opcije za filtriranje uzorka ispred agregacije.
     */
    public record StatisticsQueryParams(
            Integer godinaUpisa,
            String skolskaGodina,
            Integer kurikulumGodina,
            Integer semestar,
            Long predmetId,
            boolean includeGenerationBreakdown
    ) {
        public static StatisticsQueryParams defaultAll() {
            return new StatisticsQueryParams(null, null, null, null, null, false);
        }
    }

    public record GenerationBreakdownRow(
            int godinaUpisa,
            long totalStudentsWhoTook,
            long totalStudentsWhoPassed,
            long totalStudentsWhoFailed,
            Double passRate,
            Double averagePassingGrade
    ) {
    }

    /**
     * Jedna stavka po predmetu na programu.
     * <ul>
     *   <li>{@code passRate} = passed / took * 100 (studenti sa bar jednim izlaskom / distinct studenti)</li>
     *   <li>Prosečna, medijana, min/max — samo vrednosti ocena &gt;= 6 (stvarni redovi u {@code ocena})</li>
     * </ul>
     */
    public record SubjectStatisticsRow(
            long subjectId,
            String subjectCode,
            String subjectName,
            int espb,
            int kurikulumGodina,
            int semestar,
            long totalStudentsWhoTook,
            long totalStudentsWhoPassed,
            long totalStudentsWhoFailed,
            Double passRate,
            Double averagePassingGrade,
            Double medianPassingGrade,
            Integer highestGrade,
            Integer lowestPassingGrade,
            long totalExamAttempts,
            List<GenerationBreakdownRow> generationBreakdown
    ) {
    }

    public record RankingsBundle(
            List<SubjectStatisticsRow> hardestByPassRate,
            List<SubjectStatisticsRow> easiestByPassRate,
            List<SubjectStatisticsRow> highestAveragePassingGrade,
            List<SubjectStatisticsRow> lowestAveragePassingGrade
    ) {
    }

    public record ProgramStatisticsResponse(
            ProgramSummary program,
            List<SubjectStatisticsRow> subjects,
            RankingsBundle rankings,
            String aggregationNote
    ) {
    }

    public record StatisticsFilterOptions(
            List<Integer> generacijeUpisa,
            List<String> skolskeGodine,
            List<SubjectPickerOption> predmeti
    ) {
        public record SubjectPickerOption(long id, String sifra, String naziv, int kurikulumGodina, int semestar) {
        }
    }

    /**
     * Studentski pregled: nepoloženi predmeti iz tekuće i svih prethodnih godina kurikuluma (bez budućih godina)
     * + stopa prolaznosti na smeru.
     * Eksplicitna JSON imena da frontend uvek dobije {@code kurikulumGodina} / {@code kurikulumSemestar}.
     */
    public record UnpassedSubjectPassRateDto(
            @JsonProperty("subjectCode") String subjectCode,
            @JsonProperty("subjectName") String subjectName,
            @JsonProperty("kurikulumGodina") int kurikulumGodina,
            @JsonProperty("kurikulumSemestar") int kurikulumSemestar,
            @JsonProperty("passRate") Double passRate
    ) {
    }
}
