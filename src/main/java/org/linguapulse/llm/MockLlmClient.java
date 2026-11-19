package org.linguapulse.llm;

import org.linguapulse.model.Message;
import org.linguapulse.model.ProficiencyLevel;

import java.util.List;
import java.util.Locale;

public class MockLlmClient implements LlmClient {

    @Override
    public String chat(List<Message> messages) {
        Message last = messages.get(messages.size() - 1);

        if (last.role() == Message.Role.SYSTEM
                && (last.content().contains("JSON schema") || last.content().contains("JSON 格式"))) {
            ProficiencyLevel level = detectLevel(messages);
            return feedbackJson(level);
        }

        String language = detectLanguage(messages);
        ProficiencyLevel level = detectLevel(messages);
        int userTurns = countUserTurns(messages);
        String userText = last.content().toLowerCase(Locale.ROOT);

        return switch (language) {
            case "Chinese" -> chineseReply(level, userTurns, userText);
            case "French" -> frenchReply(level, userTurns, userText);
            case "Japanese" -> japaneseReply(level, userTurns, userText);
            case "Spanish" -> spanishReply(level, userTurns, userText);
            default -> englishReply(level, userTurns, userText);
        };
    }

    private String frenchReply(ProficiencyLevel level, int turn, String userText) {
        return switch (levelTier(level)) {
            case BEGINNER -> turn == 1
                    ? "Très bien ! 👍 Parlez-moi d'une autre habitude, d'accord ?"
                    : "Merci ! Est-ce difficile de continuer ?";
            case INTERMEDIATE -> turn == 1
                    ? "C'est un bon début ! Pouvez-vous me parler d'une habitude que vous développez en ce moment, et pourquoi elle compte pour vous ?"
                    : (userText.contains("habitude") || userText.contains("matin") || userText.contains("routine")
                    ? "J'adore — la régularité est la base de l'épanouissement personnel. Quel obstacle rend cette habitude difficile à maintenir ?"
                    : "Merci pour votre partage. Comment allez-vous appliquer cela dans vos conversations cette semaine ?");
            case ADVANCED -> turn == 1
                    ? "Ouverture convaincante — quelle identité cherchez-vous à cultiver à travers cette architecture d'habitudes ?"
                    : "Fascinant. Du point de vue des systèmes, comment cette habitude se cumule-t-elle vers votre vision à long terme ?";
        };
    }

    private String japaneseReply(ProficiencyLevel level, int turn, String userText) {
        return switch (levelTier(level)) {
            case BEGINNER -> turn == 1
                    ? "いいですね！👍 もう一つ習慣を教えてください。"
                    : "ありがとう！続けるのは難しいですか？";
            case INTERMEDIATE -> turn == 1
                    ? "良いスタートです！今取り組んでいる習慣と、その大切さを教えてください。"
                    : (userText.contains("習慣") || userText.contains("朝") || userText.contains("毎日")
                    ? "素晴らしい——継続は自己成長の基盤です。この習慣を続ける上で最大の障害は何ですか？"
                    : "共有ありがとうございます。今週の会話にどう活かしますか？");
            case ADVANCED -> turn == 1
                    ? "説得力のあるオープニングです——この習慣を通じてどんなアイデンティティを育てていますか？"
                    : "興味深いですね。システム思考の観点から、この習慣は長期ビジョンにどう複利効果を生みますか？";
        };
    }

    private String spanishReply(ProficiencyLevel level, int turn, String userText) {
        return switch (levelTier(level)) {
            case BEGINNER -> turn == 1
                    ? "¡Muy bien! 👍 Cuéntame otro hábito, ¿de acuerdo?"
                    : "¡Gracias! ¿Es difícil mantenerlo?";
            case INTERMEDIATE -> turn == 1
                    ? "¡Buen comienzo! ¿Puedes contarme un hábito que estés desarrollando ahora y por qué es importante para ti?"
                    : (userText.contains("hábito") || userText.contains("habito") || userText.contains("mañana") || userText.contains("rutina")
                    ? "Me encanta — la constancia es la base del crecimiento personal. ¿Qué obstáculo hace más difícil mantener ese hábito?"
                    : "Gracias por compartir. ¿Cómo planeas aplicar esto en tus conversaciones esta semana?");
            case ADVANCED -> turn == 1
                    ? "Apertura convincente — ¿qué identidad buscas cultivar a través de esta arquitectura de hábitos?"
                    : "Fascinante. Desde una perspectiva de sistemas, ¿cómo se acumula este hábito hacia tu visión a largo plazo?";
        };
    }

