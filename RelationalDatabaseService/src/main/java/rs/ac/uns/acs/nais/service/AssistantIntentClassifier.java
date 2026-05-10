package rs.ac.uns.acs.nais.service;

import org.springframework.stereotype.Component;

@Component
public class AssistantIntentClassifier {

    public AssistantIntentDecision classify(String question, String foldedQuestion) {
        String f = foldedQuestion == null ? "" : foldedQuestion;
        if (f.isBlank()) {
            return AssistantIntentDecision.of(
                    AssistantIntentType.UNKNOWN,
                    0.0,
                    true,
                    false,
                    false,
                    "Prazno pitanje"
            );
        }

        if (looksLikeCurriculumList(f)) {
            return AssistantIntentDecision.of(
                    AssistantIntentType.CURRICULUM_LIST,
                    0.98,
                    true,
                    false,
                    false,
                    "Eksplicitan zahtev za listu/spisak predmeta"
            );
        }

        if (looksLikeStatisticsRanking(f)) {
            return AssistantIntentDecision.of(
                    AssistantIntentType.STATISTICS_RANKING,
                    0.95,
                    false,
                    true,
                    false,
                    "Rang/prolaznost/najtezi-najlaksi"
            );
        }

        if (looksLikeSubjectDetail(f)) {
            return AssistantIntentDecision.of(
                    AssistantIntentType.SUBJECT_DETAIL,
                    0.92,
                    true,
                    false,
                    true,
                    "Detalji jednog predmeta (sadrzaj/cilj/ishodi/literatura)"
            );
        }

        if (looksLikeSubjectPresence(f)) {
            return AssistantIntentDecision.of(
                    AssistantIntentType.SUBJECT_PRESENCE,
                    0.9,
                    true,
                    false,
                    false,
                    "Pitanje da li predmet postoji u korisnikovom kurikulumu"
            );
        }

        if (looksLikeUnpassed(f)) {
            return AssistantIntentDecision.of(
                    AssistantIntentType.UNPASSED_SUBJECTS,
                    0.93,
                    true,
                    false,
                    false,
                    "Nepolozeni predmeti"
            );
        }

        if (looksLikeCurriculumRemainder(f)) {
            return AssistantIntentDecision.of(
                    AssistantIntentType.CURRICULUM_REMAINDER,
                    0.9,
                    true,
                    false,
                    false,
                    "Sta je ostalo od kurikuluma"
            );
        }

        if (looksLikeExamAttempts(f)) {
            return AssistantIntentDecision.of(
                    AssistantIntentType.EXAM_ATTEMPTS,
                    0.86,
                    true,
                    false,
                    false,
                    "Broj izlazaka na ispite"
            );
        }

        if (looksLikePassRokSummary(f)) {
            return AssistantIntentDecision.of(
                    AssistantIntentType.PASS_ROK_SUMMARY,
                    0.9,
                    true,
                    false,
                    false,
                    "U kom roku je polozen predmet"
            );
        }

        if (looksLikeStudentAll(f)) {
            return AssistantIntentDecision.of(
                    AssistantIntentType.STUDENT_ALL,
                    0.8,
                    true,
                    false,
                    false,
                    "Kompletni podaci o studentu"
            );
        }

        if (looksLikeHybridFilteredSemantic(f)) {
            return AssistantIntentDecision.of(
                    AssistantIntentType.HYBRID_FILTERED_SEMANTIC,
                    0.82,
                    true,
                    true,
                    true,
                    "Kombinacija filtera i semanticke pretrage"
            );
        }

        if (looksLikeSemanticText(f)) {
            return AssistantIntentDecision.of(
                    AssistantIntentType.SEMANTIC_TEXT_SEARCH,
                    0.78,
                    true,
                    false,
                    true,
                    "Semanticka pretraga termina po predmetima"
            );
        }

        return AssistantIntentDecision.of(
                AssistantIntentType.UNKNOWN,
                0.45,
                true,
                false,
                true,
                "Nedefinisano - fallback na postojecu pretragu"
        );
    }

