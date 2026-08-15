> **第1月合订详版（推荐主入口）：** [`MONTH1_DAY1-30_COMBINED.md`](./MONTH1_DAY1-30_COMBINED.md) · [`docs/MONTH1.md`](../MONTH1.md)
>
> 本文件保留为单日备份；内容已全文收录进合订本对应 Day。

# 第 12 课讲义：向量检索学习版实现（对照编码）

> **定位：** 纯学习；通用 ERP 教材；不接公司系统。  
> **本课约定：讲义把「怎么实现」写清楚，业务代码由你自己改；助教不直接改你的仓库实现。**  
> **前置：** Day11（Embedding / 相似度 / 索引与查询分离）已读完。  
> **目标：** 在现有关键词 RAG 旁，增加可切换的向量检索，主链路仍是「检索 → 拼资料 → LLM → sources」。

---

## 一、今天要达成的架构形态

```text
                    ┌─────────────────────┐
                    │   RagService.ask    │
                    └──────────┬──────────┘
                               │ retrieve(question, topK)
               ┌───────────────┴───────────────┐
               ▼                               ▼
     KeywordRetriever                 VectorRetriever
     （Day9 已有）                    （本课你来加）
                                               │
                                               ▼
                                        EmbeddingClient
                                        （/embeddings）
```

配置切换（推荐命名，可按你习惯微调）：

```yaml
ai:
  rag:
    retriever: keyword   # 或 vector
    top-k: 3
    # 向量相关（示例）
    embedding-model: text-embedding-3-small   # 以你的服务商文档为准
    # base-url / api-key 可复用 ai.base-url、ai.api-key，或单独拆 embedding-* 
```

原则：

1. **不要删掉** `KeywordRetriever`——并列存在，便于对比  
2. **不要把** HTTP Embedding、相似度计算、提示词拼装全塞进 `RagService`  
3. `sources` 仍然只来自检索命中的 `TextChunk` 元数据  

---

## 二、推荐新增的类型（命名供参考）

### 2.1 检索抽象（强烈建议）

```text
interface RagRetriever {
  List<RetrievedChunk> retrieve(String question, List<TextChunk> corpus, int topK);
}
```

然后：

- `KeywordRetriever implements RagRetriever`（把现有类改成实现接口，或包一层）  
- `VectorRetriever implements RagRetriever`  

`RagService` 只依赖 `RagRetriever`（或按配置注入其中一个 Bean）。

### 2.2 Embedding 客户端

```text
interface EmbeddingClient {
  float[] embed(String text);
  // 可选：List<float[]> embedBatch(List<String> texts);
}
```

学习版实现类示例名：`OpenAiCompatibleEmbeddingClient`  
- URL：`{baseUrl}/embeddings`（与 chat 一样先规范化尾部斜杠）  
- Body 概念：`{"model":"...","input":"..."}` 或 `input: ["a","b"]`  
- 响应概念：`data[i].embedding` 为 `number[]`  

**注意：** Embedding 的 `model` 通常 **不是** chat 的 `deepseek-v4-pro` 之类对话模型名；以服务商「Embeddings」文档为准。若你当前厂商暂时没有 embeddings 接口，本课可先：

- 写好接口与 `VectorRetriever` 结构，Embedding 用本地伪实现（见第六节），或  
- 换一个提供兼容 embeddings 的学习用服务  

两种都算完成本课「结构落地」。

### 2.3 带向量的索引条目

```text
class IndexedChunk {
  TextChunk chunk;
  float[] vector;
}
```

启动时（`RagCorpusIndex` 或新建 `VectorCorpusIndex`）：

```text
docs → chunker → List<TextChunk>
                → 逐个 embed → List<IndexedChunk>
```

查询时只对 question embed 一次，再与所有 `IndexedChunk.vector` 比相似度。

---

## 三、余弦相似度（实现要点）

对向量 `a`、`b`：

```text
cos = dot(a,b) / (||a|| * ||b||)
```

实现时注意：

1. 长度必须一致，否则直接报错（模型或解析错了）  
2. 防止除零（零向量）  
3. TopK：按 cos **降序**；可先过滤 `cos <= 0`（学习期可选）  
4. 与 Day9 一样返回 `RetrievedChunk(chunk, score)`，这里的 score 用余弦即可  