    private String chineseReply(ProficiencyLevel level, int turn, String userText) {
        return switch (levelTier(level)) {
            case BEGINNER -> chineseBeginner(turn, userText);
            case INTERMEDIATE -> chineseIntermediate(turn, userText);
            case ADVANCED -> chineseAdvanced(turn, userText);
        };
    }

    private String englishReply(ProficiencyLevel level, int turn, String userText) {
        return switch (levelTier(level)) {
            case BEGINNER -> englishBeginner(turn, userText);
            case INTERMEDIATE -> englishIntermediate(turn, userText);
            case ADVANCED -> englishAdvanced(turn, userText);
        };
    }

    private String chineseBeginner(int turn, String userText) {
        if (isGreetingOnly(userText)) {
            return "你好！很高兴和你练习。你醒来做的第一件事是什么？";
        }
        if (isTooShort(userText)) {
            return "好的，可以再说具体一点吗？比如：起床后会做什么？";
        }
        if (turn == 1) return "很好！👍 再说一个你的习惯，好吗？";
        if (userText.contains("习惯") || userText.contains("每天") || userText.contains("水")) {
            return "不错！这个习惯什么时候最难坚持？";
        }
        return "谢谢分享！明天你还想继续吗？";
    }

    private String chineseIntermediate(int turn, String userText) {
        if (isGreetingOnly(userText)) {
            return "你好！很高兴开始今天的练习。我刚才问的是——你醒来做的第一件事是什么？它如何帮助你成长？";
        }
        if (isTooShort(userText)) {
            return "好的，可以再具体一点吗？比如：你几点起床？第一件事做什么？为什么？";
        }
        if (turn == 1) {
            return "很好的开始！我能感受到你的积极性。"
                    + "能具体说说你现在正在培养的一个习惯吗？它为什么对你很重要？";
        }
        if (userText.contains("习惯") || userText.contains("坚持") || userText.contains("每天")) {
            return "很棒——坚持就是自我提升的基石。"
                    + "在坚持这个习惯的过程中，什么障碍最让你难以持续？";
        }
        if (userText.contains("难") || userText.contains("障碍") || userText.contains("困难")) {
            return "能正视障碍本身就已经是成长型思维的体现了。"
                    + "如果能给过去的自己一条建议，你会说什么？";
        }
        return "谢谢你的分享——你的反思很有深度。"
                + "你打算怎么把今天学到的东西应用到这周的日常对话中？";
    }

    private String chineseAdvanced(int turn, String userText) {
        if (turn == 1) {
            return "你的开场很有洞见。我注意到你在描述习惯时带有明确的意图感——"
                    + "这种习惯背后，你试图塑造的是怎样一种身份认同？";
        }
        if (userText.contains("习惯") || userText.contains("身份") || userText.contains("系统")) {
            return "很有意思。从系统思维的角度看，这个习惯如何与你的长期愿景形成复利效应？"
                    + "当环境压力增大时，你的「最小可行习惯」是什么？";
        }
        return "你的自我觉察令人印象深刻。"
                + "如果把这个习惯的设计原则迁移到另一个生活领域，你会如何重构你的行动框架？";
    }

    private String englishBeginner(int turn, String userText) {
        if (isGreetingOnly(userText)) {
            return "Hello! Glad to practice with you. What's the first thing you do when you wake up?";
        }
        if (isTooShort(userText)) {
            return "Could you say a bit more? For example: what do you do after waking up?";
        }
        if (turn == 1) return "Good! 👍 Tell me one more thing about your habit.";
        if (userText.contains("water") || userText.contains("wake") || userText.contains("habit")) {
            return "Nice! When is this habit hardest for you?";
        }
        return "Thank you! Will you try again tomorrow?";
    }

