# 第 3 个月合并讲义（Day1～Day30）+ 对照代码【详版】

> **定位：** 纯学习；通用 ERP；**不接公司生产、不自动过账/改库存。**  
> **形式：** 整月教材 + 可复制代码均在本文；由你自行落到工程。  
> **前置：** 第 1 月基础能力 + 第 2 月 Hybrid/Gate/Rerank、Flow HITL、Eval 入门。  
> **入口：** `docs/MONTH3.md`  
> **和第 2 月关系：** 第 2 月“能跑”；第 3 月“更像可维护的学习系统”（真库可选、审计、评测台、可选 OCR）。

---

## 第 3 月总目标

1. **检索生产化（学习级）：** JDBC + pgvector（或继续内存但接口完全切换）、重建任务、检索质量日志。  
2. **工作流深化：** 多节点、审计表（可用 H2/Postgres）、超时与重试语义（仍无业务写库）。  
3. **评测平台化：** 历史跑次、简易报告、本地/CI 一键脚本。  
4. **（可选）多模态：** OCR 单据文字进入 RAG/草稿的边界与骨架。  
5. **收官：** 作品集级 README + 第 4 月方向只选一条。

### 主线建议（可并行阅读，编码只追一条）

| 主线 | 适合你若… |
|---|---|
| A 检索 | 想深挖向量库与质量 |
| B 工作流 | 想深挖 HITL/审计 |
| C 评测 | 想深挖回归与报告 |
| D OCR | 有兴趣碰多模态（可选） |

**编码建议：** 每周以一条主线为主，其它主线读懂即可。

---

## 四周路线图

| 周 | Day | 主题 |
|---|---|---|
| 1 | M3-D1～7 | 检索生产化：PgStore、重建、质量日志 |
| 2 | M3-D8～14 | 工作流深化：多节点、审计、超时 |
| 3 | M3-D15～21 | 评测平台：跑次存储、报告、脚本 |
| 4 | M3-D22～30 | OCR 可选、作品集、复盘与下月 |

---

# 第 1 周｜检索生产化（学习级）

---

## M3-D1 差距诊断：从 Demo 检索到“可运维检索”

### 教材
第 2 月可能已有：`ChunkVectorStore`、Hybrid、Gate、Rerank。  
第 3 月要补的运维属性：

| 属性 | Demo | 本周目标 |
|---|---|---|
| 持久化 | 重启丢 | 表结构 + rebuild |
| 可观测 | 偶发日志 | 固定检索质量字段 |
| 变更 | 手改代码 | 重建任务可触发 |
| 维度治理 | 易忘 | 配置校验 embedding 维 |

### 当天验收
写出你当前实现的 Store 是内存还是 DB；列出缺的 3 个运维点。

---

## M3-D2 表结构与维度治理（详）

### 教材
向量维度必须与模型一致。建议配置显式声明：

```yaml
ai:
  rag:
    embedding-model: text-embedding-3-small
    embedding-dims: 1536
```

启动时：`embed("ping").length == embedding-dims`，不等则失败快速暴露。

### DDL（学习用，可 H2 不支持向量则仍用 Postgres；无库则跳过实装只读懂）

```sql
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE rag_chunk (
  id           TEXT PRIMARY KEY,
  doc_id       TEXT NOT NULL,
  section      TEXT NOT NULL,
  content      TEXT NOT NULL,
  embedding    vector(1536) NOT NULL,
  model        TEXT NOT NULL,
  dims         INT  NOT NULL,
  content_hash TEXT NOT NULL,
  updated_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX rag_chunk_doc_idx ON rag_chunk(doc_id);
-- 小数据可暂缓 ANN；数据大再上 ivfflat/hnsw
```

`content_hash`：内容未变可跳过重复 embed，省钱。

### 代码：hash

```java
public static String sha256(String s) {
    try {
        byte[] d = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(d);
    } catch (Exception e) {
        throw new IllegalStateException(e);
    }
}
```

### 当天验收
能解释为何表里要存 `model/dims/content_hash`。

---

## M3-D3 PgChunkVectorStore 骨架（JDBC）

> 无 Postgres 时：保持接口，实现类可先抛 `UnsupportedOperationException`，或用内存模拟“持久化文件”。

