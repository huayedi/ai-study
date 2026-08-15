# ERP AI 学习笔记（订正版 · 结合代码）

> 用途：复习 + 打卡跟踪。以当前仓库代码为准。  
> 总计划：[LEARNING_PLAN.md](./LEARNING_PLAN.md)  
> 工程目录：`erp-ai-assistant/`  
> **学习偏好：教材式讲义为主**（见 `docs/lessons/`）。  
> **学习边界：通用 ERP 口径 + 纯学习仓库；不依赖公司手册，不落到公司项目。**  
> **30 天总计划（逐日）：** [lessons/MONTH_30_DAY_PLAN.md](./lessons/MONTH_30_DAY_PLAN.md)  
> **第 1 月合订详版（非概述）：** [lessons/MONTH1_DAY1-30_COMBINED.md](./lessons/MONTH1_DAY1-30_COMBINED.md) · [MONTH1.md](./MONTH1.md)

**你的当前配置（可自行改）：**
- Provider：`openai-compatible`
- Base URL：`https://api.deepseek.com`
- Model：按你本地实际填写（如 `deepseek-v4-pro` / `deepseek-chat`）
- 配置入口：环境变量优先，其次 `application.yml`

---

## 进度总览

| 天 | 主题 | 状态 | 日期 |
|---|---|---|---|
| Day1 | 概念 + 接通真实 API | ✅ 完成 | |
| Day2 | 消息组装链路 + 会话/解析/重试 | ✅ 完成（含 4 个实验） | |
| Day3 | Temperature / Token / 成本 / 坏 case | ✅ 完成（坏 case 暂缓，见日志） | |
| Day4 | 系统提示词工程（ERP 术语） | ✅ 完成 | |
| Day5 | 多轮与裁剪、问题清单补齐 | ✅ 完成 | |
| Day6 | Prompt 深化：few-shot + 规则解释 | ✅ 完成 | |
| Day7 | 20 题扫弱项 + 字段校验解释 | ✅ 完成 | |
| Day8 | Prompt 收口 + RAG 入门（讲义） | ✅ 完成 | |
| Day9 | RAG 最小闭环（关键词检索学习版） | ✅ 完成 | |
| Day10 | 切分策略深入（仅讲义） | ✅ 完成（自行改码按已完成计） | |
| Day11 | Embedding 与向量检索原理（仅讲义） | ✅ 完成 | |
| Day12 | 向量检索实现（讲义·自行编码） | ✅ 按进度继续 | |
| Day13～30 | 合订详版（非概述） | ✅ 已交付 | → [MONTH1_DAY1-30_COMBINED.md](./lessons/MONTH1_DAY1-30_COMBINED.md) |
| 第 1 月 Day1～30 | 整月合订详版 | ✅ 已交付 | → [MONTH1_DAY1-30_COMBINED.md](./lessons/MONTH1_DAY1-30_COMBINED.md) |
| 第 2 月 Day1～30 | 逐日详版（与第1月同级） | ✅ 已交付 | → [MONTH2_DAY1-30_COMBINED.md](./lessons/MONTH2_DAY1-30_COMBINED.md) · [MONTH2.md](./MONTH2.md) |
| 第 3 月 Day1～30 | 逐日详版（与第1月同级） | ✅ 已交付 | → [MONTH3_DAY1-30_COMBINED.md](./lessons/MONTH3_DAY1-30_COMBINED.md) · [MONTH3.md](./MONTH3.md) |
| 第 4 月 Day1～30 | 逐日详版（与第1月同级） | ✅ 已交付 | → [MONTH4_DAY1-30_COMBINED.md](./lessons/MONTH4_DAY1-30_COMBINED.md) · [MONTH4.md](./MONTH4.md) |
| 第 5 月 Day1～30 | 逐日详版（与第1月同级） | ✅ 已交付 | → [MONTH5_DAY1-30_COMBINED.md](./lessons/MONTH5_DAY1-30_COMBINED.md) · [MONTH5.md](./MONTH5.md) |
| 第 6 月 Day1～30 | 逐日详版（与第1月同级） | ✅ 已交付 | → [MONTH6_DAY1-30_COMBINED.md](./lessons/MONTH6_DAY1-30_COMBINED.md) · [MONTH6.md](./MONTH6.md) |
| 第 7 月 Day1～30 | 逐日详版（与第1月同级） | ✅ 已交付 | → [MONTH7_DAY1-30_COMBINED.md](./lessons/MONTH7_DAY1-30_COMBINED.md) · [MONTH7.md](./MONTH7.md) |
| 第 8 月完整教材 | 工作流可视化（非 30 天） | ✅ 已交付 | → [MONTH8_WORKFLOW_VIZ_COMPLETE.md](./lessons/MONTH8_WORKFLOW_VIZ_COMPLETE.md) · [MONTH8.md](./MONTH8.md) |
| Web 前端完整教材 | Vue 3 控制台（非 30 天） | ✅ 已交付 | → [WEB_VUE_COMPLETE.md](./lessons/WEB_VUE_COMPLETE.md) · [WEB.md](./WEB.md) |
| 课程连贯桥接 | HITL 时间线 / 双控制台 / 旧菜单对照 | ✅ 已补 | → [CURRICULUM_CONTINUITY.md](./lessons/CURRICULUM_CONTINUITY.md) |
| 全部方向总览 | 对勾清单 + 分支先后图 | ✅ 已补 | → [CURRICULUM_DIRECTIONS.md](./CURRICULUM_DIRECTIONS.md) |
| 作品集模板 | PORTFOLIO | ✅ 已补 | → [PORTFOLIO.md](./PORTFOLIO.md) |

