# 技术主导学习路线图（节点制）

> **这是什么：** 以**技术 / 框架**为主线的学习节点图——回答「什么时候学 Spring AI？什么时候开 Python？什么时候碰某项技术？」  
> **这不是什么：** 不是再按 Day1～30 拆课；也不是替代第1～8月业务教材。业务能力仍读 `MONTH*.md` / `WEB.md`。  
> **适用：** 5 年 Java ERP；每天 2～3 小时；先作品与本职能力，不急转岗。  
> **边界：** 纯学习仓；不接公司生产；不自动过账。

---

## 0. 怎么用这份图

1. **先看「总闸门」**：未满足前置，不要提前啃后面的框架。  
2. **每个节点看四列：** 学什么 → 为何在此时 → 前置 → 对应仓库教材 / 产出。  
3. **主路径仍是 Java：** 你已有 Spring 肌肉；AI 应用工程优先把「可控系统」做完，再开 Python 生态。  
4. **并行原则：** 同一时期最多深挖 **1 个新技术栈**；其余只做「能读懂文档」级别。

```text
能力主线（已有教材）          技术主线（本文）
MONTH1～8 + WEB     ←对齐→    节点 T0～T12 + 选修
```

---

## 1. 一张总图（技术节点时序）

```text
T0  地基加固 ──► HTTP Client / JSON / 配置与密钥 / 日志
T1  LLM 协议 ──► OpenAI 兼容 Chat Completions（自封装，已在做）
T2  Prompt 工程 ──► 提示词文件化 / few-shot / JSON Schema 约束
T3  RAG 地基 ──► 切分 / 关键词 / Embedding API / 余弦检索
T4  检索加深 ──► Hybrid / Rerank / Gate /（可选）pgvector
T5  编排与 HITL ──► 状态机 / 审计 /（概念）工作流引擎
T6  评测与观测 ──► JSONL Eval / baseline / Micrometer·结构化日志
T7  前端演示 ──► Vue3 + Vite（WEB 教材）
T8  权限与隔离 ──► 模拟 ACL / 多租户头 /（概念）Spring Security
T9  受控写入 ──► WriteGateway / 幂等 / 假账本
T10 适配器化 ──► Port/Adapter / 契约测试 /（可选）WireMock
T11 规则引擎 ──► 自研 RuleEngine →（选修）Drools 对照
T12 可视化    ──► SVG Flow 图 / 审计回放（MONTH8）

── 闸门 A：T0～T6 + 作品可演示 之后 ──
T13 Spring AI ──► 用官方抽象替换/对照「自封装 Client」（见第 3 节）
T14 Python 入门 ──► 语法 + httpx + 读脚本，不重写整仓
T15 Python AI 生态 ──► LangChain / LlamaIndex / 评测脚本（选修深度）
T16 进阶选修 ──► Redis / ES / Docker Compose / OpenTelemetry / 本地模型…
```

**硬规则：**  
- **Spring AI** 不在 T1 一上来学——先自封装吃透协议。  
- **Python** 不在前 4 个业务月主学——闸门 A 后再开。  
- **训练 / 微调 / 多 Agent 框架大战** 全程靠后，默认不进主路径。

---

## 2. 节点详表（技术主导）

### T0｜地基加固（可与 MONTH1 并行回顾）

| 项 | 内容 |
|---|---|
| **学什么** | Java 21、Spring Boot 3、`RestClient`/`WebClient`、Jackson、`application.yml`、环境变量、SLF4J |
| **为何此时** | 后面所有 LLM/RAG 都建立在「会发 HTTP、会配 Key、会打日志」 |
| **前置** | 你会写业务 Spring 即可 |
| **产出** | 本地 `mock` 与真实 Key 两套配置都能启动 |
| **教材** | MONTH1 配置与 Client 章节 |

### T1｜LLM 协议层（自封装优先）

| 项 | 内容 |
|---|---|
| **学什么** | Chat Completions：`messages` / `role` / token / temperature / `response_format`；OpenAI 兼容网关（DeepSeek 等） |
| **为何此时** | 协议比任何 SDK 都稳；换厂商 ≈ 换 baseUrl/model/key |
| **前置** | T0 |
| **产出** | `LlmClient`（mock + openai-compatible）；`/api/ai/chat` |
| **明确不做** | 此时不上 Spring AI、不上 LangChain |
| **教材** | MONTH1 |

