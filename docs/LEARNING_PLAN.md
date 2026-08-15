# ERP Java 工程师 → AI 应用工程师学习计划

> 适用对象：5 年 Java，主做 ERP，不会 Python，每天 2–3 小时，天津，暂不换工作，以学习提升为主。  
> 主策略：纯 Java 落地练习，紧贴**常规 ERP** 场景；先问答与辅助，不做高风险自动过账。  
> 每日订正笔记与打卡：[STUDY_NOTES.md](./STUDY_NOTES.md)  
> **交付偏好：教材式讲义（`docs/lessons/`）为主。**  
> **边界：不依赖公司手册；示例文档用通用 ERP 教材；本次不应用到公司项目。**  
> **第 1 月合订详版（非概述）：** [lessons/MONTH1_DAY1-30_COMBINED.md](./lessons/MONTH1_DAY1-30_COMBINED.md) · [MONTH1.md](./MONTH1.md)  
> **第 2 月整月讲义+代码：** [lessons/MONTH2_DAY1-30_COMBINED.md](./lessons/MONTH2_DAY1-30_COMBINED.md) · [MONTH2.md](./MONTH2.md)  
> **第 3 月整月讲义+代码：** [lessons/MONTH3_DAY1-30_COMBINED.md](./lessons/MONTH3_DAY1-30_COMBINED.md) · [MONTH3.md](./MONTH3.md)  
> **第 4 月整月讲义+代码：** [lessons/MONTH4_DAY1-30_COMBINED.md](./lessons/MONTH4_DAY1-30_COMBINED.md) · [MONTH4.md](./MONTH4.md)  
> **第 5 月整月讲义+代码：** [lessons/MONTH5_DAY1-30_COMBINED.md](./lessons/MONTH5_DAY1-30_COMBINED.md) · [MONTH5.md](./MONTH5.md)  
> **第 6 月整月讲义+代码：** [lessons/MONTH6_DAY1-30_COMBINED.md](./lessons/MONTH6_DAY1-30_COMBINED.md) · [MONTH6.md](./MONTH6.md)  
> **第 7 月整月讲义+代码：** [lessons/MONTH7_DAY1-30_COMBINED.md](./lessons/MONTH7_DAY1-30_COMBINED.md) · [MONTH7.md](./MONTH7.md)  
> **第 8 月完整教材（非 30 天）：** [lessons/MONTH8_WORKFLOW_VIZ_COMPLETE.md](./lessons/MONTH8_WORKFLOW_VIZ_COMPLETE.md) · [MONTH8.md](./MONTH8.md)  
> **Web 前端轨道（完整教材，非 30 天）：** [lessons/WEB_VUE_COMPLETE.md](./lessons/WEB_VUE_COMPLETE.md) · [WEB.md](./WEB.md)  
> **技术节点路线图（何时学 Spring AI / Python / 某框架）：** [TECH.md](./TECH.md) → [TECH_ROADMAP.md](./TECH_ROADMAP.md)

---

## 一、总体策略

| 维度 | 选择 |
|---|---|
| 语言 | 先只学 Java（Spring Boot + OpenAI 兼容 API） |
| Python | 前 4 个月暂缓 |
| 场景 | 单据问答、制度检索、录入辅助、异常解释、审批摘要 |
| 目标 | 本职可落地的能力与作品，不是转岗面试 |
| 节奏 | 每天 2–3h，约每周 12–18h，共 20 周 |

### ERP 里最值钱的四件事

1. **查得准**（制度 / 物料 / 客户 / 历史单据）
2. **填得快**（字段建议、校验解释）
3. **说得清**（差异原因、审批摘要）
4. **不出事**（权限、审计、幻觉可控）

### 场景优先级

1. 操作手册 / 制度问答（RAG）← 第一优先
2. 报错与业务规则解释
3. 审批摘要 / 差异说明
4. 录单草稿建议（人工确认）
5. 只读查询工具调用
6. 以后才考虑：受控写入、工作流自动化

---

## 二、技术选型

> 详细「什么节点学什么」见 **[TECH_ROADMAP.md](./TECH_ROADMAP.md)**（T0～T16）。

**主栈**

- Java 17+ / 21，Spring Boot 3
- 自封装 OpenAI 兼容 HttpClient（本仓库已实现）
- PostgreSQL + pgvector（第 6 周起）
- Redis（会话 / 缓存，可选）
- 可选 ES：物料模糊检索以后再加

**先不要碰**

- Python 训练 / 微调
- 多 Agent 框架大战
- 本地大模型私有化（后期再说）
- 自动过账 / 自动改库存等高风险闭环
- **Spring AI / Python 主学**（须过闸门 A，见 TECH_ROADMAP T13～T15）

---

## 三、每周时间表（2–3 小时/天）

- Day1：学概念 60–90 分钟
- Day2：写代码
- Day3：写代码
- Day4：用真实 ERP 问题测
- Day5：修坏 case + 记笔记
- 周末二选一：集中 2–3h 联调 / 补文档

原则：用真实手册和真实问法；每周只追求一个可运行增量。

---

## 四、20 周详细路线

### 第 1–2 周：建立最小 LLM 闭环（纯 Java）✅ 本仓库起点

**目标：** 会稳定调模型。

**做什么：**

- Spring Boot 模块 `erp-ai-assistant`
- 接 OpenAI 兼容接口（也支持 mock 模式本地跑通）
- `/api/ai/chat`：多轮会话
- 系统提示 + 强制 JSON 输出 + 校验失败重试
- `traceId`、耗时、token、成本日志

