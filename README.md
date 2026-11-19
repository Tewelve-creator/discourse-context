# LinguaPulse 语脉

AI 外语口语陪练 Agent，面向**自我提升**场景——在职业成长、习惯养成、公开演讲、社交破冰等情境中练习外语口语。

- 8 个真实场景 × 5 种语言 × A1–C1 分级开场白
- 网页端对话，支持打字输入与浏览器语音输入（Web Speech API）
- 会话结束生成五维评分反馈；未配置 API Key 时自动进入 Mock 演示模式

## 核心能力

| 模块 | 说明 |
|------|------|
| **SpeakingAgent** | 口语对话 Agent，按场景 / 语言 / 水平注入 System Prompt |
| **8 大场景** | 晨间习惯、面试、社交、读书会、演讲、目标设定、自信教练、晚间反思 |
| **5 级水平** | A1–C1，每级带不同的教练引导策略 |
| **LevelContentService** | 按「场景 × 语言 × 水平」加载开场白，缺失时逐级回退 |
| **FeedbackService** | 生成综合 / 流利度 / 语法 / 词汇 / 自信度五维评分与改进建议 |
| **Web UI** | 场景选择、摄像头预览、语音输入、页面内配置 AI |
| **LLM 接入** | 支持任意 OpenAI 兼容 API；无 Key 时自动使用 Mock 演示模式 |

## 快速开始

### 方式一：一键脚本（推荐）

```powershell
# 在项目根目录执行
powershell -ExecutionPolicy Bypass -File run-web.ps1
```

脚本会自动下载 Jackson 依赖到 `.deps/`、用 `javac` 编译到 `target/classes/`、复制资源文件，然后启动服务并打开浏览器。

访问 **http://localhost:8090**

> 该脚本固定打开 8090；如需换端口请用方式二，并设置环境变量 `LINGUAPULSE_PORT`。

### 方式二：Maven 打包

```bash
mvn clean package

# Web 模式（默认，自动打开浏览器）
java -jar target/LinguaPulse-1.0-SNAPSHOT.jar

# 不自动打开浏览器
java -jar target/LinguaPulse-1.0-SNAPSHOT.jar --no-browser
```

### 方式三：命令行交互模式

```bash
java -cp target/LinguaPulse-1.0-SNAPSHOT.jar org.linguapulse.LinguaPulseApp
```

按提示选择水平与场景，用目标语言回复；输入 `/end` 结束并获取反馈，输入 `/quit` 直接退出。

> 打包后的 jar 默认主类是 `LinguaPulseWebApp`，所以 CLI 模式需要显式指定 `org.linguapulse.LinguaPulseApp`。

## 配置 AI

生效优先级：**页面内配置 / `config.properties` > 环境变量 > 内置默认值**（`config.properties` 中的同名项会覆盖环境变量）。

### 方式一：网页内配置（最简单）

打开页面右侧「AI 配置」标签，选择服务商、填入 API Key，点击「保存并启用 AI」立即生效，无需重启。

### 方式二：config.properties

```powershell
Copy-Item config.properties.example config.properties
```

```properties
llm.provider=deepseek
llm.api.key=sk-your-deepseek-key
llm.base.url=https://api.deepseek.com/v1
llm.model=deepseek-chat
```

`llm.provider` 支持 `deepseek` / `openai` / `qwen`，填这三者之一时会自动填充对应的 `baseUrl` 与 `model`。

### 方式三：环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `OPENAI_API_KEY` | — | API 密钥，未设置则使用 Mock 演示模式 |
| `OPENAI_BASE_URL` | `https://api.deepseek.com/v1` | 兼容 OpenAI 的 API 地址 |
| `OPENAI_MODEL` | `deepseek-chat` | 模型名称 |
| `LLM_PROVIDER` | `openai` | 提供商标识，仅在设置了 `OPENAI_API_KEY` 时读取 |
| `LINGUAPULSE_PORT` | `8090` | Web 服务端口 |

## 8 大场景

| 场景 ID | 场景 | 分类 | 推荐水平 |
|---|---|---|---|
| `morning-routine` | 晨间习惯打卡 | 习惯养成 | A2 |
| `career-interview` | 职业成长面试 | 职业发展 | B1 |
| `networking-event` | 自我提升峰会社交 | 社交破冰 | B1 |
| `book-club` | 读书会讨论 | 心态思维 | B2 |
| `public-speaking` | TED 式演讲排练 | 公开演讲 | C1 |
| `goal-setting` | 季度目标设定 | 目标规划 | B2 |
| `confidence-coaching` | 自信表达教练 | 心态思维 | B1 |
| `daily-reflection` | 晚间反思日记 | 习惯养成 | A2 |

