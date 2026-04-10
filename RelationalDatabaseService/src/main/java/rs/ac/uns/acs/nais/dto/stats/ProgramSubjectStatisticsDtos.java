package rs.ac.uns.acs.nais.dto.stats;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public final class ProgramSubjectStatisticsDtos {

    private ProgramSubjectStatisticsDtos() {
    }

    public record ProgramSummary(long id, String sifra, String naziv) {
    }

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

    public record UnpassedSubjectPassRateDto(
            @JsonProperty("subjectCode") String subjectCode,
            @JsonProperty("subjectName") String subjectName,
            @JsonProperty("kurikulumGodina") int kurikulumGodina,
            @JsonProperty("kurikulumSemestar") int kurikulumSemestar,
            @JsonProperty("passRate") Double passRate
    ) {
    }
}
