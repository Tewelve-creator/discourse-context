package org.linguapulse.model;

public enum ProficiencyLevel {
    A1("Beginner", "Use very simple words and short sentences."),
    A2("Elementary", "Use everyday vocabulary with gentle correction."),
    B1("Intermediate", "Encourage longer answers; introduce idioms sparingly."),
    B2("Upper Intermediate", "Discuss abstract self-improvement topics with nuance."),
    C1("Advanced", "Challenge with professional and rhetorical language.");

    private final String label;
    private final String coachingHint;

    ProficiencyLevel(String label, String coachingHint) {
        this.label = label;
        this.coachingHint = coachingHint;
    }

    public String label() {
        return label;
    }

    public String coachingHint() {
        return coachingHint;
    }
}
