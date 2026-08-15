# B 站视频对照表（配合 MONTH1～8）

> **怎么用：** 各月合订本 / 第8月每章开头有「建议视频」一行；本页是**总目录与纪律**。  
> **主线仍是仓库教材**（`MONTH*` / `WEB` / `TECH_ROADMAP`）；视频只作概念对照。  
> **链接可能失效：** 用「备用搜」关键词在 B 站搜；优先看官方号（黑马 / 尚硅谷 / 吴恩达搬运完整课）。

---

## 0. 观看纪律（必读）

1. **先讲义、后视频**；视频与讲义冲突时，以讲义 + 你仓内代码为准。  
2. **MONTH1～4 不要跟视频改成 Spring AI / LangChain 主栈**——协议与 RAG 先自封装（见 [TECH_ROADMAP](./TECH_ROADMAP.md) T13 闸门）。  
3. 标题带 **Agent / MCP / 微调 / 多智能体** 的：收藏即可，等 T11～T13 后再啃。  
4. 每天视频预算：**≤30～45 分钟**（挑 P 数 / 章节，别刷全集）。  
5. Python 课（吴恩达 RAG 等）：只学**概念与评测思路**，不重写 Java 仓。

---

## 1. 精选链接（可直接点）

| ID | 主题 | 链接 | 建议何时看 |
|---|---|---|---|
| V-LLM | 黑马 SpringAI+DeepSeek（先看「调用大模型 / Prompt」段） | https://www.bilibili.com/video/BV1MtZnYtEB3/ | MONTH1 Chat；整课 Spring AI 留到 T13 |
| V-SAI | Spring AI 1.0 全套（DeepSeek / Tools / Agent） | https://www.bilibili.com/video/BV1fm4yzVEpa/ | **T13 后**对照；早期只抽 DeepSeek 请求原理 |
| V-SAI2 | Spring AI + LangChain4j + RAG + Agent | https://www.bilibili.com/video/BV1h8G96vEck/ | T13 / 对照 LangChain4j |
| V-SAI3 | Spring AI 2026 入门到实战 | https://www.bilibili.com/video/BV1QCkYBnEtc/ | T13 |
| V-DS | DeepSeek + Spring AI 短系列（含 RAG 一集） | https://www.bilibili.com/video/BV18EdPYkE2U/ | MONTH1 可选扫；RAG 集对照 MONTH1 末 |
| V-RAG-ND | 吴恩达 RAG（概念最稳；Python 勿照抄） | https://www.bilibili.com/video/BV1rGCvBVEtR/ | MONTH1 D8～14、MONTH2 Hybrid |
| V-RAG-ND2 | 吴恩达 RAG（另一搬运） | https://www.bilibili.com/video/BV1FsfsBJEtj/ | 同上 |
| V-EMB | Embedding 讲解（小白向） | https://www.bilibili.com/video/BV1XzADeFEMs/ | MONTH1 D11～12 |
| V-RAG-CN | RAG 完整项目向（概念+知识库） | https://www.bilibili.com/video/BV1QLj9zfEZ5/ | MONTH1～2；跳过与讲义冲突的框架段 |
| V-RAG-CN2 | RAG 工作原理全套 | https://www.bilibili.com/video/BV1RbR6YmE1G/ | MONTH2 Hybrid/Rerank 对照 |
| V-RAG-JAVA | 企业级 RAG 痛点（Spring AI；后期） | https://www.bilibili.com/video/BV1GYkKBVEcW/ | MONTH3～4 运维/隔离对照；勿整仓迁移 |
| V-LC4J | 小智医疗：SpringBoot+LangChain4j+RAG | https://www.bilibili.com/video/BV1MyLUzrEFz/ | 对照 Prompt/RAG；主实现仍自封装 |
| V-VUE | Vue3+Vite+Pinia 4 小时入门 | https://www.bilibili.com/video/BV1aa1NYxECK/ | WEB / MONTH4 控制台 / MONTH8 |
| V-VUE2 | Vue3+Vite 极简教程 | https://www.bilibili.com/video/BV1585762EQ9/ | 同上 |
| V-BPM | Camunda 工作流（**只作概念对照**） | https://www.bilibili.com/video/BV1qe4y1m7D7/ | MONTH2～3 Flow；仓内自研状态机 |
| V-DROOLS | Drools 规则引擎（**只作概念对照**） | https://www.bilibili.com/video/BV1G44y1t7B1/ | MONTH7；仓内自研 RuleEngine |
| V-DS-INTRO | 黑马 DeepSeek 新手向直播合集 | https://www.bilibili.com/video/BV1iQNueoEBD/ | 入门扫盲；勿用 Dify 替代本仓路径 |

---

## 2. 按月速查

| 月 | 优先视频 ID | 说明 |
|---|---|---|
| MONTH1 | V-LLM、V-DS-INTRO、V-RAG-ND、V-EMB、V-LC4J（Prompt 段） | Chat/Prompt/RAG 概念 |
| MONTH2 | V-RAG-ND、V-RAG-CN2、V-BPM（概念） | Hybrid/Gate；Flow≠上 Camunda |
| MONTH3 | V-RAG-JAVA（痛点）、V-BPM | Store/reindex、审计回放 |
| MONTH4 | V-VUE / V-VUE2、V-RAG-JAVA（metadata 隔离） | 控制台 + ACL/租户概念 |
| MONTH5 | V-SAI（Tool 黑名单对照）、V-LLM（勿跟写库存） | 受控写；视频里危险 Tool 当反例 |
| MONTH6 | 搜 `六边形架构` / `Port Adapter` / `WireMock` | 链接触发少，以搜索为主 |
| MONTH7 | V-DROOLS（概念前几集） | 对照后仍用自研规则 |
| MONTH8 | V-VUE、V-BPM（高亮/节点概念） | SVG 自绘；勿上重型流程设计器 |
| T13 后 | V-SAI、V-SAI2、V-SAI3、黑马 BV1MtZnYtEB3 | 对照官方抽象，eval 两边跑 |

---

## 3. 常用备用搜索词

```text
DeepSeek API OpenAI 兼容
Prompt 工程 system few-shot
RAG Embedding 余弦相似度
Hybrid Search RRF Rerank
pgvector PostgreSQL
Vue3 Vite Pinia
六边形架构 Port Adapter
WireMock 契约测试
Camunda 入门（对照）
Drools 入门（对照）
Spring AI ChatClient（T13）
```

---

## 4. 修订

| 日期 | 说明 |
|---|---|
| 2026-08-15 | 首版：精选 BV + 月对照；逐日建议写在 MONTH 合订本各 Day 开头 |
