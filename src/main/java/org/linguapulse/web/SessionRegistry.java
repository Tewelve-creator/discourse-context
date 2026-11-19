package org.linguapulse.web;

import org.linguapulse.agent.SpeakingAgent;
import org.linguapulse.model.SpeakingSession;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SessionRegistry {

    private final Map<String, SessionEntry> sessions = new ConcurrentHashMap<>();

    public void put(String sessionId, SpeakingAgent agent, SpeakingSession session) {
        sessions.put(sessionId, new SessionEntry(agent, session));
    }

    public Optional<SessionEntry> get(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    public void remove(String sessionId) {
        sessions.remove(sessionId);
    }

    public record SessionEntry(SpeakingAgent agent, SpeakingSession session) {
    }
}