**Day2 实验：**
- ✅ 多轮：第一次不带 `sessionId`，第二次带上追问
- ✅ 改一次 `erp-system-prompt.txt` 并对比同一问题
- ✅ 高风险问题观察 `needHuman=true`（如“帮我直接过账”）
- ✅ `samples/week1-questions.md` 累计 ≥ 10 条

---

## Day1｜核心概念（订正）

### 1. `system` / `user` / `assistant`

| role | 作用 | 本项目谁写入 |
|---|---|---|
| `system` | 设定角色、硬性规则、输出格式 | 每次请求由 `ChatService#buildMessages` 从提示词文件加载后放入首位 |
| `user` | 用户输入；重试时的纠错提示也用 user | 本轮来自请求；历史来自 `SessionStore` |
| `assistant` | 模型上一轮回复 | 成功后写入 `SessionStore`，供下一轮作为历史 |

要点：Chat Completions 模式下，**服务端不默认长期记住会话**；多轮靠客户端每次把历史再发回去。

对应代码：

```135:140:erp-ai-assistant/src/main/java/com/erp/ai/service/ChatService.java
    private List<ChatMessage> buildMessages(String sessionId, String userMessage) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("system", systemPromptLoader.getSystemPrompt()));
        messages.addAll(sessionStore.getHistory(sessionId));
        messages.add(new ChatMessage("user", userMessage));
        return messages;
    }
```

系统提示词文件：`erp-ai-assistant/src/main/resources/prompts/erp-system-prompt.txt`

---

### 2. Token、费用、上下文

**Token**  
模型计费/长度单位。粗算（仅直觉，以官方 tokenizer 为准）：
- 中文：约 **1 个汉字 ≈ 1～2 个 token**
- 英文：常见词多约 **1 词 ≈ 1 token**

**费用**  
大致 = 输入 token × 输入单价 + 输出 token × 输出单价  
- 输入：system + 历史 + 本轮 user（含重试纠错提示）  
- 输出：本次模型生成内容  

**上下文（Context）**  
当前要发给模型的全部 `messages`。会话越长，输入 token 越多，费用与延迟通常越高。  
模型推理依赖你这次请求带上的上下文，**不是**在云端自动存着你们公司的聊天记录（除非另做记忆系统）。

本项目成本仅为学习估算，见 `ChatService#estimateCost`，单价来自配置：

```40:42:erp-ai-assistant/src/main/resources/application.yml
  # 粗略成本估算（美元 / 1K tokens），可按实际模型调整
  price-input-per-1k: 0.00015
  price-output-per-1k: 0.0006
```

---

### 3. 配置在哪？`provider` 是什么？

配置项（`ai.*`）：

