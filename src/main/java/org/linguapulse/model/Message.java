package org.linguapulse.model;

import java.time.Instant;

public record Message(Role role, String content, Instant timestamp) {

    public enum Role {
        SYSTEM, USER, ASSISTANT
    }

    public Message(Role role, String content) {
        this(role, content, Instant.now());
    }
}
