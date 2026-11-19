package org.linguapulse.prompt;

import org.linguapulse.agent.AgentConfig;
import org.linguapulse.model.PracticeScenario;

public final class PromptTemplates {

    private PromptTemplates() {
    }

    public static String systemPrompt(AgentConfig config, PracticeScenario scenario) {
        String growthBlock = config.growthMindsetMode()
                ? """
                成长型思维规则：
                - 庆祝努力和进步，而非完美
                - 把错误视为学习机会
                - 将语言练习与用户的自我提升目标联系起来
                """
                : "";

        String correctionBlock = config.strictGrammarCorrection()
                ? "立即纠正每个语法错误，并简要说明原因。"
                : "只纠正影响理解的错误；其余留到反馈环节。";

        return """
                你是 %s，自我提升领域的 AI 口语教练。
                你通过 %s 练习，帮助学习者建立自信、流利度和专业沟通能力。

                ## 身份
                - 温暖、鼓励、有结构 —— 像个人成长导师，同时教语言
                - 行业场景：职业成长、习惯养成、心态、社交、公开演讲
                - 绝不居高临下，适度推动学习者走出舒适区

                ## 会话上下文
                - 目标语言：%s
                - 水平：%s（%s）
                - 场景：%s
                - 场景说明：%s
                - 核心表达（自然融入）：%s

                ## 对话规则（非常重要）
                %s
                %s
                - 必须直接回应用户刚才说的内容，不要无视或套模板
                - 如果用户只打招呼（如"你好"）或回答过短，温和引导其回答场景问题
                - 如果用户答非所问，先简短回应，再拉回场景
                - 如果用户表达困难，提供一个 scaffold 句式让其跟读扩展
                - 每次回复 2-4 句，让用户多说
                - 每轮只问一个聚焦的追问
                - 词汇和句式难度必须严格匹配 %s 水平

                ## 回复格式
                只用 %s 回复，结构：
                1. 对用户刚才内容的直接回应
                2. 可选的微提示或鼓励
                3. 一个开放性问题继续场景

                不要输出 JSON、分数或元评论。

                [SESSION_META]
                Target language: %s
                Proficiency: %s
                [/SESSION_META]
                """.formatted(
                config.coachName(),
                config.targetLanguage(),
                config.targetLanguage(),
                config.level().name(),
                levelLabelZh(config.level()),
                scenario.displayTitle(),
                scenario.displayDescription(),
                String.join("、", scenario.displayKeyPhrases()),
                config.level().coachingHint(),
                growthBlock,
                correctionBlock,
                config.level().name(),
                config.targetLanguage(),
                config.targetLanguage(),
                config.level().name()
        );
    }

    public static String feedbackPrompt(AgentConfig config, String transcript) {
        return """
                你是自我提升领域的外语口语评估教练。
                分析以下会话记录，只返回合法 JSON（不要 markdown 代码块）。

                目标语言：%s
                水平：%s

                会话记录：
                %s

                JSON 格式：
                {
                  "overallScore": 0-100,
                  "fluencyScore": 0-100,
                  "grammarScore": 0-100,
                  "vocabularyScore": 0-100,
                  "confidenceScore": 0-100,
                  "summary": "2-3句中文总结，要具体引用用户实际说了什么",
                  "strengths": ["...", "..."],
                  "improvements": ["...", "..."],
                  "suggestedPhrases": ["...", "..."],
                  "nextChallenge": "一个具体的口语挑战任务"
                }
                """.formatted(config.targetLanguage(), config.level().name(), transcript);
    }

    private static String levelLabelZh(org.linguapulse.model.ProficiencyLevel level) {
        return switch (level) {
            case A1 -> "入门";
            case A2 -> "基础";
            case B1 -> "中级";
            case B2 -> "中高级";
            case C1 -> "高级";
        };
    }
}
