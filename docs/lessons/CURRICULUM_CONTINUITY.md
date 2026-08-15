# 课程连续性审查与桥接（第 1～7 月 + Web + 第 8 月入口）

> **用途：** 补上各月收官「预告」与后续教材之间的断档；统一 HITL / APPROVE 语义时间线；标明双控制台与 API 演进。  
> **形式：** 完整桥接章（**不卡 30 天**）。读完应能按真实开课路径前进，而不是停在「只选一条」的旧菜单上。  
> **入口索引：** [LEARNING_PLAN.md](../LEARNING_PLAN.md) · [PORTFOLIO.md](../PORTFOLIO.md) · **全方向对勾图：** [CURRICULUM_DIRECTIONS.md](../CURRICULUM_DIRECTIONS.md)

---

## 1. 已开课路径总图（以仓库现状为准）

| 阶段 | 入口 | 合订/完整教材 | 主题一句话 |
|---|---|---|---|
| 第1月 | [MONTH1.md](../MONTH1.md) | [MONTH1_DAY1-30_COMBINED.md](./MONTH1_DAY1-30_COMBINED.md) | Chat / Prompt / RAG 最小闭环 |
| 第2月 | [MONTH2.md](../MONTH2.md) | [MONTH2_DAY1-30_COMBINED.md](./MONTH2_DAY1-30_COMBINED.md) | Hybrid / Gate / HITL 最小流 / Eval 入门 |
| 第3月 | [MONTH3.md](../MONTH3.md) | [MONTH3_DAY1-30_COMBINED.md](./MONTH3_DAY1-30_COMBINED.md) | Store / reindex / audit / baseline |
| 第4月 | [MONTH4.md](../MONTH4.md) | [MONTH4_DAY1-30_COMBINED.md](./MONTH4_DAY1-30_COMBINED.md) | ACL / 反馈 / 多租户 / console.html |
| 第5月 | [MONTH5.md](../MONTH5.md) | [MONTH5_DAY1-30_COMBINED.md](./MONTH5_DAY1-30_COMBINED.md) | WriteGateway + 假账本；APPROVE 后才写 |
| 第6月 | [MONTH6.md](../MONTH6.md) | [MONTH6_DAY1-30_COMBINED.md](./MONTH6_DAY1-30_COMBINED.md) | Port / Adapter / Fake·Sandbox / 契约测 |
| 第7月 | [MONTH7.md](../MONTH7.md) | [MONTH7_DAY1-30_COMBINED.md](./MONTH7_DAY1-30_COMBINED.md) | RuleEngine；规则先于模型 |
| 第8月 | [MONTH8.md](../MONTH8.md) | [MONTH8_WORKFLOW_VIZ_COMPLETE.md](./MONTH8_WORKFLOW_VIZ_COMPLETE.md) | 工作流可视化（**非 30 天**完整教材） |
| Web 轨 | [WEB.md](../WEB.md) | [WEB_VUE_COMPLETE.md](./WEB_VUE_COMPLETE.md) | Vue 3 演示控制台（**非 30 天**；可与 M4+ 并行） |

```text
M1 Chat/RAG
 → M2 Hybrid/Gate/HITL/Eval
 → M3 Store/Audit/Baseline
 → M4 ACL/Tenant/Feedback/static console
 → M5 WriteGateway + Fake Ledger（语义转折点）
 → M6 Port/Adapter 稳定化
 → M7 RuleEngine 外置确定性规则
 → M8 Flow 可视化（章节式）
∥ Web Vue console（演示面；不新开魔法写接口）
```

**纪律不变：** 不接公司生产库 / SSO / 公司规则平台 / 公司 BPM；学习仓 Fake/Sandbox only。

---

## 2. HITL / APPROVE 语义时间线（最易丢连贯处）

| 阶段 | APPROVE 实际含义 | UI / 口述必须说清的话 |
|---|---|---|
| 第2～4月 | 仅推进学习 Flow 状态；**不写库存/不过账** | 「APPROVE ≠ 写库」 |
| 第5月起 | APPROVE 可触发 **WriteGateway → 内存假账本**；仍禁止模型直写；≠ 公司生产过账 | 「APPROVE ≠ 生产过账；写假账须经 Gateway + 权限 + 幂等 + 审计」 |
| 第6月 | 同上；Ledger/ERP 经 Port，可换 Fake/Sandbox Adapter | 「换 Adapter 不改主编排与 HITL」 |
| 第7月 | 写前 **RuleEngine DENY 压过 LLM**；规则不直写 Port | 「规则先于模型；冲突以规则为准」 |
| 第8月 / Web | 图上点 APPROVE 仍走后端 decide；前端不发明状态迁移 | 「可视化 ≠ 放开乱跳；写仍经规则/Gateway」 |

> **桥接句（可贴 PORTFOLIO）：**  
> 第2～4月用「APPROVE ≠ 写库」训练安全习惯；第5月起在**同一 HITL 闸门**上接上受控假账写入——不是推翻纪律，而是把「写」放进可审计路径。

---

## 3. 各月收官「旧菜单」→「已开课」对照

早期讲义曾写「下月只选一条（观测 / 规则 / 灰度 / …）」。仓库现已按固定主线开课，按下表前进即可；旧菜单项移到「第9月及以后可选」。

