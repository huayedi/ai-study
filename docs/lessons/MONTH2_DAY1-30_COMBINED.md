# 第 2 个月合并讲义（Day1～Day30）+ 对照代码

> **定位：** 纯学习；通用 ERP；不接公司生产系统。  
> **形式：** 整月教材 + 示例代码均在本文；默认由你复制到工程自行改造。  
> **前置：** 第 1 个月（Chat / Prompt / RAG 关键词·向量 / Tool·草稿概念）已学完。  
> **仓库现状对照：** `erp-ai-assistant`（Chat + 学习版 RAG）；你本地可能已有向量/切分改动。

---

## 第 2 月目标

1. 检索加深：进程内向量 → 可切换的持久化思路（PGVector 讲清 + 学习级实现提纲）+ Hybrid + 简易 Rerank  
2. 工作流：人工确认节点的状态机（仍无盲目写入业务库）  
3. 评测：题集脚本化、改配置可回归  
4. 工程：简易调试台（可选前端）+ 配置/安全加固意识  
5. 收官：个人「准应用架构」与第 3 月方向  

---

## 四周结构

| 周 | Day | 主题 |
|---|---|---|
| 1 | M2-D1～7 | 检索加深：向量持久化、Hybrid、Rerank |
| 2 | M2-D8～14 | 工作流与 Human-in-the-loop |
| 3 | M2-D15～21 | 评测自动化与观测加固 |
| 4 | M2-D22～30 | 调试台、场景串联、复盘与下月 |

---

# 第 1 周｜检索加深

## M2-D1 回顾与差距清单

### 教材
第 1 月 RAG 已具备：切分、关键词、（自学）向量、sources 纪律。  
第 2 月要补：

| 能力 | 第 1 月 | 第 2 月 |
|---|---|---|
| 向量存储 | 多在内存 | 讲清 PGVector / 表结构，可选用 JDBC 学习实现 |
| 融合 | 可能未做 | Hybrid + 融合分 |
| 重排 | 无 | 简易 Rerank |
| 未命中 | 可能口头 | 统一策略对象 |

### 对照代码（差距检查清单，不强制今天改）

```text
RagRetriever
KeywordRetriever
VectorRetriever          // 若你已实现
HybridRetriever          // 本周要有
EmbeddingClient
RagService
```

---

## M2-D2 PGVector / 持久化向量在架构中的位置

### 教材
进程内 `List<IndexedChunk>` 适合学习；缺点：重启丢失、难共享、大数据量暴力扫描慢。

持久化向量库角色：

```text
chunk 文本 + embedding[]  → 写入 DB
提问 embedding            → SQL/扩展做近邻搜索 → TopK chunk
```

PostgreSQL + `pgvector` 常见思路：

```sql
-- 概念 DDL（学习理解用；是否在本地装 Postgres 自定）
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE rag_chunk (
  id            TEXT PRIMARY KEY,
  doc_id        TEXT NOT NULL,
  section       TEXT,
  content       TEXT NOT NULL,
  embedding     vector(1536)  -- 维度必须与 embedding 模型一致
);

-- 相似度查询概念（运算符因版本而异，以官方文档为准）
-- SELECT id, content, 1 - (embedding <=> :qvec) AS score
-- FROM rag_chunk ORDER BY embedding <=> :qvec LIMIT :k;
```

### 要点
- **维度**与模型绑定，换模型要重建  
- 元数据（doc_id/section）与向量一起存，sources 才好做  
- 学习期可以：接口先抽象 `ChunkVectorStore`，内存实现 + JDBC 实现二选一  

### 自行编码接口草案

```java
public interface ChunkVectorStore {
    void rebuild(List<IndexedChunk> chunks);
    List<RetrievedChunk> search(float[] queryVector, int topK);
}
```

```java
public class InMemoryChunkVectorStore implements ChunkVectorStore { /* ... */ }
// public class PgChunkVectorStore implements ChunkVectorStore { /* 可选 */ }
```

---

