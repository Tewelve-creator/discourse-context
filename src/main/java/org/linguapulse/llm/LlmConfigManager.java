package org.linguapulse.llm;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public final class LlmConfigManager {

    private static final LlmConfigManager INSTANCE = new LlmConfigManager();

    private volatile String apiKey = "";
    private volatile String baseUrl = "https://api.deepseek.com/v1";
    private volatile String model = "deepseek-chat";
    private volatile String provider = "mock";

    private LlmConfigManager() {
        loadFromEnv();
        loadFromFile(Path.of("config.properties"));
    }

    public static LlmConfigManager getInstance() {
        return INSTANCE;
    }

    public LlmClient createClient() {
        if (hasApiKey()) {
            return new OpenAiCompatibleClient(apiKey, baseUrl, model);
        }
        return new MockLlmClient();
    }

    public boolean isLiveMode() {
        return hasApiKey();
    }

    public synchronized void updateConfig(String provider, String apiKey, String baseUrl, String model) {
        if (provider != null && !provider.isBlank()) {
            this.provider = provider;
            applyProviderDefaults(provider);
        }
        if (apiKey != null && !apiKey.isBlank()) {
            this.apiKey = apiKey.trim();
        }
        if (baseUrl != null && !baseUrl.isBlank()) {
            this.baseUrl = baseUrl.trim();
        }
        if (model != null && !model.isBlank()) {
            this.model = model.trim();
        }
        if (hasApiKey()) {
            this.provider = this.provider.equals("mock") ? "custom" : this.provider;
        }
    }

    public Map<String, Object> status() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("mode", isLiveMode() ? "live" : "mock");
        map.put("provider", provider);
        map.put("baseUrl", baseUrl);
        map.put("model", model);
        map.put("hasApiKey", hasApiKey());
        map.put("apiKeyHint", maskApiKey(apiKey));
        return map;
    }

    private void loadFromEnv() {
        String key = System.getenv("OPENAI_API_KEY");
        if (key != null && !key.isBlank()) {
            apiKey = key.trim();
            baseUrl = envOrDefault("OPENAI_BASE_URL", baseUrl);
            model = envOrDefault("OPENAI_MODEL", model);
            provider = envOrDefault("LLM_PROVIDER", "openai");
        }
    }

    private void loadFromFile(Path path) {
        if (!Files.exists(path)) {
            return;
        }
        try (InputStream in = Files.newInputStream(path)) {
            Properties props = new Properties();
            props.load(in);
            String key = props.getProperty("llm.api.key", props.getProperty("OPENAI_API_KEY", ""));
            if (key != null && !key.isBlank()) {
                apiKey = key.trim();
            }
            baseUrl = props.getProperty("llm.base.url", props.getProperty("OPENAI_BASE_URL", baseUrl));
            model = props.getProperty("llm.model", props.getProperty("OPENAI_MODEL", model));
            provider = props.getProperty("llm.provider", provider);
        } catch (IOException ignored) {
        }
    }

    private void applyProviderDefaults(String provider) {
        switch (provider.toLowerCase()) {
            case "deepseek" -> {
                baseUrl = "https://api.deepseek.com/v1";
                model = "deepseek-chat";
            }
            case "openai" -> {
                baseUrl = "https://api.openai.com/v1";
                model = "gpt-4o-mini";
            }
            case "qwen" -> {
                baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
                model = "qwen-plus";
            }
            default -> {
            }
        }
    }

    private boolean hasApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }

    private String maskApiKey(String key) {
        if (key == null || key.length() < 8) {
            return "";
        }
        return key.substring(0, 4) + "****" + key.substring(key.length() - 4);
    }

    private String envOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null && !value.isBlank() ? value : defaultValue;
    }
}