```java
public class PgChunkVectorStore implements ChunkVectorStore {
    private final JdbcTemplate jdbc;
    private final int dims;

    public PgChunkVectorStore(JdbcTemplate jdbc, int dims) {
        this.jdbc = jdbc;
        this.dims = dims;
    }

    @Override
    public void rebuild(List<IndexedChunk> chunks) {
        jdbc.update("DELETE FROM rag_chunk");
        for (IndexedChunk ic : chunks) {
            float[] v = ic.getVector();
            if (v.length != dims) throw new IllegalStateException("dims mismatch");
            // 将 float[] 转为 pgvector 参数的方式依驱动而定；学习期可先写成字符串 "[1,2,3]"
            jdbc.update(
                "INSERT INTO rag_chunk(id,doc_id,section,content,embedding,model,dims,content_hash) VALUES (?,?,?,?,?::vector,?,?,?)",
                ic.getChunk().getId(),
                ic.getChunk().getDocId(),
                ic.getChunk().getSection(),
                ic.getChunk().getContent(),
                toVectorLiteral(v),
                /* model */ "cfg",
                dims,
                sha256(ic.getChunk().getContent())
            );
        }
    }

    @Override
    public List<RetrievedChunk> search(float[] queryVector, int topK) {
        // SELECT ... ORDER BY embedding <=> ?::vector LIMIT ?
        // 映射为 RetrievedChunk
        return List.of();
    }

    private String toVectorLiteral(float[] v) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < v.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(v[i]);
        }
        return sb.append(']').toString();
    }

    @Override
    public int size() {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM rag_chunk", Integer.class);
        return n == null ? 0 : n;
    }
}
```

### 当天验收
rebuild 后 `size()>0`；search 能返回非空（有库时）。

---

## M3-D4 增量重建（按 hash 跳过）

### 教材
全量每次 embed 浪费。学习级增量：

```text
对每个 chunk:
  hash = sha256(content)
  if DB 已有同 id 且 hash 相同且 model 相同 → skip embed
  else embed + upsert
```

### 伪代码

```java
public void upsertAll(List<TextChunk> chunks, EmbeddingClient emb, String model) {
    for (TextChunk c : chunks) {
        String hash = sha256(c.getContent());
        if (existsSame(c.getId(), hash, model)) continue;
        float[] v = emb.embed(c.getContent());
        upsert(new IndexedChunk(c, v), model, hash);
    }
    // 删除语料中已不存在的 id（可选）
}
```

### 当天验收
第二次启动时日志出现 `skipped unchanged chunks = N`。

---

## M3-D5 检索质量日志（固定 schema）

每次 `/rag/ask` 打一行：

```json
{
  "traceId":"...",
  "retriever":"hybrid",
  "gate":"STRONG",
  "topScore":0.03,
  "docs":["01-purchase-flow-sample.md"],
  "sections":["1. 主链路"],
  "latencySearchMs":12,
  "latencyEmbedMs":80,
  "latencyLlmMs":900
}
```

分段计时：

```java
long t0 = System.nanoTime();
// embed/search
long searchMs = (System.nanoTime()-t0)/1_000_000;
```

### 当天验收
任意一次 RAG 请求日志可回答：用了什么检索、命中哪篇、三段耗时。

---

## M3-D6 重建 API（学习运维面）

```text
POST /api/ai/rag/reindex
→ { "chunks": 17, "skipped": 10, "embedded": 7, "tookMs": 1234 }
```

权限：学习项目可开放；将来公司必须鉴权。  
注意：耗时操作可同步（语料小）或 `@Async`（可选）。

```java
@PostMapping("/reindex")
public Map<String,Object> reindex() {
    long t0 = System.currentTimeMillis();
    ReindexResult r = corpusService.reindex();
    return Map.of("chunks", r.total(), "skipped", r.skipped(), "embedded", r.embedded(),
                  "tookMs", System.currentTimeMillis()-t0);
}
```

### 当天验收
改一个 md 后 reindex，只有该文件相关 chunk 重新 embed（看日志）。

---

## M3-D7 第 1 周复盘

口述：dims 校验、hash 跳过、reindex、质量日志各解决什么。  
对比题 4 条跑 hybrid，保存一周检索日志样本。

