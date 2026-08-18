# ERP AI Assistant

面向 ERP Java 工程师的 AI 应用学习项目（第 1–2 周：最小 LLM 闭环）。

- 默认 `mock` 模式，无需 API Key 即可运行
- `/api/ai/chat`：多轮会话、强制 JSON、校验失败重试、trace/token/成本日志
- 可切换 OpenAI 兼容接口（含国产兼容网关）

完整 20 周计划见：[docs/LEARNING_PLAN.md](../docs/LEARNING_PLAN.md)

## 项目结构

```text
erp-ai-assistant/
├── src/main/java/com/erp/ai/
│   ├── client/           # LLM 客户端（mock / openai-compatible）
│   ├── config/           # 配置
│   ├── controller/       # HTTP API
│   ├── model/            # 领域模型与 DTO
│   ├── observability/    # 调用日志
│   ├── prompt/           # 系统提示词加载
│   ├── rag/              # 学习版 RAG（keyword / vector / hybrid）
│   ├── tool/             # Day16 只读工具白名单 + 执行器（假数据）
│   └── service/          # 会话、解析、编排
├── src/main/resources/
│   ├── application.yml
│   └── prompts/erp-system-prompt.txt
└── src/test/java/        # 单元测试 + 接口测试
```

`tool/` 白名单：`queryItem` / `queryInventory` / `queryPeriodStatus`；**无写库存工具**。

## IDEA 导入

- 可打开上级仓库根目录（根 `pom.xml` 会聚合本模块），或直接打开本目录
- 若没有 Maven 窗口：右键本目录 `pom.xml` → **Add as Maven Project**
- 详细排查见仓库根 [`README.md`](../README.md) 的「IDEA 导入」一节

## 快速开始

### 1. 环境

- JDK 21+
- Maven 3.8+

### 2. 启动（mock，推荐先跑通）

```bash
cd erp-ai-assistant
mvn spring-boot:run
```

### 3. 调用接口

```bash
curl -s http://localhost:8080/api/ai/chat \
  -H 'Content-Type: application/json' \
  -d '{"message":"我想做一笔采购，需要填哪些字段？"}' | jq
```

多轮对话带上返回的 `sessionId`：

```bash
curl -s http://localhost:8080/api/ai/chat \
  -H 'Content-Type: application/json' \
  -d '{"sessionId":"<上一次的sessionId>","message":"如果供应商还没建档怎么办？"}' | jq
```

### 4. 切换真实模型

```bash
export AI_PROVIDER=openai-compatible
export AI_API_KEY=sk-xxx
export AI_BASE_URL=https://api.openai.com/v1   # 或其他兼容地址
export AI_MODEL=gpt-4o-mini

mvn spring-boot:run
```

## 响应示例

```json
{
  "traceId": "...",
  "sessionId": "...",
  "provider": "mock",
  "model": "mock-erp-assistant",
  "reply": {
    "answer": "若要发起采购，建议先建采购申请/采购订单草稿...",
    "needHuman": true,
    "suggestedDocType": "采购订单",
    "requiredFields": ["供应商", "物料编码", "数量", "含税单价", "交货日期"],
    "confidence": 0.72
  },
  "usage": {
    "promptTokens": 42,
    "completionTokens": 80,
    "totalTokens": 122,
    "estimatedCostUsd": 0.0
  },
  "latencyMs": 3,
  "attempts": 1
}
```

## 测试

```bash
mvn test
```

## 提示词与术语表

| 文件 | 说明 |
|---|---|
| `src/main/resources/prompts/erp-system-prompt.txt` | 实际发给模型的 system 提示（改完需重启） |
| `src/main/resources/prompts/erp-glossary.md` | 完整术语表（给人看/维护；精简版已写入 system 提示） |
| `src/main/resources/prompts/erp-few-shot.txt` | 少样本示范（启动时追加到 system；Day6+） |
| `src/main/resources/rag-docs/` | 通用 ERP 教材（RAG 索引来源） |
| `POST /api/ai/rag/ask` | 学习版 RAG：关键词检索 + 带 sources 回答 |
| `GET /api/ai/tool/list` | Day16：列出只读工具定义 |
| `POST /api/ai/tool/invoke` | Day16：执行白名单工具（假数据；禁写） |

手测库存示例：

```bash
curl -s http://localhost:8080/api/ai/tool/invoke \
  -H 'Content-Type: application/json' \
  -d '{"toolName":"queryInventory","args":{"itemCode":"ITEM-A001","warehouse":"原料仓"}}' | jq
```

## 第 1 周建议练习

1. 用 mock 跑通并读懂 `ChatService` 重试逻辑
2. 修改 `prompts/erp-system-prompt.txt`，观察回答变化
3. 收集 20 个真实 ERP 常问问题，放入 `samples/week1-questions.md`
4. 有 Key 后再切真实模型对比效果