## M2-D3 HybridRetriever 设计与代码骨架

### 教材
两路召回 → 融合 → 截断 TopK。保留两路原始命中便于日志。

### 代码骨架（可复制后实现）

```java
public class HybridRetriever implements RagRetriever {
    private final RagRetriever keyword;
    private final RagRetriever vector;
    private final int rrfK; // e.g. 60

    public HybridRetriever(RagRetriever keyword, RagRetriever vector, int rrfK) {
        this.keyword = keyword;
        this.vector = vector;
        this.rrfK = rrfK;
    }

    @Override
    public List<RetrievedChunk> retrieve(String question, List<TextChunk> corpus, int topK) {
        List<RetrievedChunk> a = keyword.retrieve(question, corpus, topK);
        List<RetrievedChunk> b = vector.retrieve(question, corpus, topK);
        Map<String, Double> fused = new HashMap<>();
        Map<String, TextChunk> byId = new HashMap<>();
        addRrf(a, fused, byId);
        addRrf(b, fused, byId);
        return fused.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .limit(topK)
                .map(e -> new RetrievedChunk(byId.get(e.getKey()), e.getValue()))
                .toList();
    }

    private void addRrf(List<RetrievedChunk> list, Map<String, Double> fused, Map<String, TextChunk> byId) {
        for (int i = 0; i < list.size(); i++) {
            TextChunk c = list.get(i).getChunk();
            byId.put(c.getId(), c);
            double add = 1.0 / (rrfK + i + 1);
            fused.merge(c.getId(), add, Double::sum);
        }
    }
}
```

配置：`ai.rag.retriever=hybrid`

---

## M2-D4 未命中策略对象化

### 教材
把「空 / 弱 / 强」变成明确策略，避免散落 if。

```java
public enum RetrievalStrength { EMPTY, WEAK, STRONG }

public class RetrievalDecision {
    private RetrievalStrength strength;
    private List<RetrievedChunk> chunks;
    private String userHint; // 可拼进 prompt 或直接返回
}
```

```java
public class RetrievalGate {
    private final double minScore; // 对 hybrid/rrf 分要重新标定

    public RetrievalDecision decide(List<RetrievedChunk> top) {
        if (top == null || top.isEmpty()) {
            return empty("教材未检索到相关片段，请换种问法或补充文档。");
        }
        if (top.get(0).getScore() < minScore) {
            return weak(top, "检索相关度偏低，回答仅供参考，建议人工核对。");
        }
        return strong(top);
    }
}
```

`RagService`：EMPTY 时可短路不调 LLM，或调 LLM 但强制「不可编造」。

---

## M2-D5 简易 Rerank（学习级）

### 教材
Retriever 负责「召回」，Reranker 负责「精排」。  
真交叉编码器可后置；学习级可用规则分：

```text
finalScore = 0.7 * retrievalScoreNorm + 0.3 * lexicalOverlap(question, chunk)
```

或：先召回 10，再用更严关键词重叠重排取 3。

### 代码骨架

```java
public interface Reranker {
    List<RetrievedChunk> rerank(String question, List<RetrievedChunk> candidates, int topK);
}

public class LexicalReranker implements Reranker {
    @Override
    public List<RetrievedChunk> rerank(String question, List<RetrievedChunk> candidates, int topK) {
        // 1) 对 candidates 计算 overlap
        // 2) 与原 score 加权
        // 3) 排序截断
        return candidates;
    }
}
```

编排：`retrieve(topK*3) → rerank(topK)`。

---

## M2-D6 检索配置一览（建议写入 yml）

```yaml
ai:
  rag:
    retriever: hybrid        # keyword | vector | hybrid
    top-k: 3
    recall-k: 10             # 召回再精排
    rrf-k: 60
    min-score: 0.01          # 按你的分数量纲调
    embedding-model: ...
    chunk-strategy: heading
```

改一参做一次对比，记日志：retriever、recallK、命中 docId 列表。

---

## M2-D7 第 1 周复盘

