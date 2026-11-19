package org.linguapulse.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.linguapulse.model.PracticeScenario;
import org.linguapulse.model.ProficiencyLevel;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public class ScenarioService {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final List<PracticeScenario> scenarios;

    public ScenarioService() {
        this.scenarios = loadScenarios();
    }

    public List<PracticeScenario> all() {
        return scenarios;
    }

    public Optional<PracticeScenario> findById(String id) {
        return scenarios.stream().filter(s -> s.id().equals(id)).findFirst();
    }

    public List<PracticeScenario> byCategory(String category) {
        return scenarios.stream()
                .filter(s -> s.category().equalsIgnoreCase(category))
                .toList();
    }

    public List<PracticeScenario> byLevel(ProficiencyLevel level) {
        return scenarios.stream()
                .filter(s -> s.recommendedLevel() == level)
                .toList();
    }

    private List<PracticeScenario> loadScenarios() {
        try (InputStream in = getClass().getResourceAsStream("/scenarios/self-improvement-scenarios.json")) {
            if (in == null) {
                throw new IllegalStateException("Scenario file not found");
            }
            List<ScenarioDto> dtos = MAPPER.readValue(in, new TypeReference<>() {});
            return dtos.stream().map(ScenarioDto::toScenario).toList();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load scenarios", e);
        }
    }

    private record ScenarioDto(
            String id,
            String title,
            String titleZh,
            String category,
            String categoryZh,
            String description,
            String descriptionZh,
            String openingLine,
            String openingLineZh,
            List<String> keyPhrases,
            List<String> keyPhrasesZh,
            String recommendedLevel
    ) {
        PracticeScenario toScenario() {
            return new PracticeScenario(
                    id,
                    title,
                    titleZh,
                    category,
                    categoryZh,
                    description,
                    descriptionZh,
                    openingLine,
                    openingLineZh,
                    keyPhrases,
                    keyPhrasesZh,
                    ProficiencyLevel.valueOf(recommendedLevel)
            );
        }
    }
}