---

# 第 2 周｜工作流深化

---

## M3-D8 多节点业务流（仍无写库）

### 学习流升级

```text
RECEIVED
 → CLASSIFY          (规则/小提示：RAG_ONLY / RAG_TOOL / DRAFT)
 → RETRIEVE
 → TOOL(optional)
 → DRAFT
 → RISK_CHECK        (高风险话术检测)
 → WAIT_HUMAN
 → DONE / FAILED
```

`RISK_CHECK`：若用户原问题含“改库存/过账/删除”，强制 `needHuman=true` 并附加风险提示。

```java
boolean risky(String q) {
    return List.of("改库存","过账","删除","绕过审批","付款").stream().anyMatch(q::contains);
}
```

---

## M3-D9 审计表（H2/Postgres 均可）

```sql
CREATE TABLE flow_audit (
  id          BIGSERIAL PRIMARY KEY,
  flow_id     TEXT NOT NULL,
  from_state  TEXT,
  to_state    TEXT NOT NULL,
  event       TEXT NOT NULL,  -- START/ADVANCE/DECIDE/...
  detail_json TEXT,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

每次迁移写一条。学习意义：**可回放**。

```java
public interface FlowAuditSink {
    void append(String flowId, String from, String to, String event, String detailJson);
}
```

---

## M3-D10 状态迁移表（显式合法边）

```java
public class FlowTransitions {
    private static final Map<FlowState, Set<FlowState>> ALLOWED = Map.of(
        FlowState.RECEIVED, Set.of(FlowState.CLASSIFY, FlowState.FAILED),
        FlowState.CLASSIFY, Set.of(FlowState.RETRIEVE, FlowState.FAILED),
        FlowState.RETRIEVE, Set.of(FlowState.TOOL, FlowState.DRAFT, FlowState.FAILED),
        FlowState.TOOL, Set.of(FlowState.DRAFT, FlowState.FAILED),
        FlowState.DRAFT, Set.of(FlowState.RISK_CHECK, FlowState.FAILED),
        FlowState.RISK_CHECK, Set.of(FlowState.WAIT_HUMAN, FlowState.FAILED),
        FlowState.WAIT_HUMAN, Set.of(FlowState.DONE, FlowState.FAILED, FlowState.DRAFT),
        FlowState.DONE, Set.of(),
        FlowState.FAILED, Set.of()
    );

    public static void check(FlowState from, FlowState to) {
        if (!ALLOWED.getOrDefault(from, Set.of()).contains(to)) {
            throw new IllegalStateException(from + " -> " + to + " not allowed");
        }
    }
}
```

引擎每次 `setState` 前 `check`。

---

## M3-D11 超时与重试（节点级）

### 教材
- LLM 超时：该节点 FAILED 或降级草稿  
- Tool 超时：记录失败，DRAFT 时声明“未查到实时库存”  
- 人工等待：不超时也可；若做超时，变 FAILED 并审计  

```java
public <T> T withTimeout(Duration d, Callable<T> call) throws Exception {
    ExecutorService es = Executors.newSingleThreadExecutor();
    try {
        Future<T> f = es.submit(call);
        return f.get(d.toMillis(), TimeUnit.MILLISECONDS);
    } finally {
        es.shutdownNow();
    }
}
```

学习期每个外部调用配置独立 timeout。

---

## M3-D12 人工决定扩展：EDIT

```json
{ "decision": "EDIT", "editedAnswer": "人改后的文本", "note": "补充仓库字段" }
```

语义：人改草稿后 → DONE（仍不写 ERP）。  
审计 detail 保存 editedAnswer 长度与 note。

---

## M3-D13 工作流查询 API

```text
GET /api/ai/flow/{id}
GET /api/ai/flow/{id}/audit
GET /api/ai/flow?state=WAIT_HUMAN
```

列表便于做简易“待确认队列”（debug 页可用）。

---

## M3-D14 第 2 周复盘

检查：非法迁移失败；审计可回放；RISK_CHECK 生效；EDIT/APPROVE/REJECT 三分叉。

---

# 第 3 周｜评测平台化

---

## M3-D15 跑次（Run）模型

一次评测 = 一个 Run：

```sql
CREATE TABLE eval_run (
  run_id       TEXT PRIMARY KEY,
  started_at   TIMESTAMPTZ,
  finished_at  TIMESTAMPTZ,
  config_json  TEXT,     -- retriever/topK/promptVersion...
  total        INT,
  passed       INT
);