### T2｜Prompt 与结构化输出

| 项 | 内容 |
|---|---|
| **学什么** | system/few-shot 文件化、JSON 校验重试、拒答与 need_human |
| **技术点** | 资源文件加载、版本号（promptVersion）、解析容错 |
| **前置** | T1 |
| **产出** | 术语表 + few-shot + 稳定 JSON |
| **教材** | MONTH1 |

### T3｜RAG 最小闭环

| 项 | 内容 |
|---|---|
| **学什么** | Chunking、KeywordRetriever、Embedding HTTP、内存向量检索、sources 纪律 |
| **技术点** | 文本处理、相似度、classpath 文档加载 |
| **前置** | T1～T2 |
| **产出** | `/api/ai/rag/ask` + sources |
| **可选同期** | 只读了解「向量库」名词；实现仍可用内存 List |
| **教材** | MONTH1 |

### T4｜检索加深

| 项 | 内容 |
|---|---|
| **学什么** | Hybrid/RRF、Gate、简易 Rerank、`ChunkVectorStore` 抽象 |
| **技术 / 框架** | **PostgreSQL + pgvector**（有环境再上；无则 memory） |
| **前置** | T3 |
| **产出** | retriever 可切换；质量日志字段 |
| **教材** | MONTH2、MONTH3（Store/reindex） |

### T5｜工作流与 HITL

| 项 | 内容 |
|---|---|
| **学什么** | 状态机、WAIT_HUMAN、审计流水、合法迁移表 |
| **技术对照** | 概念可读 Camunda/Flowable；**学习仓自研 FlowEngine**，不引入重型 BPM |
| **前置** | T3；建议 T4 同步 |
| **产出** | `/api/ai/flow/*` |
| **教材** | MONTH2、MONTH3、MONTH8（可视化） |

### T6｜评测与观测

| 项 | 内容 |
|---|---|
| **学什么** | JSONL 题集、EvalRunner、baseline；traceId、分段耗时、成本估算 |
| **技术 / 框架** | JUnit；可选 **Micrometer** / Actuator metrics；日志 JSON 一行 |
| **前置** | T3 |
| **产出** | `evals/` + `/stats` 或等价 |
| **教材** | MONTH2、MONTH3 |

### T7｜前端演示控制台

| 项 | 内容 |
|---|---|
| **学什么** | **Vue 3 + Vite + Router + Pinia**；proxy 联调；sources/Flow 面板 |
| **为何此时** | 后端可演示后，前端放大作品集信号 |
| **前置** | 至少 T1 + T3 可用；理想 T5 |
| **产出** | `erp-ai-console/` |
| **教材** | [WEB.md](./WEB.md) · `WEB_VUE_COMPLETE.md` |
| **明确不做** | 不上公司级中后台框架全家桶（除非你已熟） |

### T8｜权限与隔离（学习级）

| 项 | 内容 |
|---|---|
| **学什么** | 角色→可检索文档；tenantId 隔离；学习请求头 |
| **技术对照** | **Spring Security** 概念（认证/鉴权）；学习仓用头模拟即可 |
| **前置** | T3、T4 |
| **产出** | ACL/Tenant 过滤 + 越权评测题 |
| **教材** | MONTH4 |

### T9｜受控写入

| 项 | 内容 |
|---|---|
| **学什么** | WriteGateway、幂等、假账本、APPROVE 后才写 |
| **技术点** | 领域服务、幂等键存储、审计事件 |
| **前置** | T5、T8 建议完成 |
| **产出** | write-safety 套件 |
| **教材** | MONTH5 |
| **明确不做** | 模型直连写库存 Tool；接公司库 |

### T10｜适配器稳定化

| 项 | 内容 |
|---|---|
| **学什么** | Hexagonal：Port / Adapter；Fake vs Sandbox；契约测试 |
| **技术 / 框架** | 可选 **WireMock** / JDK HttpServer stub；JUnit 契约套件 |
| **前置** | T9 |
| **产出** | 换 Adapter 不改编排 |
| **教材** | MONTH6 |

### T11｜规则引擎