```32:44:erp-ai-assistant/src/main/resources/application.yml
ai:
  provider: ${AI_PROVIDER:mock} # mock | openai-compatible | deepseek | openai
  base-url: ${AI_BASE_URL:https://api.openai.com/v1}
  api-key: ${AI_API_KEY:}
  model: ${AI_MODEL:gpt-4o-mini}
  temperature: 0.2
  timeout-ms: 30000
  max-retries: 2
  # ...
  session:
    max-messages: 20
```

| 项 | 含义 |
|---|---|
| `provider` | **接入协议/实现选择**，不是厂商品牌名。真实调用应为 `openai-compatible`（DeepSeek 也走兼容协议） |
| `base-url` | 网关根地址；代码会拼 `/chat/completions` |
| `api-key` | Bearer Token；**不要提交到 Git**，用环境变量 |
| `model` | 模型名，以服务商控制台为准 |
| `max-retries` | 失败后额外重试次数；总尝试 = `maxRetries + 1` |
| `session.max-messages` | 历史消息条数上限，超出从队头删 |

最终请求 URL：

```text
{baseUrl}/chat/completions
例：https://api.deepseek.com/chat/completions
```

对应拼接：

```83:83:erp-ai-assistant/src/main/java/com/erp/ai/client/OpenAiCompatibleLlmClient.java
        String url = trimTrailingSlash(properties.getBaseUrl()) + "/chat/completions";
```

**复习口令：**  
`provider=openai-compatible` 表示“按 OpenAI 风格 HTTP 协议调模型”，DeepSeek/OpenAI/部分国产兼容模式都可能用同一套客户端。

---

## Day2｜消息组装与调用链路（订正 · 对照代码）

### 总览图

```text
POST /api/ai/chat
  Body: { "sessionId"?: "...", "message": "..." }
        │
        ▼
ChatController
        │
        ▼
ChatService.chat
  1. traceId = UUID
  2. sessionId = 传入或新建
  3. messages = [system] + history + [user]
  4. loop 最多 maxRetries+1 次:
       LlmClient.chat(messages)  ──HTTP──►  {baseUrl}/chat/completions
       ReplyParser.parse(content)
       成功 → SessionStore.append → 记日志 → 返回 ChatResponse
       失败 → messages 追加纠错 user → 重试
  5. 仍失败 → 记失败日志 → 抛异常
```

### 分步说明

#### Step A｜入口

- 类：`com.erp.ai.controller.ChatController`
- 路径：`POST /api/ai/chat`
- 请求 DTO：`ChatRequest{ sessionId, message }`  
  - `message` 必填  
  - `sessionId` 可选；不传则服务端新建并在响应里返回

#### Step B｜编排（ChatService）

关键点（订正你笔记里的字段名）：

| 你原来的写法 | 正确写法 |
|---|---|
| `{role, message}` | `{ "role", "content" }` |
| `ai.max_retries` | YAML：`ai.max-retries` → Java：`maxRetries` |
| 直接把 HTTP 响应当返回 | 分三层：HTTP 原始 JSON → `LlmResult` → `ChatResponse` |

成功才写历史（避免脏输出污染下一轮）：

```81:86:erp-ai-assistant/src/main/java/com/erp/ai/service/ChatService.java
                sessionStore.append(
                        sessionId,
                        new ChatMessage("user", request.getMessage()),
                        new ChatMessage("assistant", result.getContent())
                );
```

#### Step C｜HTTP 调用（OpenAiCompatibleLlmClient）

请求体要点：

```json
{
  "model": "...",
  "temperature": 0.2,
  "response_format": { "type": "json_object" },
  "messages": [
    { "role": "system", "content": "..." },
    { "role": "user", "content": "..." }
  ]
}
```

请求头：
- `Content-Type: application/json`
- `Authorization: Bearer <apiKey>`

从响应取值：
- 文本：`choices[0].message.content`
- 用量：`usage.prompt_tokens` / `usage.completion_tokens`

封装为项目内 `LlmResult(content, model, promptTokens, completionTokens)`。

#### Step D｜JSON 解析（ReplyParser）

必填：`answer`、`need_human`（兼容 `needHuman`）  
可选：`suggested_doc_type`、`required_fields`、`confidence`  
兼容：Markdown 代码块包裹、snake_case / camelCase