## 水平分级

| 等级 | 标签 | 教练引导策略 |
|---|---|---|
| A1 | Beginner | 用最简单的词和短句 |
| A2 | Elementary | 使用日常词汇，温和纠错 |
| B1 | Intermediate | 鼓励更长的回答，少量引入习语 |
| B2 | Upper Intermediate | 以细腻方式讨论抽象的自我提升话题 |
| C1 | Advanced | 用专业与修辞性语言挑战用户 |

## 支持语言与开场白

- **英语、中文**：覆盖全部 8 个场景 × 5 个等级
- **日语、法语、西班牙语**：覆盖 B1/B2（晨间习惯场景额外覆盖 A1/A2/C1）

找不到对应开场白时按顺序回退：目标语言 + 等级 → 英语 + 等级 → 英语 B1 → 该语言任一等级 → 场景自带开场白。

## HTTP API

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/scenarios` | 获取全部场景 |
| POST | `/api/session/start` | 开始会话，body: `{language, level, scenarioId}` |
| POST | `/api/session/chat` | 发送消息，body: `{sessionId, message}` |
| POST | `/api/session/feedback` | 生成反馈，body: `{sessionId}` |
| GET | `/api/status` | 服务与 LLM 状态（端口、provider、model、Key 掩码） |
| GET / POST | `/api/llm/config` | 读取 / 更新 LLM 配置（`provider, apiKey, baseUrl, model`） |

## 项目结构

```
src/main/
├── java/org/linguapulse/
│   ├── LinguaPulseApp.java              # CLI 交互入口
│   ├── LinguaPulseWebApp.java           # Web 入口（默认主类）
│   ├── agent/
│   │   ├── SpeakingAgent.java           # 口语 Agent 核心
│   │   ├── AgentConfig.java             # 目标语言、水平、教练名、纠错与成长型思维开关
│   │   └── AgentPersona.java            # 人设与 Prompt 构建
│   ├── prompt/
│   │   └── PromptTemplates.java         # System Prompt 与反馈 Prompt 模板
│   ├── model/                           # Message、PracticeScenario、SpeakingSession、
│   │                                    # FeedbackReport、ProficiencyLevel
│   ├── service/
│   │   ├── ScenarioService.java         # 场景加载与筛选
│   │   ├── LevelContentService.java     # 分级 / 多语言开场白
│   │   └── FeedbackService.java         # 五维评分反馈生成
│   ├── llm/
│   │   ├── LlmClient.java               # 客户端接口
│   │   ├── LlmClientFactory.java        # 客户端工厂
│   │   ├── LlmConfigManager.java        # 环境变量 + config.properties 配置源
│   │   ├── OpenAiCompatibleClient.java  # OpenAI 兼容实现
│   │   └── MockLlmClient.java           # 无 Key 时的分级演示回复
│   └── web/
│       ├── WebServer.java               # 内置 HttpServer 与 6 个 API
│       └── SessionRegistry.java         # 会话注册表
└── resources/
    ├── scenarios/
    │   ├── self-improvement-scenarios.json   # 8 个场景定义
    │   ├── level-openings.json               # 英 / 中 × 8 场景 × A1–C1 开场白
    │   └── multilingual-openings.json        # 日 / 法 / 西开场白
    └── web/                                  # index.html、app.js、app.css
```

## Agent 人设

教练 **Alex** 是一位自我提升领域的口语导师：

- 温暖鼓励，强调成长型思维
- 每次回复 2–4 句，引导用户多说
- 围绕场景自然融入关键词汇
- 在用户卡壳时提供一个 scaffold 句式让其跟读扩展

## 反馈维度

会话结束后由 `FeedbackService` 生成 `FeedbackReport`：综合得分、流利度、语法、词汇、自信度（各 0–100），并附会话摘要、亮点、待改进项、核心短语与下次挑战。

若整场会话用户一轮都没发言，则返回一份引导性报告而不是评分，提示先开口练习。

## 后续扩展

- 语音输出（TTS）——语音输入已支持浏览器 Web Speech API
- 学习进度持久化与复习计划
- 多 Agent 角色（面试官、书友、演讲教练）
- 移动端适配