| 项 | 内容 |
|---|---|
| **学什么** | 规则 vs LLM；RuleEngine；决策表；rulesVersion |
| **技术 / 框架** | 主路径：**自研轻量规则**；选修对照 **Drools**（读懂概念即可，不必上生产配置） |
| **前置** | T5、T9 |
| **产出** | classify/risk/write-precheck 外置 |
| **教材** | MONTH7 |

### T12｜工作流可视化

| 项 | 内容 |
|---|---|
| **学什么** | FlowGraph API、手写 SVG、审计回放 |
| **技术点** | 前端图形；合法边不可发明 |
| **前置** | T5、T7 |
| **产出** | 可演示状态图 |
| **教材** | [MONTH8.md](./MONTH8.md) |

---

## 3. 闸门 A 之后：Spring AI（T13）

### 何时开始学 Spring AI

**同时满足：**

1. 你能不靠 SDK 讲清 Chat Completions 与 Embedding 请求；  
2. 仓库里已有可切换的 `LlmClient` / `EmbeddingClient`；  
3. RAG + HITL + Eval 至少能演示一轮；  
4. 你想减少「样板 HTTP 代码」，而不是逃避协议理解。

**建议窗口：** 完成 **T6～T9**（约第3～5月能力）之后；**不要**在 MONTH1 替换自封装。

### Spring AI 学什么（节点内清单）

| 子项 | 深度 |
|---|---|
| `ChatClient` / Prompt 抽象 | 对照你的 `LlmClient` |
| Advisors / 拦截与重试 | 对照你的重试与日志 |
| 向量存储抽象、pgvector starter | 对照 `ChunkVectorStore` |
| Tool / Function calling 支持 | 对照只读 Tool + 白名单 |
| 与 Spring Boot 配置绑定 | 对照 `AiProperties` |

### 学法（推荐）

```text
并行对照，而不是推倒重来：
  保留自封装路径（mock / 排障最稳）
  新增 spring-ai profile 走官方抽象
  同一套 eval 两边跑，比稳定性与可观测字段
```

### 明确不做

- 用 Spring AI「一键 Agent」跳过 HITL/规则/权限  
- 没做完 Gate/sources 纪律就上复杂 RAG Advisor 链

---

## 4. 闸门 A 之后：Python（T14 → T15）

### 何时开始学 Python

**同时满足：**

1. Java 主路径已能讲清「生成 / 检索 / 人工 / 评测」；  
2. 你需要：读开源示例、写小脚本、或对接生态工具；  
3. 每天仍能保证 Java 作品维护时间（Python 初期每周 ≤3～4h）。

**建议窗口：** 原计划「前 4 个月暂缓」——对应你仓内大约 **MONTH1～4 完成后**；更稳是 **T6+T7 完成** 再开。

### T14｜Python 入门（先工具，后框架）

| 学什么 | 用途 |
|---|---|
| 语法、venv、pip | 能跑脚本 |
| `httpx` / `requests` | 调同一 OpenAI 兼容 API |
| JSON / pathlib | 处理 eval jsonl、导出报告 |
| 类型标注基础 | 读别人代码 |

**产出：** 一个 `scripts/py_eval_smoke.py`：读 jsonl → 调你的 `/api/ai/rag/ask` → 打印 passed。  
**不做：** 用 Python 重写整个 `erp-ai-assistant`。

### T15｜Python AI 生态（选修，按需点名）

| 技术 | 何时碰 | 学到什么程度 |
|---|---|---|
| **LangChain** | T14 后；想读教程/示例时 | 会跑通一个 RAG notebook；能映射到你 Java 的 Chunk/Retriever/Chain |
| **LlamaIndex** | 偏文档索引实验时 | 了解 Index/Query 概念即可 |
| **Haystack** | 企业检索向资料时 | 浏览级 |
| **FastAPI** | 要给 Java 旁路一个实验服务时 | 做一个最小 embedding/rerank 旁路 |
| **Jupyter** | 调提示词/看 embedding 时 | 会开 notebook |
| **pandas** | 分析 eval 失败表时 | 读写 csv/jsonl 级 |

**原则：** Python 生态用来 **加速实验与阅读**；**主交付仍是 Java 学习仓**（符合你本职栈）。

---

## 5. 其它技术：何时学（速查）

