package org.linguapulse.agent;

import org.linguapulse.model.PracticeScenario;
import org.linguapulse.prompt.PromptTemplates;

public final class AgentPersona {

    private AgentPersona() {
    }

    public static String buildSystemPrompt(AgentConfig config, PracticeScenario scenario) {
        return PromptTemplates.systemPrompt(config, scenario);
    }

    public static String buildFeedbackPrompt(AgentConfig config, String transcript) {
        return PromptTemplates.feedbackPrompt(config, transcript);
    }
}