    private static boolean looksLikeCurriculumList(String f) {
        if (!(f.contains("predmet") || f.contains("kurse") || f.contains("kurs"))) {
            return false;
        }
        if (f.contains("spisak") || f.contains("lista") || f.contains("nabro") || f.contains("navedi")) {
            return true;
        }
        if (f.contains("svi predmet") || f.contains("sve predmet") || f.contains("svi kurse")) {
            return true;
        }
        return f.contains("koliko predmeta");
    }

    private static boolean looksLikeStatisticsRanking(String f) {
        return f.contains("najtez") || f.contains("najlaks")
                || f.contains("najmanj") && f.contains("prolaz")
                || f.contains("najvec") && f.contains("prolaz")
                || f.contains("prolaznost")
                || (f.contains("najcesce") && f.contains("pad"));
    }

    private static boolean looksLikeSubjectDetail(String f) {
        return (f.contains("sadrzaj") || f.contains("cilj") || f.contains("ishod")
                || f.contains("literatur") || f.contains("kako se polaz") || f.contains("nacin polag"))
                && (f.contains("predmet") || f.contains("kurs") || f.contains("tog"));
    }

    private static boolean looksLikeSubjectPresence(String f) {
        boolean ask = f.contains("da li imam") || f.contains("imam li")
                || f.contains("jel imam") || f.contains("je l imam");
        if (!ask) {
            return false;
        }
        if (!(f.contains("predmet") || f.contains("kurs") || f.contains("kolegij"))) {
            return false;
        }
        return !(f.contains("spominj") || f.contains("pominj") || f.contains("sadrz")
                || f.contains("obuhvat") || f.contains("bavi") || f.contains("gde se") || f.contains("gdje se"));
    }

    private static boolean looksLikeSemanticText(String f) {
        return f.contains("spominj") || f.contains("pominj")
                || f.contains("gde se") || f.contains("gdje se")
                || f.contains("obradj") || f.contains("obraduj")
                || f.contains("sadrzi") || f.contains("sadrz")
                || f.contains("ima python") || f.contains("ima projekat")
                || f.contains("kolokvijum") || f.contains("laboratorij");
    }

    private static boolean looksLikeHybridFilteredSemantic(String f) {
        boolean semantic = looksLikeSemanticText(f);
        boolean filters = f.contains("nepoloz") || f.contains("prve godine") || f.contains("prva godina")
                || f.contains("najmanjom prolaznoscu") || f.contains("najtezi")
                || f.contains("najlaksi") || f.contains("mojih predmeta");
        return semantic && filters;
    }

    private static boolean looksLikeUnpassed(String f) {
        return (f.contains("nepoloz") || f.contains("nisam poloz") || f.contains("nemam polozen"))
                && f.contains("predmet");
    }

    private static boolean looksLikeCurriculumRemainder(String f) {
        return f.contains("sta mi je ostalo") || f.contains("sta mi fali")
                || f.contains("preostaje mi") || f.contains("koje predmete jos");
    }

    private static boolean looksLikeExamAttempts(String f) {
        boolean aboutCount = f.contains("koliko puta") || f.contains("broj izlazaka")
                || f.contains("koliko izlazaka");
        return aboutCount && (f.contains("ispit") || f.contains("izlazak"));
    }

    private static boolean looksLikePassRokSummary(String f) {
        return f.contains("u kom roku") || f.contains("ispitni rok")
                || f.contains("kad sam poloz") || f.contains("kada sam poloz");
    }

    private static boolean looksLikeStudentAll(String f) {
        return (f.contains("sta znas o meni") || f.contains("sta zna") || f.contains("sve o meni"))
                || (f.contains("profil") && f.contains("ocene") && f.contains("prosek"));
    }
}