CREATE TABLE eval_run_case (
  run_id     TEXT,
  case_id    TEXT,
  passed     BOOLEAN,
  reasons    TEXT,
  answer     TEXT,
  sources    TEXT,
  PRIMARY KEY(run_id, case_id)
);
```

无 DB 时用 `evals/runs/{runId}.json` 落盘也行。

---

## M3-D16 Runner 写跑次

```java
public EvalRunResult runAndPersist(List<EvalCase> cases, Map<String,Object> configSnapshot) {
    String runId = UUID.randomUUID().toString().replace("-", "");
    // 执行每条 case，收集结果
    // 写入 eval_run / eval_run_case 或 JSON 文件
    return new EvalRunResult(runId, total, passed, failures);
}
```

`configSnapshot` 必须包含：retriever、topK、promptVersion、embeddingModel。

---

## M3-D17 报告输出

```text
GET /api/ai/eval/runs
GET /api/ai/eval/runs/{runId}
```

Markdown 报告模板：

```markdown
# Eval Report {runId}
- config: ...
- pass: {passed}/{total}
## Failures
- {caseId}: {reasons}
```

可 `Files.writeString(path, reportMd)`。

---

## M3-D18 本地一键脚本

`scripts/run-eval.sh`：

```bash
#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mvn -q -DskipTests package
# 方式1：测内嵌 JUnit
mvn -q -Dtest=RagEvalIT test
# 方式2：curl 触发
# curl -s -X POST http://localhost:8080/api/ai/eval/run
```

### CI 概念（GitHub Actions 示意，可不启用）

```yaml
# .github/workflows/eval.yml（概念）
# on: [push]
# jobs:
#   eval:
#     runs-on: ubuntu-latest
#     steps:
#       - uses: actions/checkout@v4
#       - uses: actions/setup-java@v4
#         with: { distribution: temurin, java-version: 21 }
#       - run: mvn -q -Dtest=RagEvalIT test
```

学习项目可先本地脚本，不一定真开 CI。

---

## M3-D19 回归基线（Baseline）

选定一次“满意”的 run 为 baseline：

```text
evals/baseline.json  { "runId":"...", "passed":10, "total":12 }
```

新跑次：若 `passed < baseline.passed` → 退出码非 0（门禁）。

```java
if (passed < baselinePassed) {
  System.exit(2);
}
```

---

## M3-D20 评测分类扩展

除 RAG 外增加：

| 套件 | 测什么 |
|---|---|
| chat-safety | 注入、改库存、要系统提示 |
| flow-hitl | start 后必 WAIT_HUMAN；decide 合法 |
| tool-read | 假库存命中；禁止写工具存在性反射检查 |

写工具类名扫描（学习级）：

```java
// 失败如果 classpath 出现 *WriteInventory* 之类（按你的命名约定）
```

---

## M3-D21 第 3 周复盘

具备：Run 持久化、报告、脚本、baseline 门禁意识。  
本周至少留 3 份 run 报告在 `evals/runs/`。

---

# 第 4 周｜OCR 可选、作品集、收官

---

## M3-D22 多模态边界（先讲清再碰代码）

### 能做什么
图片/PDF 扫描件 → OCR 文本 → 进入草稿抽取或 RAG 资料。

### 不能幻想什么
OCR 错字会导致字段全错；不能替代人工确认；本月仍无写库。

### 架构

```text
image bytes → OcrClient.recognize() → text
text → DraftService / 临时 chunk 检索
→ WAIT_HUMAN
```

---

## M3-D23 OcrClient 骨架

```java
public interface OcrClient {
    OcrResult recognize(byte[] content, String filename);
}

public class OcrResult {
    public String text;
    public double confidence; // 若无则 0
}

