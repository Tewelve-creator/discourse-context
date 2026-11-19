package org.linguapulse.model;

import java.util.List;

public record PracticeScenario(
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
        ProficiencyLevel recommendedLevel
) {
    public String displayTitle() {
        return titleZh != null && !titleZh.isBlank() ? titleZh : title;
    }

    public String displayCategory() {
        return categoryZh != null && !categoryZh.isBlank() ? categoryZh : category;
    }

    public String displayDescription() {
        return descriptionZh != null && !descriptionZh.isBlank() ? descriptionZh : description;
    }

    public List<String> displayKeyPhrases() {
        return keyPhrasesZh != null && !keyPhrasesZh.isEmpty() ? keyPhrasesZh : keyPhrases;
    }

    public String openingLineFor(String targetLanguage) {
        if (targetLanguage != null && targetLanguage.equalsIgnoreCase("Chinese")
                && openingLineZh != null && !openingLineZh.isBlank()) {
            return openingLineZh;
        }
        return openingLine;
    }
}
