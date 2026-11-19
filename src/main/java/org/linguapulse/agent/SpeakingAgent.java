package org.linguapulse.agent;

import org.linguapulse.llm.LlmConfigManager;
import org.linguapulse.model.Message;
import org.linguapulse.model.PracticeScenario;
import org.linguapulse.model.SpeakingSession;

import java.util.ArrayList;
import java.util.List;

public class SpeakingAgent {

    private final AgentConfig config;
    private final LlmConfigManager llmManager;

    public SpeakingAgent(AgentConfig config, LlmConfigManager llmManager) {
        this.config = config;
        this.llmManager = llmManager;
    }

    public SpeakingSession startSession(PracticeScenario scenario, String openingLine) {
        SpeakingSession session = new SpeakingSession(config.targetLanguage(), config.level(), scenario);
        session.addMessage(new Message(Message.Role.SYSTEM, AgentPersona.buildSystemPrompt(config, scenario)));
        session.addMessage(new Message(Message.Role.ASSISTANT, openingLine));
        return session;
    }

    public String reply(SpeakingSession session, String userInput) {
        session.addMessage(new Message(Message.Role.USER, userInput));

        List<Message> llmMessages = new ArrayList<>(session.messages());
        String response = llmManager.createClient().chat(llmMessages);

        session.addMessage(new Message(Message.Role.ASSISTANT, response));
        return response;
    }

    public String buildTranscript(SpeakingSession session) {
        StringBuilder sb = new StringBuilder();
        for (Message message : session.messages()) {
            if (message.role() == Message.Role.SYSTEM) {
                continue;
            }
            sb.append(message.role().name()).append(": ").append(message.content()).append("\n");
        }
        return sb.toString().trim();
    }

    public AgentConfig config() {
        return config;
    }
}
