# 第 10 课讲义：切分策略深入（Chunking）

> **定位：** 纯学习；通用 ERP 教材；不接公司系统。  
> **本课约定：只改文档理解，不由助教直接改你的业务代码。**  
> 文末「自行对照调整指南」供你自愿动手；不动手只读懂也可以进入 Day11。  
> **前置：** Day9 RAG 最小闭环。  
> **现有对照代码（只读）：** `HeadingChunker`、`TextChunk`、`RagCorpusIndex`、`KeywordRetriever`

---

## 一、为什么切分要单独学

RAG 检索的基本单位是 **chunk（文本块）**：

```text
文档 → 切分 → TextChunk 列表 → 检索 TopK → 进入提示词 → 生成
```

同一份教材，切法不同，结果不同：

| 现象 | 常见切分原因 |
|---|---|
| 问「主链路」却带回整章杂讯 | 过粗：一块里多个主题 |
| 答案缺半句、sources 很碎 | 过细：或窗口切断关键句 |
| 只能引用文件名、说不清章节 | 元数据弱：缺少可用的 `section` |
| 提示词突然很长、很贵 | 块太大或 TopK 块过多 |

**检索效果的上限，很多在切分阶段就定了。**

---

## 二、三种基础策略（先建立直觉）

### 2.1 按标题切（Heading）——当前仓库正在用的思路

适合结构清晰的 Markdown 手册（本仓库 `rag-docs` 就是）。

```markdown
## 1. 主链路
……完整主题……

## 2. 采购订单常见字段
……另一主题……
```

每个标题下的正文成为一块；若单节过长，再按字符上限二次切开。

**优点：** 一块≈一个业务小节；章节名适合做引用。  
**缺点：** 依赖标题质量；某一节写成超长文时仍可能偏粗。

**对照现有实现：** `HeadingChunker`
- `splitByHeadings`：遇到 `##` / `###` 开新块  
- `splitLong`：超过 `MAX_CHARS`（当前代码里是 800）再切  
- 产出 `TextChunk(id, docId, section, content)`

### 2.2 固定窗切（Fixed window）

无视标题，按固定字符数切：`[0,N) [N,2N) …`

**优点：** 简单，对无标题长文也适用，块大小可控。  
**缺点：** 易切断句子；`section` 往往只能叫 `window-0`，引用可读性差。

### 2.3 重叠窗切（Overlap window）

固定窗 + 相邻块重叠，例如窗 500、重叠 100：

`[0,500) [400,900) [800,1300) …`

**优点：** 降低「关键句卡在边界」的概率。  
**缺点：** 内容重复进索引；重叠太大易近重复命中。

---

## 三、用本仓库三份教材想清楚「该怎么切」

| 文件 | 特征 | 更合适的默认 |
|---|---|---|
| `01-purchase-flow-sample.md` | 标题清楚（主链路/字段/问题） | **heading** |
| `02-inventory-exceptions.md` | 分节清楚 | heading |
| `03-period-approval.md` | 期间/凭证/审批分节 | heading |

结论（学习期够用）：

1. 有清晰标题 → 先 heading（你现在的代码方向是对的）  
2. 无结构长文 → 再考虑 fixed / overlap  
3. 先保证「一块一个主题」，再调大小  

**过细想象：** 窗只有几十个字，主链路箭头被切断，关键词分变碎。  
**过粗想象：** 整份采购说明糊成一块，问「必填字段」也拖进主链路与暂估大段噪声。

---

## 四、元数据与引用（工程纪律）

`TextChunk` 当前字段：

| 字段 | 作用 |
|---|---|
| `id` | 块 ID |
| `docId` | 文件名 |
| `section` | 章节名（heading 策略的价值主要在这） |
| `content` | 正文 |

`/api/ai/rag/ask` 的 `sources` 应来自检索命中的 chunk，**不要让模型杜撰书名章节当系统依据**。

阅读顺序建议：

1. `TextChunk`  
2. `HeadingChunker#chunk`  
3. `RagCorpusIndex#rebuild`（启动时对所有教材调用切分）  
4. `KeywordRetriever` / `RagService`（看块如何被检索、如何进 `sources`）

---

## 五、切分 × 检索 × 生成

```text
切分  → 决定池子里每块的粒度与元数据
检索  → 只是从池子里挑 TopK
生成  → 只看见进提示词的那几块
```

调效果的推荐顺序：

1. 先看返回的 `sources` 是否合理（切分/检索）  
2. 再改 RAG 提示词（生成）  
3. 最后才考虑换更大模型  

---

## 六、自行对照调整指南（可选，你来改代码）

> 助教**不会**直接改你的仓库实现。若你想自己加深，可按下面最小步骤做。  
> 改完需重启，因为索引在 `RagCorpusIndex` 启动时构建。

### 练习 A：只调 heading 的二次切分长度

文件：`HeadingChunker.java`  
把 `MAX_CHARS = 800` 改成例如 `300` 或 `1200`，重启后问同一句：

```bash
curl -s http://localhost:8080/api/ai/rag/ask \
  -H 'Content-Type: application/json' \
  -d '{"question":"采购主链路有哪些单据？"}'
```

观察：`sources` 变碎还是变整、`section` 是否出现 `(1/2)` 这类后缀。

### 练习 B：自己增加 fixed 策略（扩展题）

思路（不必一次完美）：

1. 新写一个类（例如 `WindowChunker`），按 `chunkSize` 循环 `substring`  
2. `section` 可用 `window-0`、`window-1`…  
3. 在 `RagCorpusIndex` 里暂时改成调用你的新类，或加一个简单 `if` 配置分支  
4. 用同一问题对比 heading vs fixed 的 `sources`

重叠窗：在 fixed 基础上让 `start` 每次只前进 `chunkSize - overlap`（注意 `overlap < chunkSize`）。

### 练习 C：把魔法数挪到配置（扩展题）

在 `application.yml` 增加例如：

```yaml
ai:
  rag:
    max-chunk-chars: 800
```

并在 `AiProperties.Rag` 增加对应字段，让 `HeadingChunker` 读配置——这是工程习惯，不是 RAG 必选项。

### 不建议本课做的事

- 先上向量库（留给 Day11～12 讲义）  
- 为大改切分去改公司项目  
- 为了“分数好看”同时改提示词、TopK、模型，导致无法归因  

---

## 七、读完应形成的理解

1. 能口述 heading / fixed / overlap 的差异与适用场景  
2. 能解释过粗、过细分别会导致什么  
3. 明白 `docId` + `section` 对引用的意义  
4. 知道调 RAG 应先看 `sources`  
5. （可选）能自己改 `MAX_CHARS` 并解释现象  

---

## 八、下一课预告（Day11）

**Embedding 与向量检索原理**（仍以讲义为主；代码是否动、怎么动，按你的节奏，默认先讲清原理）。

---

## 修订

| 日期 | 说明 |
|---|---|
| 2026-08-15 | Day10 讲义；明确不直接改业务代码，仅提供自行对照调整指南 |
