package org.linguapulse.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.linguapulse.agent.AgentConfig;
import org.linguapulse.agent.SpeakingAgent;
import org.linguapulse.llm.LlmConfigManager;
import org.linguapulse.model.FeedbackReport;
import org.linguapulse.model.PracticeScenario;
import org.linguapulse.model.ProficiencyLevel;
import org.linguapulse.model.SpeakingSession;
import org.linguapulse.service.FeedbackService;
import org.linguapulse.service.LevelContentService;
import org.linguapulse.service.ScenarioService;

import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WebServer {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int PORT = Integer.parseInt(
            System.getenv().getOrDefault("LINGUAPULSE_PORT", "8090")
    );

    private final ScenarioService scenarioService;
    private final LevelContentService levelContentService;
    private final FeedbackService feedbackService;
    private final LlmConfigManager llmManager;
    private final SessionRegistry registry = new SessionRegistry();

    public WebServer() {
        this.llmManager = LlmConfigManager.getInstance();
        this.scenarioService = new ScenarioService();
        this.levelContentService = new LevelContentService();
        this.feedbackService = new FeedbackService(llmManager);
    }

    public void start(boolean openBrowser) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", this::handleStatic);
        server.createContext("/api/scenarios", this::handleScenarios);
        server.createContext("/api/session/start", this::handleStartSession);
        server.createContext("/api/session/chat", this::handleChat);
        server.createContext("/api/session/feedback", this::handleFeedback);
        server.createContext("/api/status", this::handleStatus);
        server.createContext("/api/llm/config", this::handleLlmConfig);
        server.setExecutor(null);
        server.start();

        String url = "http://localhost:" + PORT;
        System.out.println("LinguaPulse Web 已启动: " + url);
        System.out.println(llmManager.isLiveMode()
                ? "模式: AI 智能对话（" + llmManager.status().get("provider") + "）"
                : "模式: 演示模式（请在页面配置 API Key 启用 AI）");

        if (openBrowser) {
            openBrowser(url);
        }
    }

    private void handleStatic(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendJson(exchange, 405, Map.of("error", "Method not allowed"));
            return;
        }

        String path = exchange.getRequestURI().getPath();
        if ("/".equals(path)) {
            path = "/index.html";
        }

        String resourcePath = "/web" + path;
        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null) {
                sendText(exchange, 404, "Not Found");
                return;
            }
            byte[] bytes = in.readAllBytes();
            exchange.getResponseHeaders().set("Content-Type", contentType(path));
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        }
    }

    private void handleScenarios(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendJson(exchange, 405, Map.of("error", "Method not allowed"));
            return;
        }
        List<Map<String, Object>> scenarios = scenarioService.all().stream()
                .map(this::scenarioToMap)
                .collect(Collectors.toList());
        sendJson(exchange, 200, Map.of("scenarios", scenarios));
    }

    private void handleStartSession(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendJson(exchange, 405, Map.of("error", "Method not allowed"));
            return;
        }

        Map<String, String> body = readJsonMap(exchange);
        String language = body.getOrDefault("language", "English");
        String levelStr = body.getOrDefault("level", "B1");
        String scenarioId = body.get("scenarioId");

        ProficiencyLevel level;
        try {
            level = ProficiencyLevel.valueOf(levelStr);
        } catch (IllegalArgumentException e) {
            sendJson(exchange, 400, Map.of("error", "Invalid level: " + levelStr));
            return;
        }

        PracticeScenario scenario = scenarioService.findById(scenarioId)
                .orElse(scenarioService.all().get(0));

        AgentConfig config = AgentConfig.defaults(language, level);
        SpeakingAgent agent = new SpeakingAgent(config, llmManager);
        String openingLine = levelContentService.openingLine(scenario, language, level);
        SpeakingSession session = agent.startSession(scenario, openingLine);
        registry.put(session.id(), agent, session);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("sessionId", session.id());
        response.put("openingLine", openingLine);
        response.put("scenario", scenarioToMap(scenario));
        response.put("language", language);
        response.put("level", level.name());
        response.put("levelLabel", levelLabel(level));
        sendJson(exchange, 200, response);
    }

    private void handleChat(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendJson(exchange, 405, Map.of("error", "Method not allowed"));
            return;
        }

        Map<String, String> body = readJsonMap(exchange);
        String sessionId = body.get("sessionId");
        String message = body.get("message");

        if (sessionId == null || message == null || message.isBlank()) {
            sendJson(exchange, 400, Map.of("error", "sessionId and message required"));
            return;
        }

        SessionRegistry.SessionEntry entry = registry.get(sessionId)
                .orElse(null);
        if (entry == null) {
            sendJson(exchange, 404, Map.of("error", "Session not found"));
            return;
        }

        try {
            String reply = entry.agent().reply(entry.session(), message.trim());
            sendJson(exchange, 200, Map.of(
                    "reply", reply,
                    "turnCount", entry.session().turnCount(),
                    "mode", llmManager.isLiveMode() ? "live" : "mock"
            ));
        } catch (Exception e) {
            sendJson(exchange, 500, Map.of("error", "AI 回复失败：" + e.getMessage()));
        }
    }

    private void handleLlmConfig(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            sendJson(exchange, 200, llmManager.status());
            return;
        }
        if ("POST".equals(exchange.getRequestMethod())) {
            Map<String, String> body = readJsonMap(exchange);
            llmManager.updateConfig(
                    body.get("provider"),
                    body.get("apiKey"),
                    body.get("baseUrl"),
                    body.get("model")
            );
            sendJson(exchange, 200, llmManager.status());
            return;
        }
        sendJson(exchange, 405, Map.of("error", "Method not allowed"));
    }

    private void handleFeedback(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendJson(exchange, 405, Map.of("error", "Method not allowed"));
            return;
        }

        Map<String, String> body = readJsonMap(exchange);
        String sessionId = body.get("sessionId");
        if (sessionId == null) {
            sendJson(exchange, 400, Map.of("error", "sessionId required"));
            return;
        }

        SessionRegistry.SessionEntry entry = registry.get(sessionId)
                .orElse(null);
        if (entry == null) {
            sendJson(exchange, 404, Map.of("error", "Session not found"));
            return;
        }

        FeedbackReport report = feedbackService.generate(entry.agent(), entry.session());
        sendJson(exchange, 200, report);
    }

    private void handleStatus(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendJson(exchange, 405, Map.of("error", "Method not allowed"));
            return;
        }
        Map<String, Object> status = new LinkedHashMap<>(llmManager.status());
        status.put("port", PORT);
        sendJson(exchange, 200, status);
    }

    private Map<String, Object> scenarioToMap(PracticeScenario s) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", s.id());
        map.put("title", s.displayTitle());
        map.put("titleEn", s.title());
        map.put("category", s.displayCategory());
        map.put("categoryEn", s.category());
        map.put("description", s.displayDescription());
        map.put("descriptionEn", s.description());
        map.put("openingLine", s.openingLine());
        map.put("keyPhrases", s.displayKeyPhrases());
        map.put("keyPhrasesEn", s.keyPhrases());
        map.put("recommendedLevel", s.recommendedLevel().name());
        map.put("recommendedLevelLabel", levelLabel(s.recommendedLevel()));
        return map;
    }

    private String levelLabel(org.linguapulse.model.ProficiencyLevel level) {
        return switch (level) {
            case A1 -> "入门";
            case A2 -> "基础";
            case B1 -> "中级";
            case B2 -> "中高级";
            case C1 -> "高级";
        };
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> readJsonMap(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        return MAPPER.readValue(body, Map.class);
    }

    private void sendJson(HttpExchange exchange, int status, Object data) throws IOException {
        byte[] bytes = MAPPER.writeValueAsBytes(data);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private void sendText(HttpExchange exchange, int status, String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private String contentType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=utf-8";
        if (path.endsWith(".css")) return "text/css; charset=utf-8";
        if (path.endsWith(".js")) return "application/javascript; charset=utf-8";
        return "application/octet-stream";
    }

    private void openBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(url));
            } else {
                new ProcessBuilder("cmd", "/c", "start", url).start();
            }
        } catch (Exception e) {
            System.out.println("请手动打开浏览器访问: " + url);
        }
    }
}
