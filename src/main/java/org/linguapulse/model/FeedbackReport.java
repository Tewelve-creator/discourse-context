package org.linguapulse.model;

import java.util.List;

public record FeedbackReport(
        int overallScore,
        int fluencyScore,
        int grammarScore,
        int vocabularyScore,
        int confidenceScore,
        String summary,
        List<String> strengths,
        List<String> improvements,
        List<String> suggestedPhrases,
        String nextChallenge,
        boolean noParticipation
) {
    public FeedbackReport(
            int overallScore,
            int fluencyScore,
            int grammarScore,
            int vocabularyScore,
            int confidenceScore,
            String summary,
            List<String> strengths,
            List<String> improvements,
            List<String> suggestedPhrases,
            String nextChallenge
    ) {
        this(
                overallScore, fluencyScore, grammarScore, vocabularyScore, confidenceScore,
                summary, strengths, improvements, suggestedPhrases, nextChallenge, false
        );
    }
}
