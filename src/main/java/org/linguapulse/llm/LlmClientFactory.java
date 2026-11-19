package org.linguapulse.llm;

public final class LlmClientFactory {

    private LlmClientFactory() {
    }

    public static LlmClient create() {
        return LlmConfigManager.getInstance().createClient();
    }
}