| 收官处 | 旧表述风险 | 已开课下月 | 可选延后（第9月+） |
|---|---|---|---|
| M2-D30 | 四选一主线 | **第3月** Store/reindex/audit/baseline（四周全覆盖） | OCR 兴趣向 |
| M3-D30 | 「观测深化作第5月」易误导 | **第4月** ACL/反馈/多租户/console | 观测指标/trace 平台化 |
| M4-D30 / FAQ | FAQ 仍像「从观测/规则里选」 | **第5月** 受控写入 | 观测 / 真 pg 运维 / 可视化 / 规则（后已独立成月） |
| M5-D30 / FAQ | FAQ 仍列「真适配器」为选项之一 | **第6月** Port/Adapter | 观测大盘 / 灰度 |
| M6-D30 / FAQ | FAQ 仍「规则等四选一」 | **第7月** 规则引擎 | 观测大盘 / 灰度 |
| M7-D30 / FAQ | FAQ 仍「可视化等四选一」 | **第8月** 工作流可视化（完整教材） | 观测大盘 / 灰度 / 提示词运营 |

---

## 4. 能力桥接清单（缺哪块就回哪月补，不重开月）

| 假设已会 | 首次讲透位置 | 若跳读会出现的症状 | 补法 |
|---|---|---|---|
| Hybrid + Gate | M2 | RAG 不稳、幻觉无拦 | 回 M2-D1～14 |
| Flow WAIT_HUMAN | M2～3 | 无审批节点 | 回 M2 HITL + M3 EDIT/审计 |
| reindex / Store | M3 | 换教材不生效 | 回 M3 reindex 周 |
| baseline 门禁 | M3 | 改坏不知 | 回 M3 eval/baseline |
| ACL + tenant 头 | M4 | 串租户 / 越权 sources | 回 M4-D1～21 |
| Feedback → 题集 | M4 | 踩点无闭环 | 回 M4 反馈周 |
| WriteGateway + 幂等 | M5 | APPROVE 后乱写或双写 | 回 M5 全月；跑 `write-safety` |
| Port 只依赖接口 | M6 | Gateway 绑死 InMemory | 回 M6；跑 `adapter-contract` |
| RuleEngine 写前 | M7 | Prompt 绕过关期间 | 回 M7；跑 `rules-cases` |
| Vue proxy + 身份头 | Web | CORS / 头丢失 | 回 [WEB_VUE_COMPLETE](./WEB_VUE_COMPLETE.md) 第4～7章 |

---

## 5. 双控制台：谁演示什么

| 面 | 路径 | 来自 | 适用 |
|---|---|---|---|
| 静态学习台 | `erp-ai-assistant/.../static/console.html` | 第4～5月讲义 | 零前端工程、快速彩排 ACL/反馈/写入链路 |
| Vue 演示台 | `erp-ai-console/` | [WEB.md](../WEB.md) | 路由/Pinia/可维护 UI；第8月 Flow 图挂这里 |

**桥接规则：**

1. 两者都只调现有 `/api/ai/**`，**禁止**前端发明「一键过账」魔法接口。  
2. 学习头同一套：`X-User-Id` / `X-Roles` / `X-Tenant-Id` /（写）`X-Idempotency-Key`。  
3. 第5月后 Vue Flow 文案用「≠生产过账」；静态台若仍写「≠写库」，须加脚注指向第5月语义。  
4. 第8月可视化默认接 Vue；静态台可只读 audit JSON，不强制画 SVG。

---

## 6. REST 能力按月增量（Web / curl 对照）

| 月 | 典型新增（学习期路径，以实现为准） |
|---|---|
| M1 | `/api/ai/chat` · `/api/ai/rag/ask` |
| M2 | `/api/ai/flow/*` · `/api/ai/eval/*` · `/api/ai/stats` |
| M3 | reindex / admin · audit 回放相关 |
| M4 | `/api/ai/feedback` · 角色/租户过滤行为 |
| M5 | `/api/ai/ledger/*` · `/api/ai/write/*` · decide→APPLY_WRITE |
| M6 | `/api/ai/erp/health` · items/inventory 只读 Port · contract suite |
| M7 | `/api/ai/console/rules` · rules dry-run · `rules-cases` |
| M8 | graph / view / audit 可视化 API（见第8月教材） |

完整前端对照见 Web 教材附录 C（已扩 M5～M7）。

---

## 7. 作品集与回归套件最小集

**文件：** [docs/PORTFOLIO.md](../PORTFOLIO.md)（仓库已给模板；按月粘贴证据）。

| 套件 | 从哪月起 | 用途 |
|---|---|---|
| 核心 rag / baseline | M3 | 防读路径退化 |
| `acl-forbidden` / tenant | M4 | 隔离 |
| `write-safety` | M5 | 写路径安全 |
| `adapter-contract` | M6 | Port 行为 |
| `rules-cases` | M7 | 规则 outcome |

---

## 8. 第9月及以后可选（未开课，勿与 M5～8 抢主线）

任选一条深挖即可，形式可用「完整章节教材」不必 30 天：

1. **观测大盘**（指标 / trace / 坏答案归因）  
2. **灰度 / 影子 Adapter**（双写比对，仍不接公司生产账套）  
3. **提示词运营**（版本、实验、与 rulesVersion 对齐）  
4. **真 pg 运维深化**（备份、迁移、向量表运维）

---

## 9. 审查结论（本次 gap-fill）

| 问题 | 处理 |
|---|---|
| `PORTFOLIO.md` 被多月引用但不存在 | 已补模板 |
| M3「观测→第5月」与真实 M5 受控写入冲突 | 收官段改为指向 M4，并把观测标为远期可选 |
| M4/M5/M6/M7 FAQ「只选一条」菜单过时 | FAQ/收官改为「已开课下月 + 链接」 |
| Web 仍写死「APPROVE≠写库」且 API 停在 M4 | 语义时间线 + 附录 API/交叉索引扩到 M5～M7 |
| 双控制台无说明 | 本章第5节 |
| 缺总索引式连贯文档 | **本文** |

---

## 修订

| 日期 | 说明 |
|---|---|
| 2026-08-15 | 首版：1～7月连贯审查 + HITL 时间线 + 双控制台 + API 增量 + 旧菜单对照 |
