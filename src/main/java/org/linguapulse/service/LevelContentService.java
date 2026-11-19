package org.linguapulse.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.linguapulse.model.PracticeScenario;
import org.linguapulse.model.ProficiencyLevel;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class LevelContentService {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final Map<String, Map<String, Map<String, String>>> openings;

    public LevelContentService() {
        this.openings = loadAllOpenings();
    }

    public String openingLine(PracticeScenario scenario, String language, ProficiencyLevel level) {
        String lang = normalizeLanguage(language);
        String levelKey = level.name();

        Map<String, Map<String, String>> byLang = openings.get(scenario.id());
        if (byLang == null) {
            return scenario.openingLineFor(language);
        }

        String line = findLine(byLang, lang, levelKey);
        if (line != null) {
            return line;
        }

        line = findLine(byLang, "English", levelKey);
        if (line != null) {
            return line;
        }

        return scenario.openingLineFor(language);
    }

    private String findLine(Map<String, Map<String, String>> byLang, String lang, String levelKey) {
        Map<String, String> byLevel = byLang.get(lang);
        if (byLevel == null) {
            return null;
        }
        if (byLevel.containsKey(levelKey)) {
            return byLevel.get(levelKey);
        }
        if (byLevel.containsKey("B1")) {
            return byLevel.get("B1");
        }
        return byLevel.values().stream().findFirst().orElse(null);
    }

    private String normalizeLanguage(String language) {
        if (language == null || language.isBlank()) {
            return "English";
        }
        return switch (language.trim()) {
            case "Chinese" -> "Chinese";
            case "Japanese" -> "Japanese";
            case "French" -> "French";
            case "Spanish" -> "Spanish";
            default -> "English";
        };
    }

    private Map<String, Map<String, Map<String, String>>> loadAllOpenings() {
        Map<String, Map<String, Map<String, String>>> merged = new HashMap<>();
        mergeFile(merged, "/scenarios/level-openings.json");
        mergeFile(merged, "/scenarios/multilingual-openings.json");
        return merged;
    }

    private void mergeFile(Map<String, Map<String, Map<String, String>>> target, String path) {
        try (InputStream in = getClass().getResourceAsStream(path)) {
            if (in == null) {
                return;
            }
            Map<String, Map<String, Map<String, String>>> loaded =
                    MAPPER.readValue(in, new TypeReference<>() {});
            for (var scenarioEntry : loaded.entrySet()) {
                target.computeIfAbsent(scenarioEntry.getKey(), k -> new HashMap<>())
                        .putAll(scenarioEntry.getValue());
            }
        } catch (Exception ignored) {
        }
    }
}