口述：
1. 内存向量 vs PGVector 差在哪  
2. RRF 在融合什么  
3. Gate 三种强度  
4. Rerank 与 Retrieve 分工  

自测题：口语问法 + 专名问法各 3 条，对比 keyword/vector/hybrid 的 sources。

---

# 第 2 周｜工作流与 HITL

## M2-D8 为何 ERP 要用状态机

### 教材
Agent 自动规划适合探索；ERP 合规路径适合 **预定义状态迁移**。

示例：「库存问询建议」工作流（学习版，无写库）：

```text
RECEIVED → RETRIEVE_DOC → OPTIONAL_TOOL_QUERY → DRAFT_ANSWER → WAIT_HUMAN → DONE
                                         ↘ REJECTED
```

任何「写库存/过账」状态本月仍不实现，只保留注释位。

---

## M2-D9 状态枚举与持久化（学习级）

```java
public enum FlowState {
    RECEIVED,
    RETRIEVING,
    QUERYING_TOOL,
    DRAFTING,
    WAIT_HUMAN,
    DONE,
    FAILED
}

public class FlowInstance {
    private String flowId;
    private FlowState state;
    private String userQuestion;
    private String draftAnswer;
    private List<String> sources;
    private String humanDecision; // APPROVE / REJECT / null
}
```

学习期可用 `ConcurrentHashMap<String, FlowInstance>`；不必上 DB。

---

## M2-D10 工作流引擎骨架

```java
public class SimpleFlowEngine {
    public FlowInstance start(String question) {
        FlowInstance fi = new FlowInstance();
        fi.setFlowId(UUID.randomUUID().toString());
        fi.setState(FlowState.RECEIVED);
        fi.setUserQuestion(question);
        // save
        return advance(fi);
    }

    public FlowInstance advance(FlowInstance fi) {
        switch (fi.getState()) {
            case RECEIVED -> { fi.setState(FlowState.RETRIEVING); /* retrieve */ fi.setState(FlowState.DRAFTING); }
            case DRAFTING -> { /* call LLM */ fi.setState(FlowState.WAIT_HUMAN); }
            case WAIT_HUMAN -> { /* 等待 API 传入人工决策 */ }
            default -> {}
        }
        return fi;
    }

    public FlowInstance humanDecide(String flowId, String decision) {
        // APPROVE → DONE；REJECT → FAILED 或回 DRAFTING
        return null;
    }
}
```

API 建议：

```text
POST /api/ai/flow/start     {question}
POST /api/ai/flow/{id}/resume
POST /api/ai/flow/{id}/decide  {decision: APPROVE|REJECT, note?:...}
GET  /api/ai/flow/{id}
```

---

## M2-D11 HITL 产品语义

| 决策 | 含义（学习版） |
|---|---|
| APPROVE | 人接受草稿建议；**仍不自动写 ERP** |
| REJECT | 人否定；可附 note 重生成或结束 |
| EDIT（可选） | 人改正文后再 DONE |

界面上即使以后做前端，也要展示：sources、tool 结果、风险提示。

---

## M2-D12 把 RAG/Tool 挂进节点

节点职责单一：

| 状态 | 调用 |
|---|---|
| RETRIEVING | RagRetriever |
| QUERYING_TOOL | 仅当问题像「库存多少」 |
| DRAFTING | LlmClient + 约束 JSON |
| WAIT_HUMAN | 不调模型 |

意图可用规则：含「多少」「现存量」→ 走工具节点。

---

## M2-D13 工作流测试要点

1. 普通教材题：不经工具，进入 WAIT_HUMAN  
2. 库存题：经工具假数据，再 WAIT_HUMAN  
3. REJECT 后状态正确  
4. 全程无线上写库  

---

## M2-D14 第 2 周复盘

能画状态图；能说明 HITL 为什么挡在写操作前；能指出 Agent 全自动的风险。

---

# 第 3 周｜评测自动化与观测

## M2-D15 评测集格式

建议 `src/test/resources/eval/rag-cases.jsonl` 或 markdown 表。JSONL 示例：

