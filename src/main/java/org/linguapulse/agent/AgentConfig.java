package org.linguapulse.agent;

import org.linguapulse.model.ProficiencyLevel;

public record AgentConfig(
        String targetLanguage,
        ProficiencyLevel level,
        String coachName,
        boolean strictGrammarCorrection,
        boolean growthMindsetMode
) {
    public static AgentConfig defaults(String targetLanguage, ProficiencyLevel level) {
        return new AgentConfig(targetLanguage, level, "Alex", false, true);
    }
}
