# LinguaPulse 语脉

AI 外语口语 Agent，面向**自我提升行业**——帮助用户在职业成长、习惯养成、公开演讲、社交破冰等场景中练习外语口语。

## 核心能力

| 模块 | 说明 |
|------|------|
| **SpeakingAgent** | 口语对话 Agent，带完整 System Prompt 与人设 |
| **8 大场景** | 晨间习惯、面试、社交、读书会、演讲、目标设定、自信教练、晚间反思 |
| **FeedbackService** | 会话结束后生成流利度/语法/词汇/自信度评分与改进建议 |
| **LLM 接入** | 支持 OpenAI 兼容 API；无 Key 时自动使用 Mock 演示模式 |

## 快速开始

### 浏览器运行（推荐）

```powershell
# 在项目根目录执行
powershell -ExecutionPolicy Bypass -File run-web.ps1
```

浏览器将自动打开 **http://localhost:8090**

> 若 8090 端口被占用，可设置环境变量 `LINGUAPULSE_PORT=8091`

### 命令行运行

```bash
mvn clean package
java -jar target/LinguaPulse-1.0-SNAPSHOT.jar --cli
```

## 环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `OPENAI_API_KEY` | — | API 密钥，未设置则使用 Mock |
| `OPENAI_BASE_URL` | `https://api.openai.com/v1` | 兼容 OpenAI 的 API 地址 |
| `OPENAI_MODEL` | `gpt-4o-mini` | 模型名称 |

## 项目结构

```
src/main/java/org/linguapulse/
├── LinguaPulseApp.java          # CLI 入口
├── agent/
│   ├── SpeakingAgent.java       # 口语 Agent 核心
│   ├── AgentConfig.java         # 配置（语言、水平、教练名）
│   └── AgentPersona.java        # 人设与 Prompt 构建
├── prompt/
│   └── PromptTemplates.java     # System Prompt 模板
├── model/                       # 消息、场景、会话、反馈
├── service/
│   ├── ScenarioService.java     # 场景加载
│   └── FeedbackService.java     # 反馈生成
└── llm/                         # LLM 客户端
```

## Agent 人设

教练 **Alex** 是一位自我提升领域的口语导师：

- 温暖鼓励，强调成长型思维
- 每次回复 2-4 句，引导用户多说
- 围绕场景自然融入关键词汇
- 在用户卡壳时提供 scaffold 句式

## 在 Cursor 中使用

项目包含 `.cursor/skills/lingua-speaking-agent/SKILL.md`，可在 Cursor 中直接调用该 Skill 进行口语陪练。

## 后续扩展

- 语音输入/输出（ASR + TTS）
- Web / 移动端 UI
- 学习进度持久化与复习计划
- 多 Agent 角色（面试官、书友、演讲教练）