```json
{"id":"r1","question":"采购主链路有哪些单据？","expectDocs":["01-purchase-flow-sample.md"],"mustContain":["采购订单","到货单"],"forbid":[]}
{"id":"r2","question":"原料仓 A001 现在多少库存？","expectRefuseRealtime":true}
```

Chat 行为题另放 `chat-cases.jsonl`：高风险必须 `need_human=true` 等。

---

## M2-D16 评测运行器骨架

```java
public class EvalCase {
    public String id;
    public String question;
    public List<String> expectDocs;
    public List<String> mustContain;
    public boolean expectRefuseRealtime;
}

public class EvalReport {
    public int total;
    public int passed;
    public List<String> failures;
}
```

```java
public class RagEvalRunner {
    public EvalReport run(List<EvalCase> cases, RagService rag) {
        EvalReport report = new EvalReport();
        for (EvalCase c : cases) {
            var resp = rag.ask(/* wrap c.question */);
            boolean ok = true;
            if (c.expectDocs != null) {
                ok &= resp.getSources().stream().anyMatch(s -> c.expectDocs.contains(s.getDocId()));
            }
            if (c.mustContain != null) {
                String ans = resp.getReply().getAnswer();
                ok &= c.mustContain.stream().allMatch(ans::contains);
            }
            // ...
            report.total++;
            if (ok) report.passed++; else report.failures.add(c.id);
        }
        return report;
    }
}
```

可用 JUnit 调 runner，或 `main`/命令行模块。

---

## M2-D17 回归门禁意识

改动 Prompt / 切分 / retriever 后：

```text
跑 eval → 看 passed/total → 读 failures id → 再改
```

没有评测的「感觉变好」不可信。

---

## M2-D18 观测字段扩展

在现有 `AiCallLog` 思路上增加（日志即可）：

```text
retriever, recallK, topScore, flowId, promptVersion, toolNames
```

`promptVersion` 可用文件 hash：

```java
String promptVersion = DigestUtils.sha1Hex(systemPrompt).substring(0, 8);
```

---

## M2-D19 成本看板（学习级）

不必真仪表盘：写一个 `CostAggregator` 内存累计：

```java
public class CostAggregator {
    private final AtomicLong calls = new AtomicLong();
    private final DoubleAdder cost = new DoubleAdder();
    public void add(double usd) { calls.incrementAndGet(); cost.add(usd); }
    public String snapshot() { return "calls=" + calls.get() + ", costUsd=" + cost.sum(); }
}
```

`GET /api/ai/stats` 返回 snapshot（学习用）。

---

## M2-D20 A/B 对照实验记录法

固定表：

| 实验 | 变更 | 题集 | pass率 | 备注 |
|---|---|---|---|---|
| E1 | retriever=keyword→hybrid | rag-cases | | |

一次只改一个变量。

---

## M2-D21 第 3 周复盘

能跑通至少 5 条自动断言；能说明失败用例如何驱动修改；日志能回答「这次用了什么检索」。

---

# 第 4 周｜调试台、串联、收官

## M2-D22 简易调试台需求（可选前端）

最小页面一页即可（HTML+fetch 也行）：

- 输入问题  
- 选择模式：chat / rag / flow  
- 展示 answer、sources、traceId、usage  
- flow 模式展示 state + APPROVE/REJECT 按钮  

目的：降低 curl 摩擦，不是做产品 UI。

### 原生 JS 片段示例

```html
<!-- static/debug.html 思路 -->
<input id="q" />
<button onclick="ask()">RAG Ask</button>
<pre id="out"></pre>
<script>
async function ask() {
  const res = await fetch('/api/ai/rag/ask', {
    method: 'POST',
    headers: {'Content-Type':'application/json'},
    body: JSON.stringify({question: document.getElementById('q').value})
  });
  document.getElementById('out').textContent = JSON.stringify(await res.json(), null, 2);
}
</script>
```

