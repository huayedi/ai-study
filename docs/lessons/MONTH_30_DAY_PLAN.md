# 30 天学习总计划（逐日教材大纲）

> **学习方式：** 每天以讲义阅读为主（`docs/lessons/`），代码对照为辅；不默认布置填表作业。  
> **口径：** 常规 ERP 通用知识 + 本仓库学习项目；**不接公司系统、不依赖公司手册。**  
> **节奏：** 每天约 2～3 小时。  
> **已完成：** Day1～Day9（见下方勾选）。从 Day10 起按日继续出详细讲义。

---

## 月度目标（30 天后你应具备）

1. 能讲清并操作：Chat 闭环（提示词 / few-shot / 会话 / 成本 / 安全边界）  
2. 能讲清并操作：RAG 最小闭环（切分 → 检索 → 带引用生成），理解关键词 vs 向量  
3. 能讲清：Tool Calling / 工作流与 Agent 的差别；只读工具与高风险禁区  
4. 能画出学习版「ERP AI 助手」架构，并知道上公司项目前还缺什么（权限、评测、观测等）  
5. **不要求**微调、训练、落地公司生产  

---

## 四周结构一览

| 周 | 天数 | 主题 |
|---|---|---|
| 第 1 周 | Day1～7 | LLM 应用入门 + Prompt 基础 |
| 第 2 周 | Day8～14 | RAG 从概念到可用 |
| 第 3 周 | Day15～21 | 工具调用、工作流、辅助生成 |
| 第 4 周 | Day22～30 | 工程化、安全、评测、复盘与进阶地图 |

---

## 第 1 周｜LLM 应用入门 + Prompt

### Day1 ✅ 概念 + 接通 API
- **教材重点：** Chat Completions、role、token、费用、上下文、OpenAI 兼容协议  
- **仓库对照：** `application.yml`、`OpenAiCompatibleLlmClient`  
- **产出理解：** 能独立配置 DeepSeek（或兼容网关）并成功调用  

### Day2 ✅ 消息组装链路
- **教材重点：** system/user/assistant 组装；会话；JSON 校验与重试  
- **仓库对照：** `ChatService`、`SessionStore`、`ReplyParser`  
- **产出理解：** 能画出一次 `/api/ai/chat` 全链路  

### Day3 ✅ Temperature / Token / 超时重试
- **教材重点：** 温度与稳定性；短问/长问/多轮的 token；401 不盲重试  
- **产出理解：** ERP 助手默认低温度（约 0.2～0.3）  

### Day4 ✅ 术语与系统提示词
- **教材重点：** 提示词分层（角色/术语/约束/输出格式）  
- **仓库对照：** `erp-system-prompt.txt`、`erp-glossary.md`  

### Day5 ✅ 多轮与历史裁剪
- **教材重点：** 上下文重发；`max-messages` 队头裁剪与成本  
- **仓库对照：** `SessionStore`  

### Day6 ✅ Few-shot
- **教材重点：** 少样本如何约束风格与边界  
- **仓库对照：** `erp-few-shot.txt`、`SystemPromptLoader`  

### Day7 ✅ Prompt 打磨（规则解释 / 字段校验）
- **教材重点：** 弱项用示范修补；字段校验解释结构  
- **产出理解：** Prompt 管「怎么答」，不负责私有长文档知识  

---

## 第 2 周｜RAG

### Day8 ✅ RAG 入门（概念）
- **讲义：** `docs/lessons/day08-prompt-wrap-and-rag-intro.md`  
- **重点：** Prompt vs RAG；解析/切分/向量/检索/生成；纯学习边界  

### Day9 ✅ RAG 最小闭环（关键词）
- **讲义：** `docs/lessons/day09-rag-minimum-loop.md`  
- **代码：** `com.erp.ai.rag`，`POST /api/ai/rag/ask`  
- **重点：** 切分、TopK、sources 由检索给出  

### Day10 ✅ 切分策略深入（讲义日）
- **讲义：** `docs/lessons/day10-chunking-strategies.md`  
- **重点：** heading / fixed / overlap；过粗过细；元数据与引用  
- **约定：** 不由助教直接改代码；自行对照调整  

### Day11 ✅ Embedding 与向量检索原理（讲义日）
- **讲义：** `docs/lessons/day11-embedding-vector-retrieval.md`  

### Day12 ✅ 向量检索学习版实现（讲义 · 自行编码）
- **讲义：** `docs/lessons/day12-vector-retriever-impl.md`  

### Day13～30 ✅ 正文已合并
- **合并讲义：** `docs/lessons/day13-30-combined.md`  
- 大纲条目仍见本文上方；**以合并讲义为阅读正文**  

---

## 每日标准学习流程（以后都按这个）

1. **读当天讲义**（主任务，`docs/lessons/dayXX-....md`）  
2. **按讲义中的顺序打开对应代码**（对照，不要求先写）  
3. **可选：** 启动项目，按讲义里的示例请求看一眼现象  
4. 有疑问直接问；需要时我再补「加餐短讲义」  

详细讲义规则：  
- **Day1～9：** 已有笔记/讲义  
- **Day10～12：** 单独讲义（切分 / Embedding / 向量实现提纲）  
- **Day13～30：** **合并正文** → [`day13-30-combined.md`](./day13-30-combined.md)  
- 默认助教不直接改学员业务代码；需要改码时在讲义中给对照提纲  

---

## 进度勾选（总览）

| Day | 状态 | 主题 |
|---|---|---|
| 1 | ✅ | API 与概念 |
| 2 | ✅ | 消息链路 |
| 3 | ✅ | 温度/Token/重试 |
| 4 | ✅ | 术语与系统提示 |
| 5 | ✅ | 多轮与裁剪 |
| 6 | ✅ | Few-shot |
| 7 | ✅ | Prompt 打磨 |
| 8 | ✅ | RAG 概念 |
| 9 | ✅ | RAG 关键词闭环 |
| 10 | ✅ | 切分策略深入 |
| 11 | ✅ | Embedding 原理 |
| 12 | ✅ | 向量检索实现 |
| 13～30 | 📖 | 见 [day13-30-combined.md](./day13-30-combined.md) |

---

## 修订

| 日期 | 说明 |
|---|---|
| 2026-08-15 | 首版 30 天逐日大纲；教材式；通用 ERP；纯学习 |