伪代码：

```text
q = embed(question)
for each indexed:
  score = cosine(q, indexed.vector)
收集 → 排序 → topK
```

---

## 四、建议改动的现有类（对照清单）

| 现有类 | 建议你怎么动 |
|---|---|
| `AiProperties.Rag` | 增加 `retriever`、`embeddingModel` 等字段 |
| `application.yml` | 增加上述配置；**Key 仍用环境变量** |
| `KeywordRetriever` | 实现 `RagRetriever`（方法签名可保持） |
| `RagCorpusIndex` | 若 `retriever=vector`，建索引时一并 embed；或拆出向量索引组件 |
| `RagService` | 注入 `RagRetriever`（按配置选实现），其余生成逻辑不动 |
| `RagPromptBuilder` | 一般 **不用改** |

Bean 注入学习期简单做法：

- `@Bean` / `@ConditionalOnProperty` 按 `ai.rag.retriever` 注册一个 `RagRetriever`，或  
- 两个都注册，在 `RagService` 构造里 `if (vector) ... else ...`  

能跑、能切换即可，不追求企业级装配花样。

---

## 五、索引重建时机

向量与切分绑定：

- 你改了 `HeadingChunker` / `MAX_CHARS` → **必须重建向量索引**（重启或提供 rebuild 方法）  
- 只改 `top-k` → 不必重 embed  
- 换 embedding 模型 → 必须全量重 embed  

学习期：启动时全量 embed 三份小教材完全可接受。文档上千份时才需要持久化向量库——那是后话。

---

## 六、没有 Embeddings API 时的学习替身（可选）

若暂时调不通官方 embeddings，可用 **BagEmbeddingClient** 完成结构练习（语义会很弱，仅练链路）：

思路示例（任选一种你能写的）：

- 用词袋/哈希把字符映射到固定维稀疏向量再归一化  
- 或对 tokenize 后的词做简单 hashing trick  

目的：让 `VectorRetriever` + 配置切换先跑通；等有真 embeddings 再替换 Client。  
真假 Client 都应实现同一 `EmbeddingClient` 接口。

---

## 七、自测观察（不是交作业）

同一批问题，分别切 `keyword` / `vector` 看 `sources`：

1. `采购主链路有哪些单据？`（字面题，关键词往往已很好）  
2. `下完采购单以后货到了怎么处理？`（口语题，向量更可能拉开差距）  
3. `账期关了还能不能过账？`（同义改写）  

观察维度：

- Top1 的 `docId/section` 是否合理  
- score 数值是否「相对可排序」（绝对值因模型而异）  
- 切换 retriever 后，后半段 JSON 回答链路是否仍稳定  

---

## 八、常见失败与排查

| 现象 | 可能原因 |
|---|---|
| 401/404 on /embeddings | Key、baseUrl、路径是否少了或多了 `/v1` |
| 空向量 / 解析失败 | 响应 JSON 路径不是 `data[].embedding` |
| 所有分数差不多 | 模型不对、文本过短、或伪 embedding 区分力差 |
| 改切分后结果怪异 | 忘了重建向量索引 |
| sources 对但回答跑飞 | 回查 `RagPromptBuilder` 约束，不是检索器问题 |

---

## 九、本课边界

- 不上 PGVector / Milvus（进程内 List 足够学习）  
- 不接公司文档与权限  
- 不要求 Hybrid（Day13）  
- 助教不直接提交你的 Java 改动  

---

## 十、读完 / 做完应形成的理解

1. 能画出 `EmbeddingClient` + `VectorRetriever` + 配置切换  
2. 知道索引阶段与查询阶段各 embed 什么  
3. 能用余弦实现 TopK  
4. 能解释为何保留 Keyword 做对比  

---

## 十一、下一课预告（Day13）

**混合检索与未命中行为**：keyword + vector 如何合并；低分/空命中时如何拒答；为何 sources 仍不能让模型编。

---

## 修订

| 日期 | 说明 |
|---|---|
| 2026-08-15 | Day12 向量检索实现讲义；对照编码，不直接改学员代码 |
