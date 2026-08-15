# ERP AI 学习控制台（Vue 3）

纯学习前端：对接 `erp-ai-assistant` 的学习期 REST。**不接公司生产、不自动过账。**

## 技术栈

- Vue 3 + Vite + `<script setup>`
- Vue Router + Pinia
- 开发代理：`/api` → `http://localhost:8080`

## 启动

```bash
# 终端 A：后端
cd erp-ai-assistant && mvn spring-boot:run

# 终端 B：前端
cd erp-ai-console && npm install && npm run dev
```

打开 http://localhost:5173

## 构建拷贝到 Spring static（可选）

```bash
npm run build
# 将 dist/* 拷到 erp-ai-assistant/src/main/resources/static/
```

## 学习请求头

由顶部身份条设置，经 `api/http.js` 自动带上：

- `X-User-Id`
- `X-Roles`
- `X-Tenant-Id`

## 说明

- Chat / RAG：对接仓库现有接口
- Flow / Feedback / Eval / Stats：前端已实现；若后端尚未提供对应 REST，面板会显示友好错误（可后续按第 2～4 月教材补齐后端）
- APPROVE ≠ 写库：仅表示接受助手建议
