package org.linguapulse.llm;

import org.linguapulse.model.Message;

import java.util.List;

public interface LlmClient {

    String chat(List<Message> messages);

    boolean isAvailable();
}
