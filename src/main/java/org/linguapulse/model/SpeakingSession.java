package org.linguapulse.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SpeakingSession {

    private final String id;
    private final String targetLanguage;
    private final ProficiencyLevel level;
    private final PracticeScenario scenario;
    private final Instant startedAt;
    private final List<Message> messages = new ArrayList<>();

    public SpeakingSession(String targetLanguage, ProficiencyLevel level, PracticeScenario scenario) {
        this.id = UUID.randomUUID().toString();
        this.targetLanguage = targetLanguage;
        this.level = level;
        this.scenario = scenario;
        this.startedAt = Instant.now();
    }

    public String id() {
        return id;
    }

    public String targetLanguage() {
        return targetLanguage;
    }

    public ProficiencyLevel level() {
        return level;
    }

    public PracticeScenario scenario() {
        return scenario;
    }

    public Instant startedAt() {
        return startedAt;
    }

    public List<Message> messages() {
        return List.copyOf(messages);
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    public int turnCount() {
        return (int) messages.stream()
                .filter(m -> m.role() == Message.Role.USER)
                .count();
    }
}
