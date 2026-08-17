# 口述 / 自测标准答案（MONTH1～8 · WEB）

> **用法：** 先闭卷自测，再对照本文。开放题（第5月方向等）给**合格答法样例**，不是唯一解。  
> **风格：** 与 MONTH3「参考要点」同级——短句要点，可录音复述。  
> **入口：** 各合订本口述节也有就地答案；本文为总索引。

---

## M1｜Day14 RAG 周复盘（6 题）

1. **RAG 五步** — 文档加载 → 切分 → 检索 → 拼资料进 Prompt → LLM 生成；`sources` 来自检索。  
2. **heading vs overlap** — heading：按标题语义块，适合手册；overlap：固定窗+重叠，适合无标题长文。  
3. **索引/查询 embed** — 索引：每个 chunk；查询：用户 question；必须同一 Embedding 模型。  
4. **Hybrid** — 关键词保专名，向量保同义改写；合并后更稳。  
5. **空命中** — `sources=[]`，拒答或声明教材未覆盖；禁止模型伪造章节。  
6. **/chat vs /rag** — Chat 管怎么说；RAG 管依据什么说；更新知识改文档而非只改 Prompt。

---

## M1｜Day17 工作流 vs Agent（3 题）

1. **为何偏工作流** — ERP 高风险、要审计与人工确认；纯 Agent 难控合法跳转。  
2. **三类必人工** — 过账/改库存、付款、删单或绕审批类操作。  
3. **白名单支撑 HITL** — 只暴露只读 Tool；写操作不进模型 Tool，只走 APPROVE 后 Gateway。

---

## M1｜Day21 第3周复盘（4 题）

1. **Tool vs RAG** — RAG 取教材依据；Tool 取实时/结构化查询（只读）。  
2. **草稿 need_human** — 建议非过账；防误提交；产品边界。  
3. **路径 A/B** — 开放：A 深 Tool；B 深草稿+HITL；说清利弊即可。  
4. **演示选哪个** — 开放：选你最稳的一条（常选 RAG+sources 或草稿 HITL）。

---

## M1｜Day26 数据流复习（3 题）

1. **chat messages** — system（含 few-shot）→ 历史 user/assistant → 本轮 user。  
2. **rag ask** — loader → chunker → retriever → Gate? → prompt → LLM → sources 映射。  
3. **tool** — 白名单校验 → 执行只读 → 结果进上下文/日志 → 禁止写库存。

---

## M1｜Day29 口述 15 题

1. **provider vs model** — provider=接入协议（mock/openai-compatible）；model=具体模型名。  
2. **多轮更贵** — 历史 messages 重复计入 prompt tokens。  
3. **few-shot** — 教格式与口径；不代替检索真实制度、不保证无幻觉。  
4. **RAG 五步** — 见 Day14-1。  
5. **sources 不让模型编** — 引用必须来自检索 chunk 元数据，否则假权威。  
6. **索引/查询 embed** — 见 Day14-3。  
7. **Hybrid** — 见 Day14-4。  
8. **空/弱命中** — EMPTY：拒答+空 sources；WEAK：降置信、`need_human=true`。  
9. **Tool vs RAG** — 见 Day21-1。  
10. **偏工作流** — 见 Day17-1。  
11. **禁写库存 Tool** — 风险不对称；本月只做问答辅助。  
12. **missing 字段** — 标草稿不完整，逼人工补全。  
13. **注入三例** — 「忽略上文」「假装已审批」「输出密钥」；防线：系统提示分层、拒答、不执行写工具。  
14. **降级不静默瞎答** — 降级要可见（日志/文案）；静默会给假确定性。  
15. **单变量实验** — 每次只改一个因子（retriever/topK/prompt），其余固定，对比 sources/通过率。

---

## M2｜D7 第1周复盘

1. **RRF 不需同量纲** — 用排名而非原始分融合。  
2. **minScore** — 量纲随 keyword/余弦/RRF 而变，要用回归题标定。  
3. **recallK vs topK** — recall 多召回候选；topK 最终进提示词条数。  
4. **换 embedding** — 全量重建向量索引；改配置+重启/reindex。  
5. **Store 最小集** — upsert/search（+可选 rebuild/delete）；统一 RetrievedChunk。

---

## M2｜D14 第2周复盘

1. **状态机非纯 Agent** — 合法边、审计、HITL；可控可测。  
2. **RETRIEVING/DRAFTING** — 检索写 hits/sources；起草写 draftAnswer/warnings。  
3. **APPROVE** — 推进状态/接受草稿展示；**不等于写库**。

---

## M2｜D21 第3周复盘