解析结果 → `AssistantReply`，再放进对外的 `ChatResponse.reply`。

#### Step E｜会话（SessionStore）

- 第 1–2 周：内存 `ConcurrentHashMap`（重启丢失）
- 历史按 `session.max-messages` 从队头裁剪，控制后续输入 token

#### Step F｜对外响应（ChatResponse）

```text
traceId          一次请求追踪 ID
sessionId        多轮续聊用这个
provider         如 openai-compatible
model            实际命中模型名
reply            AssistantReply（业务结构化结果）
usage            prompt/completion/totalTokens + estimatedCostUsd
latencyMs        耗时
attempts         本轮尝试次数（含重试）
```

注意：对外 JSON 里布尔字段常是 Java Bean 风格 `needHuman`；模型侧 Schema 要求的是 `need_human`。解析器两者都认。

---

## 你发现的重点：重试与 Token

### 现象（正确）

解析/调用失败时，会在**本次请求的内存 messages** 末尾追加纠错 user，然后整包重发：

```146:152:erp-ai-assistant/src/main/java/com/erp/ai/service/ChatService.java
    private List<ChatMessage> withRepairHint(List<ChatMessage> messages, String error) {
        List<ChatMessage> repaired = new ArrayList<>(messages);
        repaired.add(new ChatMessage(
                "user",
                "上一次输出不符合要求（" + error + "）。请重新只输出合法 JSON，字段必须包含 answer 与 need_human。"
        ));
        return repaired;
    }
```

因此：
- 每次重试 ≈ 再付一次「当前（已变长的）上下文」输入成本 + 新的输出成本  
- 若错误原因不变（如 401 Key 错），重试往往无效，属于空耗  

### 容易漏的一点（补充）

- 纠错消息只存在于**这一次 HTTP 请求的重试循环**  
- **失败内容默认不会写入 `SessionStore`**；只有解析成功才 `append`  
- 所以：会费钱，但不一定污染长期会话历史  

### 后续改进方向（先记结论，本周不必改代码）

1. 鉴权/配置类错误快速失败，不走“请重输出 JSON”逻辑  
2. 相同错误短路，避免无效追加  
3. 重试时考虑只保留 system + 最近轮次 + schema 提示  
4. 最终失败返回降级结构，而不是烧满重试  

---

## 关键文件索引（复习导航）

| 文件 | 复习看什么 |
|---|---|
| `controller/ChatController.java` | API 入口 |
| `service/ChatService.java` | 编排、重试、计费、组装 messages |
| `service/SessionStore.java` | 多轮与裁剪 |
| `service/ReplyParser.java` | JSON 提取与必填校验 |
| `client/OpenAiCompatibleLlmClient.java` | 真实 HTTP 协议 |
| `client/MockLlmClient.java` | 无 Key 时的本地假模型 |
| `resources/application.yml` | provider/baseUrl/model/重试/单价 |
| `resources/prompts/erp-system-prompt.txt` | 角色与 JSON Schema |
| `samples/week1-questions.md` | 真实问题收集 |

---

## 自测题（复习用，不看笔记答）

1. 为什么多轮要传 `sessionId`？不传会怎样？  
2. 一次请求的 messages 固定顺序是什么？  
3. `provider` 和 `model` 区别？  
4. 解析失败时，历史会话会被写入失败草稿吗？  
5. `max-retries: 2` 最多请求模型几次？  
6. DeepSeek 最终 URL 如何由 `base-url` 拼出？  
7. 为什么长会话更贵？裁剪历史在哪个类？  

（建议答案见文末折叠区）

<details>
<summary>自测参考答案</summary>

1. 用来定位同一条会话历史；不传会新建 session，模型看不到前文。  
2. system → 历史（user/assistant…）→ 当前 user。  
3. provider 选客户端/协议实现；model 是具体模型名。  
4. 不会；成功解析后才 `SessionStore.append`。  
5. 3 次（2+1）。  
6. `trim(baseUrl) + "/chat/completions"`。  
7. 每次重发全部上下文，输入 token 变多；裁剪在 `SessionStore#trim`。  

</details>

---