    private String englishIntermediate(int turn, String userText) {
        if (isGreetingOnly(userText)) {
            return "Hello! Great to start today's practice. I asked — what's the first thing you do when you wake up, and how does it help you grow?";
        }
        if (isTooShort(userText)) {
            return "Good start! Could you be more specific? For example: what time do you wake up, what's your first action, and why?";
        }
        if (turn == 1) {
            return "That's a great start! I can hear your motivation. "
                    + "Could you tell me one specific habit you're building right now, and why it matters to you?";
        }
        if (userText.contains("habit") || userText.contains("routine") || userText.contains("every day")) {
            return "I love that — consistency is the foundation of self-improvement. "
                    + "What obstacle usually makes it hardest for you to stick with that habit?";
        }
        if (userText.contains("difficult") || userText.contains("hard") || userText.contains("obstacle")) {
            return "Acknowledging obstacles is already a sign of growth mindset. "
                    + "If you could give your past self one piece of advice about learning, what would it be?";
        }
        return "Thank you for sharing that — your reflection shows real self-awareness. "
                + "How do you plan to apply what you've learned in your daily conversations this week?";
    }

    private String englishAdvanced(int turn, String userText) {
        if (turn == 1) {
            return "Compelling opening — I notice you're framing your routine with clear intentionality. "
                    + "What identity are you consciously cultivating through this habit architecture?";
        }
        if (userText.contains("habit") || userText.contains("identity") || userText.contains("system")) {
            return "Fascinating. From a systems-thinking lens, how does this habit compound toward your long-term vision? "
                    + "When environmental friction increases, what's your minimum viable habit?";
        }
        return "Your self-awareness is impressive. "
                + "If you transplanted this habit-design principle to another domain, how would you restructure your action framework?";
    }

    private String feedbackJson(ProficiencyLevel level) {
        int base = switch (level) {
            case A1 -> 62;
            case A2 -> 68;
            case B1 -> 75;
            case B2 -> 82;
            case C1 -> 88;
        };
        return """
                {
                  "overallScore": %d,
                  "fluencyScore": %d,
                  "grammarScore": %d,
                  "vocabularyScore": %d,
                  "confidenceScore": %d,
                  "summary": "%s",
                  "strengths": %s,
                  "improvements": %s,
                  "suggestedPhrases": %s,
                  "nextChallenge": "%s"
                }
                """.formatted(
                base, base - 2, base + 2, base - 3, base + 1,
                feedbackSummary(level),
                feedbackArray(feedbackStrengths(level)),
                feedbackArray(feedbackImprovements(level)),
                feedbackArray(feedbackPhrases(level)),
                feedbackChallenge(level)
        );
    }

    private String feedbackSummary(ProficiencyLevel level) {
        return switch (level) {
            case A1 -> "作为入门练习，你能用简单句子回应问题，这是很好的起点。建议继续积累基础词汇。";
            case A2 -> "基础水平表现不错，能围绕话题给出简短回答。可以尝试把句子说得更完整一些。";
            case B1 -> "中级表达整体流畅，能围绕自我提升话题展开对话。语法基本正确，词汇可以更丰富。";
            case B2 -> "中高级表现良好，回答有逻辑层次，能使用场景相关表达。可进一步增加复杂句式。";
            case C1 -> "高级水平表现出色，论述有深度，自我反思能力强。可继续打磨修辞与论证结构。";
        };
    }

    private String[] feedbackStrengths(ProficiencyLevel level) {
        return switch (level) {
            case A1, A2 -> new String[]{"敢于开口", "能回答核心问题"};
            case B1 -> new String[]{"回答有逻辑", "敢于表达观点", "能使用场景关键词"};
            case B2, C1 -> new String[]{"论述有深度", "自我反思能力强", "词汇运用较丰富"};
        };
    }

    private String[] feedbackImprovements(ProficiencyLevel level) {
        return switch (level) {
            case A1 -> new String[]{"多积累基础词汇", "尝试说完整句子"};
            case A2 -> new String[]{"扩展句型", "减少停顿"};
            case B1 -> new String[]{"尝试使用更复杂的从句", "减少口头禅", "扩展同义替换"};
            case B2 -> new String[]{"增加论证层次", "使用更多连接词", "提升表达精准度"};
            case C1 -> new String[]{"打磨修辞结构", "增强论证说服力", "控制句子长度节奏"};
        };
    }

