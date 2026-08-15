# Web 前端轨道（30 天）

整月**逐日详版**教材（与第 1 月同级，一天一份完整讲义，非概述）：

→ [WEB_DAY1-30_COMBINED.md](./lessons/WEB_DAY1-30_COMBINED.md)

**技术栈：** **Vue 3** + **Vite** + `<script setup>` Composition API；推荐 **Vue Router**（面板路由）、**Pinia**（身份头 / session）；纯 CSS 或 scoped CSS + CSS 变量设计令牌（**不要求** UI 组件库）。开发期 Vite `server.proxy` → Spring Boot `http://localhost:8080`（`/api`）；生产学习路径：`vite build` 产物拷入 `erp-ai-assistant/src/main/resources/static/` 或独立预览。

**对接 API：** `/api/ai/chat`、`/api/ai/rag/ask`、`/api/ai/flow/*`、`/api/ai/eval/*`、`/api/ai/stats`、反馈接口；请求头 `X-User-Id` / `X-Roles` / `X-Tenant-Id`。

每天结构：为什么 → 概念加深 → 怎么做 → 代码骨架 → 坑与排障 → 当天验收。

**前置：** 建议已完成第 1 月 Chat/RAG 概念；可与第 2～5 月并行，但 API 以仓库学习接口为准。

**与第 4 月：** 独立 Web 轨道，用 Vue 端到端搭学习控制台；M4-D22～D26 控制台章节可作**可选**平行阅读。

约定：纯学习；通用 ERP AI 助手 UI；骨架在讲义 Markdown 中自行复制到独立目录 `erp-ai-console/`（**勿**静默改 `erp-ai-assistant/` 仓库内应用代码）；**不接公司生产**。
