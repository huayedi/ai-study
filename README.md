# ai-study

Java 5 年 ERP 工程师转 AI 应用（自学提升）仓库。

## 内容

| 路径 | 说明 |
|---|---|
| [docs/LEARNING_PLAN.md](docs/LEARNING_PLAN.md) | 20 周详细学习计划（每天 2–3 小时） |
| [erp-ai-assistant/](erp-ai-assistant/) | 可运行 Spring Boot 项目（第 1–2 周起点） |

## 立刻开始

```bash
cd erp-ai-assistant
mvn spring-boot:run
```

另开终端：

```bash
curl -s http://localhost:8080/api/ai/chat \
  -H 'Content-Type: application/json' \
  -d '{"message":"我想做一笔采购，需要填哪些字段？"}'
```

默认 `mock` 模式，无需 API Key。完整说明见 `erp-ai-assistant/README.md`。