| 技术 / 框架 | 建议节点 | 前置 | 备注 |
|---|---|---|---|
| **Maven / IDE** | T0 | — | 已有则跳过 |
| **Docker / Compose** | T4 或 T10 | T3 | 一键起 Postgres+pgvector；非必须 |
| **Testcontainers** | T10 | T4+JUnit | 契约/适配器集成测 |
| **WireMock** | T10 | T6 | HTTP stub |
| **Redis** | T6 后选修 | T1 | 会话/幂等键外置；学习仓可继续内存 |
| **Elasticsearch** | T4 后选修 | T3 | 关键词/混合检索进阶；ERP 物料模糊搜场景 |
| **Micrometer + Prometheus** | T6 | T1 | 比手写计数更标准 |
| **OpenTelemetry** | T6 后 | T6 | 分布式 trace；先把 traceId 贯好 |
| **Spring Security** | T8 | T1 | 先概念；学习仓可用请求头模拟 |
| **OAuth2 / OIDC** | 远景 | T8 | 不接公司 SSO；只读文档 |
| **Camunda / Flowable** | T5 对照 | T5 | **只对照概念**；仓内自研状态机 |
| **Drools** | T11 对照 | T11 | 可选；主路径自研规则 |
| **Kafka / MQ** | 远景 | T9 | 写入异步化；非主路径 |
| **Kubernetes** | 远景 | Docker | 本职 AI 应用非优先 |
| **本地模型（Ollama 等）** | T1 后选修 | T1 | 离线实验；注意与兼容 API 差异 |
| **重排序模型 / Cross-Encoder** | T4 后 | T4 | 先规则 rerank，再模型 rerank |
| **OCR（Tesseract/云 API）** | MONTH3 可选后 | T2 | FakeOcr 优先 |
| **React** | 不优先 | — | 本仓前端定 Vue；勿双栈 |
| **多 Agent 框架** | 默认延后 | T5+T11 | 有 HITL/规则后再谈 |
| **微调 / 训练** | 默认不做 | — | 应用工程主路径不依赖 |

---

## 6. 「现在该学什么」决策树

```text
后端 Chat 还不稳定？ ──是──► 停在 T1～T2（别碰 Spring AI / Python）
        │否
RAG 无 sources / 无 Gate？ ──是──► T3～T4
        │否
不能演示人工确认？ ──是──► T5（+ MONTH8 可视化可稍后）
        │否
改 Prompt 不敢回归？ ──是──► T6
        │否
作品集缺界面？ ──是──► T7（Vue）
        │否
还要权限/写入/适配器/规则？ ──是──► T8～T11 按月教材
        │否
想减样板代码、对齐官方？ ──是──► T13 Spring AI（对照，不推倒）
        │否
要读 Python 示例 / 写评测脚本？ ──是──► T14 → 按需 T15
        │否
维护作品 + 选修表里挑 1 个深挖
```

---

## 7. 与现有教材对照表

| 技术节点 | 主要教材 |
|---|---|
| T0～T3 | [MONTH1.md](./MONTH1.md) |
| T4～T6 入门 | [MONTH2.md](./MONTH2.md) |
| T4 运维 / T6 加深 | [MONTH3.md](./MONTH3.md) |
| T8 | [MONTH4.md](./MONTH4.md) |
| T9 | [MONTH5.md](./MONTH5.md) |
| T10 | [MONTH6.md](./MONTH6.md) |
| T11 | [MONTH7.md](./MONTH7.md) |
| T12 | [MONTH8.md](./MONTH8.md) |
| T7 | [WEB.md](./WEB.md) |
| T13～T16 | 本文 + 官方文档；尚未单独开「月教材」时可按本节清单自学 |

---

## 8. 学习纪律（技术向）

1. **协议先于 SDK：** 先自封装，后 Spring AI。  
2. **Java 主交付，Python 辅实验：** 除非你改职业目标。  
3. **同时只深挖一个新技术。**  
4. **每个新技术必须挂回 ERP 场景**（问答/检索/HITL/写入），禁止空转 demo。  
5. **评测不过，不升级框架。**  
6. **公司生产技术（SSO、真 BPM、真库）只读不接。**

---

## 9. 修订

| 日期 | 说明 |
|---|---|
| 2026-08-15 | 首版：技术节点 T0～T16；明确 Spring AI / Python 闸门与速查表 |