    private String[] feedbackPhrases(ProficiencyLevel level) {
        return switch (level) {
            case A1, A2 -> new String[]{"I wake up at...", "I like to...", "Every day I..."};
            case B1, B2 -> new String[]{"I've been working on...", "One habit that changed my life is...", "I'm proud of the progress I've made"};
            case C1 -> new String[]{"I've architected my routine around...", "This habit compounds because...", "The identity I'm cultivating is..."};
        };
    }

    private String feedbackChallenge(ProficiencyLevel level) {
        return switch (level) {
            case A1 -> "用 3 个简单句子描述你的晨间习惯。";
            case A2 -> "用 30 秒描述一个你每天坚持的小习惯。";
            case B1 -> "用 90 秒描述一个你正在培养的习惯，并解释它如何帮助你成长。";
            case B2 -> "模拟面试：用 2 分钟讲述一次从失败中学习的经历。";
            case C1 -> "准备一段 3 分钟的 TED 式开场，阐述一个习惯如何重塑你的身份认同。";
        };
    }

    private String feedbackArray(String[] items) {
        return "[\"" + String.join("\", \"", items) + "\"]";
    }

    private boolean isGreetingOnly(String text) {
        String t = text.trim().toLowerCase(Locale.ROOT);
        return t.matches("^(你好|您好|hi|hello|hey|bonjour|hola|こんにちは|おはよう)[!.?~]*$");
    }

    private boolean isTooShort(String text) {
        return text.trim().length() <= 6;
    }

    private enum LevelTier { BEGINNER, INTERMEDIATE, ADVANCED }

    private LevelTier levelTier(ProficiencyLevel level) {
        return switch (level) {
            case A1, A2 -> LevelTier.BEGINNER;
            case B1, B2 -> LevelTier.INTERMEDIATE;
            case C1 -> LevelTier.ADVANCED;
        };
    }

    private String detectLanguage(List<Message> messages) {
        for (Message message : messages) {
            if (message.role() == Message.Role.SYSTEM) {
                String content = message.content();
                String fromMeta = extractMetaValue(content, "Target language:");
                if (fromMeta != null) {
                    return fromMeta;
                }
                String fromZh = extractLineValue(content, "目标语言：");
                if (fromZh != null) {
                    return fromZh;
                }
            }
        }
        return "English";
    }

    private ProficiencyLevel detectLevel(List<Message> messages) {
        for (Message message : messages) {
            if (message.role() == Message.Role.SYSTEM) {
                String content = message.content();
                String fromMeta = extractMetaValue(content, "Proficiency:");
                if (fromMeta != null) {
                    try {
                        return ProficiencyLevel.valueOf(fromMeta.trim());
                    } catch (IllegalArgumentException ignored) {
                    }
                }
                String fromZh = extractLineValue(content, "水平：");
                if (fromZh != null) {
                    String levelStr = fromZh.split("[（(\\s]")[0].trim();
                    try {
                        return ProficiencyLevel.valueOf(levelStr);
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
        }
        return ProficiencyLevel.B1;
    }

    private String extractMetaValue(String content, String key) {
        int blockStart = content.indexOf("[SESSION_META]");
        if (blockStart < 0) {
            return null;
        }
        int blockEnd = content.indexOf("[/SESSION_META]", blockStart);
        if (blockEnd < 0) {
            return null;
        }
        String block = content.substring(blockStart, blockEnd);
        int idx = block.indexOf(key);
        if (idx < 0) {
            return null;
        }
        return block.substring(idx + key.length()).split("\n")[0].trim();
    }

    private String extractLineValue(String content, String key) {
        int idx = content.indexOf(key);
        if (idx < 0) {
            return null;
        }
        return content.substring(idx + key.length()).split("\n")[0].trim();
    }

    private int countUserTurns(List<Message> messages) {
        return (int) messages.stream()
                .filter(m -> m.role() == Message.Role.USER)
                .count();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