Spring 可将 `classpath:/static/debug.html` 作为学习调试页。

---

## M2-D23 场景串联：手册问答

路径：`debug → /rag/ask → sources 人工看`  
验收：问主链路，sources 落在 01 文档。

---

## M2-D24 场景串联：实时库存（只读工具）

路径：规则命中 → tool → 再生成  
验收：假数据数量出现在答案；改库存请求被拒。

---

## M2-D25 场景串联：草稿 + HITL

路径：utterance → draft JSON → WAIT_HUMAN → APPROVE  
验收：APPROVE 不等于写库；日志有 decision。

---

## M2-D26 安全加固清单（对照打勾）

- [ ] 无写工具  
- [ ] 注入话术抽测仍守 JSON/拒答  
- [ ] Key 不在 Git  
- [ ] 日志无完整 Key、无过多 PII  
- [ ] EMPTY retrieval 不瞎编制度  

---

## M2-D27 性能与体验直觉

| 现象 | 方向 |
|---|---|
| RAG 很慢 | 资料块过大；topK 过大；同步 embed  
| 多轮很贵 | max-messages；摘要化历史（概念） |
| 向量建索引慢 | 批量 embeddings；缓存 |

学习期以「测得出、讲得清」为主，不为压测而压测。

---

## M2-D28 架构终稿（建议你自己画一版）

```text
                    ┌──────── debug.html ────────┐
                    ▼                            ▼
              REST Controllers              Flow Decide API
                    │                            │
        ┌───────────┼───────────┐                │
        ▼           ▼           ▼                ▼
      Chat        RAG         Draft         FlowEngine
        │           │           │                │
        └───── LlmClient ───────┴── ToolExecutor ┘
                    │
              Prompt files / rag-docs / eval cases / logs
```

---

## M2-D29 第 2 月口述自测

1. ChunkVectorStore 解决什么问题？  
2. RRF 融合的输入是什么？  
3. RetrievalGate 三种强度？  
4. Rerank 放在召回前还是后？  
5. Flow WAIT_HUMAN 的产品含义？  
6. APPROVE 为何仍不写库？  
7. eval JSONL 最少字段？  
8. 改 Prompt 后为何要跑评测？  
9. 调试台最小三块信息？  
10. 第 2 月相对第 1 月最大工程进步是什么？  

---

## M2-D30 收官与第 3 月方向

### 本月成果（对照）
- 检索：Hybrid +（可选）持久化向量 + Rerank + Gate  
- 交互：Flow + HITL  
- 质量：Eval runner + 观测字段  
- 体验：可选 debug 页  

### 仍明确不做
微调、公司权限落地、自动过账、无人看管多 Agent。

### 第 3 月可选主线（择一）
1. 真·工作流引擎（Temporal/自研）+ 审批落审计表  
2. Rerank 模型 / Hybrid 搜推生产化细节  
3. 评测平台化（报告、趋势、CI）  
4. 多模态单据理解（OCR）——仅当有兴趣  

---

# 附录 A｜推荐包结构（第 2 月结束时可接近）

```text
com.erp.ai
  chat/...
  rag/
    retrieve/  KeywordRetriever VectorRetriever HybridRetriever Reranker RetrievalGate
    store/     ChunkVectorStore InMemory... Pg...
    embed/     EmbeddingClient
  flow/        FlowEngine FlowInstance FlowState
  tool/        ToolRegistry ToolExecutor ...
  draft/       DraftService
  eval/        RagEvalRunner EvalCase
  web/         Controllers + static/debug.html
```

---

# 附录 B｜学习纪律（续）

1. 先讲义后编码；一次只引入一个新组件  
2. 每个新能力配至少 1 条自动或半自动验收  
3. sources / 工具结果 / 人工决策 三者不要互相冒充  
4. 本仓库永远先是训练场，再谈公司项目  

---

## 修订

| 日期 | 说明 |
|---|---|
| 2026-08-15 | 第 2 个月 Day1～30 合并讲义 + 对照代码骨架（单 MD） |