1. **失败先查检索还是生成** — 先看 sources/Gate；资料错再查 Prompt。  
2. **promptVersion** — 评测可归因、可回滚口径。  
3. **安全题 100%** — 红线不可漂；过账/删库类必须拒。  
4. **jsonl 一行一条** — 流式追加、易解析、坏一行不毁全文。  
5. **stats 重启清零** — 学习期可接受；生产要外置存储。

---

## M2｜D29 口述 15 题

1. **rebuild 时机** — 切分/模型/语料变；启动或 reindex。  
2. **RRF 的 k** — 平滑排名贡献，减轻第一名霸权。  
3. **WEAK prompt** — 声明依据不足，强制 need_human、压 confidence。  
4. **recall10→top3** — 两路各召回≤10 → RRF 融合 → 取 3。  
5. **非法 decide** — 拒迁（409/错态），写审计，不改业务状态。  
6. **APPROVE 语义** — 人工确认继续流程；≠过账。  
7. **eval forbid** — 断言禁止出现危险/越权内容。  
8. **promptVersion** — 见 D21-2。  
9. **/stats** — 调用次数、延迟、token/成本等学习观测。  
10. **Hybrid 打两路日志** — 对比哪路贡献命中，便于调参。  
11. **DONE 不过账** — 产品边界；写库另开 Gateway 月。  
12. **Rerank 输入** — 来自召回候选列表，非整库。  
13. **换 embedding 三步** — 改模型配置 → 全量 reindex → 跑 eval。  
14. **debug 按钮** — 至少：提问、切换 retriever、看 sources/Gate。  
15. **最大风险** — 无权限隔离或提前写入；第2月仍只读+HITL。

---

## M3｜D7 / D14 / D21 / D29

合订本已就地写「参考要点」（与标准答案同级）。请直接对照：

→ [MONTH3_DAY1-30_COMBINED.md](./lessons/MONTH3_DAY1-30_COMBINED.md)（搜 `参考要点` / `口述题 + 参考要点`）

要点纪律不变：Store/reindex、Flow 审计、eval 快照、OCR 强制人工、APPROVE≠写库。

---

## M4｜D7 ACL 周复盘（8 题）

1. **Principal vs User** — 学习头模拟身份；非完整认证体系。  
2. **Filter 位置** — 建议检索后、进 Prompt 前；也有人放 Rerank 后，需防泄露。  
3. **forbid-leak** — 不可见文档正文不得出现在回答/sources。  
4. **ADMIN** — 映射为可检索全集（仍是学习假权限）。  
5. **测正文子串** — 证明没把禁看内容喂给模型或输出。  
6. **Flow 队列过滤** — 待办也不能看见无权限实例/摘要。  
7. **学习头可伪造** — Demo 级；生产必须真认证；学习仓明确「不可当真」。  
8. **quality log** — 记 principal/roles、过滤前后命中数。

---

## M4｜D14 反馈飞轮（6 题）

1. **不自动改 prompt** — 噪声/误点；需人工晋升评测题。  
2. **traceId** — 请求生成 → 日志/反馈/eval 同一 ID。  
3. **WRONG vs UNSAFE** — 错答 vs 安全违规；UNSAFE 优先进安全套件。  
4. **JSONL≈audit** — 追加只写、易回放、学习期够用。  
5. **drafts vs suite** — 草案待审；suite 才进回归门禁。  
6. **stats 演示** — 展示反馈计数/种类，证明飞轮在转。

---

## M4｜D21 多租户（8 题）

1. **tenant vs org** — 学习简化隔离键；≠公司真实多组织模型。  
2. **键含 tenantId** — 防串库；search/rebuild 作用域正确。  
3. **default-tenant** — 易误绑数据；更稳：缺省拒绝或显式 learning。  
4. **ADMIN 跨租户** — 学习可设计；默认建议仍要带头，避免习惯性越权。  
5. **reindex 按租户** — 只重建当前租户，免误伤。  
6. **串租户负例** — A 租户问到 B 语料必须空/拒。  
7. **顺序** — 先 tenant 作用域，再 ACL 过滤。  
8. **排障字段** — tenantId、roles、命中 docs、gate、latency。

---

## M4｜D29 口述 20 题