**ERP 切入题：**  
一句话需求 → 建议单据类型 + 必填字段清单（JSON）

**验收：**

- [ ] 本地可调用，输出稳定可解析
- [ ] 有基础超时重试
- [ ] 有 trace / token / 成本日志

**本周任务（Day 1–7）**

1. 启动本项目，先用 `ai.provider=mock` 跑通
2. 配置真实 API Key，切换 `ai.provider=openai-compatible`
3. 试用 `/api/ai/chat`，观察 JSON 与 `need_human`
4. 收集 20 个同事常问问题（手册类）
5. 准备 3 份可脱敏操作文档，为第 6 周 RAG 做材料

---

### 第 3–5 周：Prompt 工程 + ERP 领域提示词

**目标：** 把通用聊天变成懂 ERP 话术的助手。

**学什么：**

- 系统提示分层：角色、术语表、输出约束、拒答规则
- 少样本（用真实话术）
- 字段级校验解释

**项目：ERP 术语与规则解释器**

- 输入：用户问题 / 报错信息
- 输出：通俗解释 + 可能原因 + 建议操作（不直接改库）
- 术语表：物料、BOM、仓位、凭证、期间、核销等

**验收：**

- [ ] 30 个常见业务问题基本可用
- [ ] 明确“不知道就说不知道”

---

### 第 6–10 周：ERP 知识库 RAG（仍用 Java）

**目标：** 本职最有复利的能力。

**学什么：**

- 文档解析、切分、Embedding、向量检索、引用回答
- 向量库：PostgreSQL + pgvector
- 权限过滤：按组织 / 账套 / 角色

**项目：制度 & 操作手册问答**

1. 文档上传 / 导入
2. 切分入库
3. 提问 → 检索 → 带引用回答
4. 管理端：重建索引、启停文档
5. 评测：50 题

**分周建议：**

- 第 6 周：解析 + 切分 + 入库
- 第 7 周：检索 + 生成 + 引用
- 第 8 周：权限与租户隔离
- 第 9 周：评测与坏 case 修复
- 第 10 周：打磨成内部可用小工具

**验收：**

- [ ] 回答能指出文档来源段落
- [ ] 无权限内容检不出
- [ ] 50 题有正确率统计

---

### 第 11–14 周：单据辅助

**目标：** 提高录入与审核效率，先不做高风险自动执行。

**项目（三选一）：**

1. 采购 / 销售录单助手：自然语言 → 草稿字段
2. 审批摘要助手：长审批流总结风险点与差异
3. 异常解释助手：库存不足 / 凭证不平衡 / 期间关闭 → 排查步骤

**工程要点：**

- 只生成草稿建议，写入必须人确认
- 建议落审计日志
- 低置信度高亮
- 现有 Java 校验优先于模型

**验收：**

- [ ] 自己先测 20 单，有提效体感
- [ ] 任何建议可追溯

---

### 第 15–17 周：轻量工具调用（有约束的伪 Agent）

**目标：** 学会 Tool Calling，ERP 里用白名单 + 只读优先。

**只读工具示例：**

- `queryItem`
- `queryInventory`
- `queryOrderStatus`
- `queryCustomerCredit`

**禁止：** 直接调支付、过账、删单、改库存等写接口。

**验收：**

- [ ] 工具调用可追踪
- [ ] 无工具时不编造库存 / 金额
- [ ] 超时 / 失败有降级话术

---

### 第 18–20 周：评测、成本、安全、沉淀

**做什么：**

- Prompt 版本管理
- 成本统计（按模块 / 用户）
- 安全：提示注入、越权提问、敏感字段脱敏
- 写 2 篇复盘：
  1. RAG 在 ERP 手册场景的坑
  2. 为什么单据助手必须“建议 + 人工确认”

**验收：**

- [ ] 能独立演示完整链路
- [ ] 有一个可在公司内网试用的小模块
- [ ] 形成提示词 / 评测题 / 坏 case 模式库

---

## 五、20 周后自用 KPI

- 能独立做手册问答
- 能做录单 / 审批辅助草稿（带审计与人工确认）
- 能设计只读工具调用，并清楚为何不能让模型直接过账
- 能用 50 题评测集判断改动是变好还是变差
- 能评估成本与延迟

---

## 六、学习资源（少而精）

1. OpenAI 兼容 API 文档（Chat / Embeddings / Tools）
2. RAG 工程实践（切分、检索、引用、权限）
3. Spring Boot 官方示例
4. 你们自己的 ERP 手册与历史工单（最好的教材）

不建议现在报一堆算法课。

---

## 七、常见坑

1. 只追模型，不追场景
2. 过度 Agent 化（能用工作流就别上全自动 Agent）
3. 忽视评测
4. 一把梭微调
5. 丢掉 Java / ERP 优势
6. 安全后置

---

## 八、与本仓库的对应关系

| 阶段 | 本仓库目录 / 模块 |
|---|---|
| 第 1–2 周 | `erp-ai-assistant` 已实现 Chat 闭环 |
| 第 3–5 周 | 扩展 `prompt/` 与术语表 |
| 第 6–10 周 | 新增 `rag/`（解析、切分、向量、引用） |
| 第 11–14 周 | 新增 `document-assist/`（录单 / 审批辅助） |
| 第 15–17 周 | 新增 `tool/`（只读工具调用） |
| 第 18–20 周 | 新增 `eval/`、`billing/`、`security/` |

进度请在本文对应验收 checkbox 上打勾。
