package org.linguapulse;

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

import java.util.List;
import java.util.Scanner;

public class LinguaPulseApp {

    public static void main(String[] args) {
        printBanner();

        LlmConfigManager llmManager = LlmConfigManager.getInstance();
        printLlmMode(llmManager);

        ScenarioService scenarioService = new ScenarioService();
        LevelContentService levelContentService = new LevelContentService();
        FeedbackService feedbackService = new FeedbackService(llmManager);

        Scanner scanner = new Scanner(System.in);

        String language = prompt(scanner, "目标语言 (默认 English): ", "English");
        ProficiencyLevel level = selectLevel(scanner);
        PracticeScenario scenario = selectScenario(scanner, scenarioService);

        AgentConfig config = AgentConfig.defaults(language, level);
        SpeakingAgent agent = new SpeakingAgent(config, llmManager);
        String openingLine = levelContentService.openingLine(scenario, language, level);
        SpeakingSession session = agent.startSession(scenario, openingLine);

        printSessionHeader(config, scenario);
        System.out.println("\n🤖 Alex: " + openingLine);
        System.out.println("\n输入你的口语回复（输入 /end 结束并获取反馈，/quit 直接退出）\n");

        while (true) {
            System.out.print("🎤 You: ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("/quit")) {
                System.out.println("\n再见！Keep growing. 🌱");
                return;
            }
            if (input.equalsIgnoreCase("/end")) {
                break;
            }
            if (input.isEmpty()) {
                continue;
            }

            String reply = agent.reply(session, input);
            System.out.println("\n🤖 Alex: " + reply + "\n");
        }

        if (session.turnCount() == 0) {
            System.out.println("\n还没有对话内容，下次再来练习吧！");
            return;
        }

        System.out.println("\n⏳ 正在生成学习反馈...\n");
        FeedbackReport report = feedbackService.generate(agent, session);
        printFeedback(report);
    }

    private static void printBanner() {
        System.out.println("""
                
                ╔══════════════════════════════════════════╗
                ║           LinguaPulse 语脉               ║
                ║   AI 外语口语 Agent · 自我提升行业       ║
                ╚══════════════════════════════════════════╝
                """);
    }

    private static void printLlmMode(LlmConfigManager manager) {
        if (manager.isLiveMode()) {
            System.out.println("✅ AI 智能对话已启用（" + manager.status().get("provider") + "）\n");
        } else {
            System.out.println("ℹ️  演示模式。在项目根目录创建 config.properties 或设置 OPENAI_API_KEY 启用 AI。\n");
        }
    }

    private static ProficiencyLevel selectLevel(Scanner scanner) {
        ProficiencyLevel[] levels = ProficiencyLevel.values();
        System.out.println("\n选择水平:");
        for (int i = 0; i < levels.length; i++) {
            System.out.printf("  %d. %s — %s%n", i + 1, levels[i].name(), levels[i].label());
        }
        while (true) {
            System.out.print("请输入编号 (默认 3/B1): ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return ProficiencyLevel.B1;
            }
            try {
                int idx = Integer.parseInt(input) - 1;
                if (idx >= 0 && idx < levels.length) {
                    return levels[idx];
                }
            } catch (NumberFormatException ignored) {
            }
            System.out.println("无效选择，请重试。");
        }
    }

    private static PracticeScenario selectScenario(Scanner scanner, ScenarioService service) {
        List<PracticeScenario> scenarios = service.all();
        System.out.println("\n选择练习场景:");
        for (int i = 0; i < scenarios.size(); i++) {
            PracticeScenario s = scenarios.get(i);
            System.out.printf("  %d. [%s] %s (%s)%n", i + 1, s.category(), s.title(), s.recommendedLevel().name());
        }
        while (true) {
            System.out.print("请输入编号 (默认 1): ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return scenarios.get(0);
            }
            try {
                int idx = Integer.parseInt(input) - 1;
                if (idx >= 0 && idx < scenarios.size()) {
                    return scenarios.get(idx);
                }
            } catch (NumberFormatException ignored) {
            }
            System.out.println("无效选择，请重试。");
        }
    }

    private static void printSessionHeader(AgentConfig config, PracticeScenario scenario) {
        System.out.printf("%n📋 场景: %s%n", scenario.title());
        System.out.printf("📝 %s%n", scenario.description());
        System.out.printf("🎯 语言: %s | 水平: %s | 教练: %s%n", config.targetLanguage(), config.level().name(), config.coachName());
    }

    private static void printFeedback(FeedbackReport report) {
        System.out.println("══════════════════ 学习反馈 ══════════════════");
        System.out.printf("综合得分: %d  |  流利度: %d  |  语法: %d  |  词汇: %d  |  自信度: %d%n",
                report.overallScore(), report.fluencyScore(), report.grammarScore(),
                report.vocabularyScore(), report.confidenceScore());
        System.out.println("\n📊 " + report.summary());

        printList("✅ 优势", report.strengths());
        printList("📈 待提升", report.improvements());
        printList("💡 推荐表达", report.suggestedPhrases());

        System.out.println("\n🎯 下次挑战: " + report.nextChallenge());
        System.out.println("═══════════════════════════════════════════════\n");
    }

    private static void printList(String title, List<String> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        System.out.println("\n" + title + ":");
        for (String item : items) {
            System.out.println("  • " + item);
        }
    }

    private static String prompt(Scanner scanner, String label, String defaultValue) {
        System.out.print(label);
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? defaultValue : input;
    }
}