1. Demo 无假权限 → 演示越权/假合规，习惯带病进生产思维。  
2. Principal=谁；DocAcl=谁可见哪些 doc；Filter=检索后过滤。  
3. 检索/Rerank 之后、拼 Prompt 之前（项目约定）。  
4. 禁止回答/sources 泄露不可见正文。  
5. 待办列表/摘要也不能越权。  
6. useful / wrong / unsafe。  
7. 点踩噪声大，乱改模型/提示词无回归。  
8. 生成 ID → 响应返回 → 反馈/日志/eval 复用。  
9. drafts 人工确认后才进 suite。  
10. 反馈→日志→晋升题→回归。  
11. 学习隔离键 vs 企业组织主数据。  
12. 索引/查询键空间隔离。  
13. 拒绝缺省，或落到明确 default（需文档化）。  
14. 跨租户问句不得命中他租资料。  
15. tenant 定库/索引范围，ACL 定角色可见 doc。  
16. 角色/租户切换、反馈、eval 彩排一体。  
17. A 权限 sources；B 反馈进题；C 租户隔离负例。  
18. 隔离后 sources 仍只能来自允许集。  
19. 不接 SSO/真权；不自动改模型；不接生产库。  
20. 开放：观测/pg 深耕/可视化/规则等只选一条并说理由。

---

## M5｜D7 / D14 / D21 / D29

### D7（8）
1. 先有 RAG/HITL/评测/ACL 才敢写。  
2. **模型永不直接持有无审批写库存工具；写只经 APPROVE→Gateway。**  
3. READ 只查；CONTROLLED_WRITE 审批后写假账。  
4. 唯一入口才能统一鉴权/幂等/审计。  
5. 内存假账；不接公司库。  
6. 读看 DocAcl；写看写角色+Gateway。  
7. Chat 路径无写命令通道。  
8. 关期间拒绝过账类写。

### D14（8）
1. 仅合法迁移且 APPROVE 后进入。  
2. REJECT 不 enqueue 写。  
3. DUPLICATE=幂等命中已成功；FAILED=执行失败。  
4. 扫描禁止 write 类 Tool 名。  
5. Gateway/幂等存储层，随 commandId。  
6. Flow → FAILED（并审计）。  
7. suggest 给建议；write 改账（禁直出）。  
8. reverse 仅受控补偿路径+权限。

### D21（8）
1. 未审批不写、越权/关期间拒绝、幂等不双写等。  
2. 负例路径账本不变。  
3. 写安全回归门禁，防回退。  
4. who/when/what/reason（+commandId）。  
5. 错误写入反馈→人工确认→write-safety 题。  
6. expect 期间关闭错误码/拒写。  
7. 读评测看答案/sources；写评测看账本副作用。  
8. 写审计事件 vs Flow 状态迁移审计。

### D29（20）
1. 先有 RAG/HITL/评测/ACL 才敢写。  
2. **模型永不直接持有无审批写库存工具；写只经 APPROVE→Gateway。**  
3. Chat 路径无写命令通道。  
4. APPROVE 推进 HITL；APPLY_WRITE 才调 Gateway。  
5. commandId 幂等业务键；traceId 观测关联。  
6. DUPLICATE 对账本是成功（不双写）。  
7. reverse 是受控补偿；≠ DB 自动 rollback。  
8. write-safety 测写副作用；acl-forbidden 测读越权。  
9. 关期间 → FAILED + WRITE_REJECTED。  
10. suggest 给建议；write 改账（禁直出）。  
11. 写角色/权限在 Gateway 校验。  
12. before/after 来自假账本执行结果。  
13. console 只能 decide，不能绕过 Gateway。  
14. 写安全不允许「差不多」；门禁常要求全过。  
15. 反馈→人工确认→晋升 write-safety 题。  
16. PostingState 按实现（DRAFT/POSTED/CLOSED 等）。  
17. FAILED vs REJECTED 分清审计语义。  
18. 假账重启可丢，学习可接受。  
19. 开放：常选 Port/Adapter 稳定化并说理由。  
20. 8 分钟 pitch：假账本 + HITL + 禁写 + 幂等/审计；APPROVE≠过账。

---

## M6｜D29 口述 20 题

1. 模型再强，接口一变编排全改、难测难切环境。  
2. **业务只依赖 Port；学习用 Fake/Sandbox；换 ERP 只换适配器。**  
3. Port=稳定接口；Adapter=实现。  
4. 读写分离：查询与写副作用、权限与重试策略不同。  
5. Gateway 依赖 Port，不 new 具体账本类。  
6. TIMEOUT/CONFLICT/PERIOD_CLOSED/UNAUTHORIZED/VALIDATION/UNAVAILABLE（按你实现六码）。  
7. 401/期间关闭/校验错误通常不重试。  
8. 同一断言跑多适配器，防「假绿」。  
9. fake 内存；sandbox 打本地 stub HTTP。  
10. Flow commandId → Gateway → Port 头/参数。  
11. OPEN 时快速失败，防雪崩。  
12. 聚合适配器是否可用，演示/探活。  
13. Adapter 侧做外部模型↔领域模型映射。  
14. OpenAPI 描述外部契约；Port 是领域边界。  
15. 学习边界；无公司 SSO/库。  
16. tenant/user/roles 进适配器审计上下文。  
17. A 只读 Port；B 过账+幂等；C 关期间/超时降级。  
18. 配置切换 fake→sandbox，行为对比。  
19. 规则引擎外置 CLASSIFY/RISK。  
20. pitch：Port 边界 + 契约测 + 不接生产。