/** 学习替身：不接云 OCR，直接读同名 .txt 固件 */
public class FakeOcrClient implements OcrClient {
    public OcrResult recognize(byte[] content, String filename) {
        // 从 fixtures/ocr/xxx.txt 读预期文字
        OcrResult r = new OcrResult();
        r.text = "供应商:华东供应\n存货编码:A001\n数量:100";
        r.confidence = 0.9;
        return r;
    }
}
```

真 OCR（可选）：对接任意云厂商 HTTP；**Key 走环境变量**。

---

## M3-D24 OCR → Draft API

```text
POST /api/ai/draft/from-image   multipart file
→ { ocrText, draftFields, needHuman:true, warnings:["OCR 可能有误"] }
```

提示词强调：字段来自 OCR 文本，不确定就进 missing。

---

## M3-D25 调试台增强

debug 页增加：
- 上传图片（若做了 OCR）  
- 显示最近 eval run pass 率  
- 待确认 flow 列表  

---

## M3-D26 作品集 README 模板（写进你的仓库）

```markdown
# ERP AI Learning Assistant
## 能力
- Chat 结构化助手
- RAG（keyword/vector/hybrid + gate/rerank）
- Flow HITL
- Eval runner + baseline
## 如何本地运行
## 架构图
## 明确不做
公司权限/自动过账/微调…
```

---

## M3-D27 端到端彩排剧本

1. reindex  
2. eval 跑过 baseline  
3. rag 问 2 题看 sources  
4. flow 走完 APPROVE  
5. （可选）OCR draft  
6. 导出一份 eval 报告  

全程录屏或记笔记均可。

---

## M3-D28 架构终审清单

- [ ] Store 可切换内存/PG  
- [ ] Hybrid+Gate+Rerank 可配置  
- [ ] Flow 合法迁移 + 审计  
- [ ] Eval 可门禁  
- [ ] 无写库存工具  
- [ ] Key 不在 Git  
- [ ] 文档声明学习边界  

---

## M3-D29 口述自测 12 题

1. content_hash 的作用？  
2. embedding dims 校验何时做？  
3. reindex API 的风险（若暴露公网）？  
4. 审计表最少字段？  
5. 合法迁移表防止什么？  
6. RISK_CHECK 节点为何存在？  
7. eval_run 为何要存 config_json？  
8. baseline 门禁如何失败？  
9. FakeOcr 的意义？  
10. OCR 草稿为何强制 needHuman？  
11. 第3月相对第2月的“可运维”体现在哪？  
12. 你下月主线选什么？为何？  

---

## M3-D30 收官与第 4 月

### 本月交付物
- 检索可运维（接口/库/reindex/日志）  
- 工作流可回放（审计）  
- 评测可门禁（baseline）  
- （可选）OCR 通路  
- 作品集 README  

### 第 4 月可选（仍然只选一条）
1. 真正的权限模型（用户/角色/文档 ACL）— 仍建议在学习仓模拟  
2. 多租户 RAG  
3. 在线反馈飞轮（点赞/点踩 → 题集）  
4. 前端正式化（不止 debug）  

### 结束语
第 1 月学会“生成可控”，第 2 月学会“流程与质量”，第 3 月学会“可运维与可证明”。  
**能证明（评测/审计/日志）比能演示更接近工程师。**

---

# 附录

## 附录 A｜第 3 月包结构增量

```text
rag/store/PgChunkVectorStore
rag/CorpusReindexService
flow/FlowTransitions FlowAuditSink
eval/EvalRunRepository EvalReportWriter
ocr/OcrClient FakeOcrClient
scripts/run-eval.sh
evals/runs/...
evals/baseline.json
```

## 附录 B｜配置增量示例

```yaml
ai:
  rag:
    store: memory          # memory | pg
    embedding-dims: 1536
    reindex-on-startup: true
  flow:
    audit-enabled: true
  eval:
    baseline-path: evals/baseline.json
  ocr:
    provider: fake         # fake | http
```

## 附录 C｜文档索引

| 文档 | 路径 |
|---|---|
| 第1月大纲 | `docs/lessons/MONTH_30_DAY_PLAN.md` |
| 第1月D13-30 | `docs/lessons/day13-30-combined.md` |
| 第2月详版 | `docs/lessons/MONTH2_DAY1-30_COMBINED.md` |
| 第3月详版 | 本文 |

---

## 修订

| 日期 | 说明 |
|---|---|
| 2026-08-15 | 第3月 Day1～30 合并讲义+对照代码（详版） |