## 每日打卡模板（复制到下方「学习日志」）

```text
### DayX（日期）
- 今日目标：
- 实际完成：
- 对照代码看过的类：
- 实验现象（请求/关键返回字段）：
- 疑问 / 明天要弄清：
- 用时：
```

## 学习日志

### Day1
- 今日目标：概念 + 接通 API  
- 实际完成：DeepSeek 真实调用跑通；理清 role / token / context / provider  
- 对照代码：`application.yml`、`OpenAiCompatibleLlmClient`  
- 备注：baseUrl=`https://api.deepseek.com`，provider=`openai-compatible`

### Day2
- 今日目标：读通消息组装链路 + 4 实验  
- 实际完成：链路梳通；多轮 session、改提示词、高风险 needHuman、问题清单均已完成；发现重试费 token 问题  
- 对照代码：`ChatService`、`SessionStore`、`ReplyParser`、`OpenAiCompatibleLlmClient`

### Day3
- 今日目标：Temperature / Token / 成本 / 超时与坏 case  
- 实际完成：见下；探测题未打出明显乱编/越权，坏 case 本暂缓  
- 固定观察题：`创建采购订单应该如何去设计`
- Temperature：
  - `0.0`：更保守，偏常规步骤 + 人工核查
  - `0.3`：更贴 ERP，会连带收货/发票等后续方案（当前更推荐）
  - `1.0`：更复杂，易掺数据权限/字段权限等发散内容
  - 小结：本题差异存在但不大；ERP 助手默认可继续用低温度（0.2～0.3）
- Token/成本：短问少、长答多、带 `sessionId` 因重发历史会持续升高 —— 理解正确
- 超时/重试：`timeout-ms=30000` 管 HTTP 连接+读取；总次数=`max-retries+1`；**401 重试无意义** —— 正确
- 坏 case：DeepSeek 成熟题上表现稳；探测题库保留在 `day3-experiments.md`，第 4–5 周改提示词/上 RAG 前再回归测一轮
- 对照代码：`application.yml`（temperature/timeout/retries）、`AppConfig`/`OpenAiCompatibleLlmClient`、`ChatService`、`SessionStore`

### Day4
- 今日目标：术语表 + 系统提示词工程  
- 已为你生成：
  - `erp-ai-assistant/src/main/resources/prompts/erp-glossary.md`（完整最小术语表，可填「本系统别名」）
  - `erp-ai-assistant/src/main/resources/prompts/erp-system-prompt.txt`（已嵌入术语摘要 + 回答结构）
- 你需要做：重启应用，用下面 3 题做改后效果验证（并可选与印象中的改前对比）
  1. 创建采购订单应该如何去设计
  2. 采购订单和采购入库单有什么区别
  3. week1 清单里一道真实题
- 实际完成：（你测完后补）

### Day5
- 今日目标：多轮连贯、历史裁剪、问题清单补到 20、第 1 周收口  
- 实验表：`docs/day5-experiments.md`  
- 实际完成：
  - 多轮：能跟上文、术语正确、`totalTokens` 持续升高  
  - 裁剪：`max-messages=4` 会遗忘更早对答；理解队头删除、控 token  
  - 问题清单：`samples/week1-questions.md` 已补齐 20 条（本系统术语）  
  - 自检：完成  
- 收口：第 1～2 周最小 LLM 闭环学习完成，下一段进入第 3 周 Prompt 深化

### Day6
- 今日目标：few-shot + 规则解释风格  
- 实验表：`docs/day6-experiments.md`  
- 相关文件：`prompts/erp-few-shot.txt`、`SystemPromptLoader`（会追加 few-shot）  
- 实际完成：
  - few-shot 已生效  
  - 5 道规则题打分完成  
  - 改过 1 条 few-shot，效果变好  
  - 拒答抽测有效（不编造实时库存）

### Day7
- 今日目标：20 题速扫弱项 + 字段校验解释 few-shot + 低分回修  
- 实验表：`docs/day7-experiments.md`  
- 实际完成：
  - 20 题扫弱项完成  
  - 字段校验类 few-shot 已补充（见 `erp-few-shot.txt` 示范 4/5）  
  - 弱项回修完成  