---

## M7｜D29 口述 20 题

1. 确定性规则走引擎；LLM 理解/草稿/解释。  
2. DENY 禁止；WARN 可继续但提示。  
3. DENY/ROUTE 可配置短路，后续规则不跑。  
4. ROUTE 把意图导向 RAG/TOOL 等节点。  
5. WriteGateway 前 write-precheck。  
6. 审计/eval 快照可回归。  
7. ruleId、result、context 摘要等。  
8. **冲突时规则优先。**  
9. 禁止；规则只读 Port。  
10. 查期间状态，供决策表。  
11. 校验失败不加载，保留旧版。  
12. Gate 管检索强弱；Rule 管业务允许与否。  
13. 给定 context → ALLOW/DENY/ROUTE。  
14. classify / risk / write-precheck。  
15. 关期间 DENY、缺仓 WARN 等。  
16. 无 FINANCE 角色则写前 DENY（示例策略）。  
17. 试跑 Context，不改生产状态。  
18. A 关期间 DENY；B 口语 ROUTE；C Prompt 诱导仍 DENY。  
19. 规则单测 + rules-cases + 相关回归。  
20. 开放：工作流可视化等。

---

## M8｜第19章（已有参考答法，此处补完整口述句）

1. 学习可视化=只读投影；BPM=设计器+引擎；本月不接公司 BPM。  
2. graph 静态可缓存；view 随实例变。  
3. legalTransitions 必须与 FlowTransitions 同源。  
4. 前端禁止拖边改状态；只走 decide。  
5. APPROVE≠写库；写在 APPLY_WRITE→Gateway。  
6. RISK_CHECK 上 RULE_DENIED；之后 FAILED，不能再 APPROVE 当成功写。  
7. APPLY_WRITE 调 Gateway；假账本/适配器落地。  
8. 回放用 audit 事件累积 replay 边高亮。  
9. 稳定 edgeId 贯通拓扑与审计。  
10. SVG 易 a11y/命中检测/教学透明。  
11. current=WAIT_HUMAN 且运行中且非只读。  
12. demo 禁 decide/start；可回放观看。  
13. 用 traceId 串 Chat/Flow/Write 日志。  
14. `:key` 强制重建，清回放态。  
15. ledgerRef、幂等键、成功/失败徽标。  
16. HITL 可点；WRITE 只读样式。  
17. toast + 重新拉 view；不前端改边。  
18. 可视化是投影层，不改编排职责。  
19. pending 列表；view 单实例快照。  
20. 选修：WS、大图、多版本等（见第20章）。

---

## WEB｜第19章 口述提纲标准答法

**1′ 定位** — Vue3+Vite 学习控制台，对接 `erp-ai-assistant` 的 `/api/ai/*`；不接公司 SSO。  
**2′ ACL** — Pinia/请求头模拟角色与租户；切换后 RAG sources 变化可演示。  
**3′ RAG** — 展示 sources 与 Gate；强调引用非模型编造。  
**4′ Flow** — WAIT_HUMAN → decide；口述「APPROVE≠写库」。  
**5′ 反馈/观测** — 反馈挂 traceId；stats/eval 可回归。  
**6′ Q&A** — 边界：假权限、假账本、学习仓。

**收官一句（标准稿）：**  
「我用 Vue3 控制台接学习期 ERP AI：模拟 ACL/租户，RAG 展示 sources，反馈挂 traceId，Eval 可回归；Flow 的 APPROVE 不等于写库。」

**本章自检「对照通过」标准：**  
`npm run build` 成功；dev 无红错；经 proxy 的 `/api/ai/*` 为 200（若后端已启）；能按上面提纲 8～10 分钟讲完且含 APPROVE≠写库。

---

## 修订

| 日期 | 说明 |
|---|---|
| 2026-08-17 | 首版：补 MONTH1～2、4～7 口述标准答案；M3/M8 已有则交叉引用并加长述；WEB 口述提纲标准答法 |
| 2026-08-17 | 补全合订本就地答案（M1 D14/17、M2 D14、M5～7 D29、M8 第19章全文、WEB 第19章）；入口页互链 |
