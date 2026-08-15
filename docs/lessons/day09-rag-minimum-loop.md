# 第 9 课讲义：RAG 最小闭环（学习版）

> **定位：** 纯学习，通用 ERP 教材，不接公司系统。  
> **读法：** 先读本讲义建立心智模型，再对照仓库代码阅读。  
> **实现策略：** 先打通「文档→切分→检索→带资料生成」，检索先用**关键词**，下节再升级向量。

---

## 一、今天要建成什么

一条最小 RAG 链路：

```text
samples/rag-docs（通用 ERP 教材 Markdown）
        ↓ 启动时加载
      切分成 TextChunk
        ↓
   内存索引（学习版）
        ↓ 用户提问
   关键词检索 TopK
        ↓
  资料片段 + 问题 → LLM
        ↓
  回答 + 引用来源（哪个文件/哪一节）
```

新增接口（学习用）：

```http
POST /api/ai/rag/ask
{"question":"采购主链路有哪些单据？"}
```

与已有 `/api/ai/chat` 的分工：

| 接口 | 用途 |
|---|---|
| `/api/ai/chat` | 行为约束下的通用对话（Prompt / few-shot） |
| `/api/ai/rag/ask` | **必须先检索教材**再回答，并返回 sources |

---

## 二、为什么第一版用关键词检索

向量检索（Embedding）更懂“同义改写”，但会多引入：

- Embedding API / 本地模型  
- 向量库（PGVector 等）  
- 距离、维度等概念  

学习第一课更重要的是把**数据流**跑通。关键词重叠打分足够让你看清：

- chunk 从哪来  
- 为什么切分影响检索  
- 为什么要把资料塞进提示词  
- 引用字段如何返回给调用方  

下节课再把 `KeywordRetriever` 换成向量检索，**上层 `RagService` 几乎不用改**——这正是接口隔离的价值。

---

## 三、模块心智模型（对照包名）

```text
com.erp.ai.rag
├── TextChunk              一切分后的文本块（含 docId、标题、正文）
├── DocumentCorpusLoader   从 classpath:rag-docs 读 Markdown
├── HeadingChunker         按 ## / ### 切分，过长再按长度切开
├── KeywordRetriever       问句分词与 chunk 重叠打分，取 TopK
├── RagPromptBuilder       把资料格式化进 system/user
├── RagService             编排：检索 → 拼提示 → 调 LlmClient → 组装引用
└── RagController          POST /api/ai/rag/ask
```

已有组件复用：

- `LlmClient`：仍然负责调模型  
- `ReplyParser`：仍然要求 JSON 输出  
- **不复用** `SessionStore`（本节 RAG 默认单轮，先把主链路学清）

---

## 四、切分（Chunking）在教什么

教材是 Markdown，学习版按标题切：

```markdown
## 1. 主链路
段落...

## 2. 常见字段
段落...
```

每个 `##` 下的内容变成一个 `TextChunk`，并记录：

- `docId`：文件名，如 `01-purchase-flow-sample.md`  
- `section`：标题文本  
- `content`：正文  

**切分过粗：** 一块里主题混杂，检索命中但噪声大，费 token。  
**切分过细：** 缺上下文，模型难以完整作答。

这就是为什么工程上要调 chunk 大小——不是玄学，是检索精度与上下文成本的权衡。

---

## 五、检索（Retrieval）在教什么

学习版步骤：

1. 把问题拆成词（中文按非汉字/字母数字边界做极简切分即可）  
2. 对每个 chunk 计算重叠词数量（可再除以长度做归一）  
3. 排序取 TopK（配置项 `ai.rag.top-k`，默认 3）

命中差时常见原因：

- 问题用词与教材用词不一致（同义改写）→ 向量检索更擅长  
- 切分把答案句子切断  
- TopK 太小  

返回给前端的 `sources` **以检索结果为准**（文件、章节、摘要、分数），不要让模型随意编造引用。

---

## 六、生成（Generation）在教什么

检索到的资料进入提示词，核心约束应是：

1. 优先根据资料回答  
2. 资料不足要明确说不够，不要脑补成公司制度  
3. 保持 JSON 输出（与 Chat 一致，降低解析成本）

注意：RAG **不能取消** Prompt 里的安全边界——  
即使资料里写了操作步骤，也仍然不能替用户执行过账、改库存。

---

## 七、代码阅读顺序（建议）

1. `HeadingChunker`、`TextChunk` — 看数据长什么样  
2. `DocumentCorpusLoader` — 启动时如何建库  
3. `KeywordRetriever` — 分数怎么来  
4. `RagPromptBuilder` — 资料如何进提示词  
5. `RagService` + `RagController` — 一次请求的完整编排  

本地验证（真实模型或 mock 均可）：

```bash
curl -s http://localhost:8080/api/ai/rag/ask \
  -H 'Content-Type: application/json' \
  -d '{"question":"采购主链路有哪些单据？"}'
```

重点看响应里的 `sources`：是否指向 `01-purchase-flow-sample.md` 等教材文件。

另可对比：

```bash
curl -s http://localhost:8080/api/ai/chat \
  -H 'Content-Type: application/json' \
  -d '{"message":"采购主链路有哪些单据？"}'
```

Chat 可能也答得像；RAG 的差异是**显式依据检索片段**，这是工程可观测性，不是玄学更聪明。

---

## 八、本课边界（刻意不做）

- 不做 PGVector / Embedding API（下节）  
- 不接公司文档库、权限、登录  
- 不做多轮 RAG 会话  
- 不自动写入业务单据  

---

## 九、读完应形成的理解

1. RAG = 检索 + 生成；第一版可用关键词打通。  
2. Chunk 的元数据（文档名/章节）是引用的来源。  
3. `sources` 应由检索器给出，而不是模型杜撰。  
4. `/chat` 与 `/rag/ask` 职责分离，便于以后替换检索实现。  

---

## 十、下一课预告

**向量检索版 RAG：** Embedding、相似度、与关键词的对比实验（仍用同一套教材文档）。

---

## 修订

| 日期 | 说明 |
|---|---|
| 2026-08-15 | Day9：关键词检索最小 RAG 讲义 + 学习版实现 |
