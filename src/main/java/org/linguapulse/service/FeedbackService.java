package org.linguapulse.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.linguapulse.agent.AgentConfig;
import org.linguapulse.agent.AgentPersona;
import org.linguapulse.agent.SpeakingAgent;
import org.linguapulse.llm.LlmConfigManager;
import org.linguapulse.model.FeedbackReport;
import org.linguapulse.model.Message;
import org.linguapulse.model.PracticeScenario;
import org.linguapulse.model.ProficiencyLevel;
import org.linguapulse.model.SpeakingSession;

import java.util.List;

public class FeedbackService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final LlmConfigManager llmManager;

    public FeedbackService(LlmConfigManager llmManager) {
        this.llmManager = llmManager;
    }

    public FeedbackReport generate(SpeakingAgent agent, SpeakingSession session) {
        if (session.turnCount() == 0) {
            return buildNoParticipationReport(session);
        }

        AgentConfig config = agent.config();
        String transcript = agent.buildTranscript(session);
        String prompt = AgentPersona.buildFeedbackPrompt(config, transcript);

        List<Message> messages = List.of(
                new Message(Message.Role.SYSTEM, prompt)
        );

        String json = llmManager.createClient().chat(messages);
        return parseReport(json);
    }

    private FeedbackReport parseReport(String json) {
        try {
            String cleaned = json.trim();
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.replaceAll("^```(?:json)?\\s*", "").replaceAll("\\s*```$", "");
            }
            return MAPPER.readValue(cleaned, FeedbackReport.class);
        } catch (Exception e) {
            return new FeedbackReport(
                    70, 70, 70, 70, 70,
                    "会话已完成，暂无法生成详细反馈。",
                    List.of("完成了对话练习"),
                    List.of("继续练习以提高流利度"),
                    List.of(),
                    "下次尝试更长的回答",
                    false
            );
        }
    }

    private FeedbackReport buildNoParticipationReport(SpeakingSession session) {
        PracticeScenario scenario = session.scenario();
        ProficiencyLevel level = session.level();
        String lang = languageLabelZh(session.targetLanguage());
        List<String> phrases = scenario.displayKeyPhrases();
        if (phrases.size() > 4) {
            phrases = phrases.subList(0, 4);
        }

        return new FeedbackReport(
                0, 0, 0, 0, 0,
                "本次练习还没有你的发言记录。Alex 已经向你提问，请先对着摄像头或麦克风用"
                        + lang + "回答至少一轮，再结束练习即可获取详细评分。",
                List.of(
                        "已进入场景：「" + scenario.displayTitle() + "」",
                        "教练开场白已就绪，随时可以开口"
                ),
                List.of(
                        "点击 🎤 使用语音输入，直接对着摄像头说话",
                        "也可以打字发送第一条回复",
                        "完成至少 1 轮对话后再点「结束练习」"
                ),
                phrases,
                "下次挑战：用 " + level.name() + "（" + levelLabelZh(level) + "）水平回答 Alex 的开场问题，试着说 2～3 句话。",
                true
        );
    }

    private static String languageLabelZh(String language) {
        return switch (language) {
            case "Chinese" -> "中文";
            case "English" -> "英语";
            case "Japanese" -> "日语";
            case "French" -> "法语";
            case "Spanish" -> "西班牙语";
            default -> language;
        };
    }

    private static String levelLabelZh(ProficiencyLevel level) {
        return switch (level) {
            case A1 -> "入门";
            case A2 -> "基础";
            case B1 -> "中级";
            case B2 -> "中高级";
            case C1 -> "高级";
        };
    }
}
