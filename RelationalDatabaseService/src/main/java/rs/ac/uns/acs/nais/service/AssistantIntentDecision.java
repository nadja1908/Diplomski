package rs.ac.uns.acs.nais.service;

public record AssistantIntentDecision(
        AssistantIntentType type,
        double confidence,
        boolean useRelational,
        boolean useColumnar,
        boolean useVector,
        String rationale
) {
    public static AssistantIntentDecision of(
            AssistantIntentType type,
            double confidence,
            boolean useRelational,
            boolean useColumnar,
            boolean useVector,
            String rationale
    ) {
        return new AssistantIntentDecision(type, confidence, useRelational, useColumnar, useVector, rationale);
    }

    public boolean is(AssistantIntentType t) {
        return type == t;
    }
}