- 备注：示范 4 拒绝直接建单；示范 5 必填字段校验解释

### Day8
- 形式：讲义阅读  
- 讲义：`docs/lessons/day08-prompt-wrap-and-rag-intro.md`  
- 边界：无公司手册；通用 ERP 教材；纯学习  
- 状态：✅ 进入 Day9

### Day9
- 讲义：`docs/lessons/day09-rag-minimum-loop.md`  
- 代码：`com.erp.ai.rag` + `POST /api/ai/rag/ask`  
- 状态：✅

### Day10
- 讲义：`docs/lessons/day10-chunking-strategies.md`  
- 状态：✅（自行对照改码未提交也按完成计）

### Day11
- 讲义：`docs/lessons/day11-embedding-vector-retrieval.md`  
- 状态：✅

### Day12
- 讲义：`docs/lessons/day12-vector-retriever-impl.md`  
- 状态：✅（自行实现按个人进度）

### Day13～30 / 第 1 月合订
- **合订详版（非概述）：** `docs/lessons/MONTH1_DAY1-30_COMBINED.md`  
- 入口：`docs/MONTH1.md`  
- 状态：✅ 已交付（Day8～12 全文收录；Day13～30 逐日加厚）

### 第 2 个月
- **逐日详版合订（与第1月同级，非概述）：** `docs/lessons/MONTH2_DAY1-30_COMBINED.md`  
- 入口：`docs/MONTH2.md`  
- 状态：✅ 已交付

### 第 3 个月
- **逐日详版合订（与第1月同级，非概述）：** `docs/lessons/MONTH3_DAY1-30_COMBINED.md`  
- 入口：`docs/MONTH3.md`  
- 主题：PgStore/reindex/质量日志、多节点工作流+审计、评测跑次与 baseline、可选 OCR、作品集  
- 状态：✅ 已交付

### 第 4 个月
- **逐日详版合订（与第1月同级，非概述）：** `docs/lessons/MONTH4_DAY1-30_COMBINED.md`  
- 入口：`docs/MONTH4.md`  
- 主题：模拟 ACL、反馈飞轮、多租户 RAG、学习控制台正式化  
- 状态：✅ 已交付

### 第 5 个月
- **逐日详版合订（与第1月同级，非概述）：** `docs/lessons/MONTH5_DAY1-30_COMBINED.md`  
- 入口：`docs/MONTH5.md`  
- 主题：受控写入与模拟过账（假账本 + 强制 HITL + 审计）  
- 状态：✅ 已交付

### 第 6 个月
- **逐日详版合订（与第1月同级，非概述）：** `docs/lessons/MONTH6_DAY1-30_COMBINED.md`  
- 入口：`docs/MONTH6.md`  
- 主题：真适配器接口稳定化（Port/Adapter + Fake/Sandbox + 契约测试）  
- 状态：✅ 已交付  

### 第 7 个月
- **逐日详版合订（与第1月同级，非概述）：** `docs/lessons/MONTH7_DAY1-30_COMBINED.md`  
- 入口：`docs/MONTH7.md`  
- 主题：规则引擎（规则先于模型；可版本化/评测/审计）  
- 状态：✅ 已交付

### 第 8 个月
- **完整章节式教材（非 30 天）：** `docs/lessons/MONTH8_WORKFLOW_VIZ_COMPLETE.md`  
- 入口：`docs/MONTH8.md`  
- 主题：工作流可视化（节点/合法边/当前高亮/审计回放）  
- 状态：✅ 已交付

### Web 前端轨道（独立 · 完整教材）
- **完整章节式教材（非 30 天）：** `docs/lessons/WEB_VUE_COMPLETE.md`  
- 入口：`docs/WEB.md`  
- 主题：**Vue 3 + Vite** 学习控制台（Chat/RAG/Flow/Eval/ACL头/反馈）、构建与彩排  
- 状态：✅ 已交付（已取消 WEB-D1～30 逐日结构）  
- 约定：骨架自落到 `erp-ai-console/`；Vite proxy 对接后端  

---

## 修订记录

| 日期 | 说明 |
|---|---|
| 2026-08-13 | 根据 Day1/Day2 个人笔记订正，并绑定当前仓库代码路径 |
