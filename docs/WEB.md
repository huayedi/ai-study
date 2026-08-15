# Web 前端轨道

**完整教材（非 30 天合订）** — Vue 3 学习控制台连续章节教科书：

→ [WEB_VUE_COMPLETE.md](./lessons/WEB_VUE_COMPLETE.md)

**已生成工程代码（本仓库）：** [`erp-ai-console/`](../erp-ai-console/)  
启动：`cd erp-ai-console && npm install && npm run dev`（需后端 `localhost:8080`，Vite 已代理 `/api`）。

**技术栈：** Vue 3 + Vite + `<script setup>`；Vue Router + Pinia；CSS 变量设计令牌。  
开发：Vite `server.proxy` → Spring Boot `http://localhost:8080`（`/api`）。  
交付：`npm run build` 后可将 `dist/` 拷入 `erp-ai-assistant/src/main/resources/static/`。

**约定：** 纯学习；不接公司生产；APPROVE ≠ 写库。

旧「WEB-D1～30」逐日合订已退役：  
→ [WEB_DAY1-30_COMBINED.md](./lessons/WEB_DAY1-30_COMBINED.md)
