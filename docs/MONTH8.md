# 第 8 个月

**完整教材（非 30 天合订）** — 工作流可视化连续章节教科书：

→ [MONTH8_WORKFLOW_VIZ_COMPLETE.md](./lessons/MONTH8_WORKFLOW_VIZ_COMPLETE.md)

**学习库 MySQL：** [MYSQL.md](./MYSQL.md)（`154.8.183.10:3306/ai`，测试库非生产）

**口述标准答案：** [ORAL_ANSWERS.md](./ORAL_ANSWERS.md)（第19章参考答法已展开为完整口述句）

**技术节点：** T12 Flow 可视化（需 T7 Vue）  
**技术前置：** 合订/完整教材内每个 Day（或第8月每章）开头有「此时应当学会 … 后再进行阅读」。章节开头均有技术前置。  
总图：[TECH_ROADMAP.md](./TECH_ROADMAP.md) · 入口：[TECH.md](./TECH.md)

**建议视频：** 合订本每个 Day（第8月每章）开头有「建议视频」行（含 B 站链接或搜索词）。  
总目录：[BILIBILI.md](./BILIBILI.md)（纪律：先讲义后视频；MONTH1～4 勿改 Spring AI 主栈）

**主题：** 工作流可视化 — 把 Flow 状态机画成可读图：节点、合法边、当前高亮、审计回放。可视化是理解与演示工具，不是放开 Agent 乱跳状态。APPROVE≠写库；写入仍经规则/Gateway。本月不接公司 BPM 平台。

**技术栈：** Java DTO/API 骨架（粘贴到 `erp-ai-assistant/`）+ Vue 3 SVG 图组件（粘贴到 `erp-ai-console/`）；与 Web 轨道共用 proxy 与身份头。

**前置：** 第1～7月（Chat/RAG、Flow HITL、Store/audit、ACL/多租户、WriteGateway/假账本、Port/Adapter、RuleEngine）+ [Web 控制台](./WEB.md)（`erp-ai-console/` 工程与 `api/http.js`）。

**约定：** 纯学习；骨架在 Markdown 中自行粘贴改造；**勿**直接改仓库内 `erp-ai-assistant/`、`erp-ai-console/` 应用源码；不接公司生产 BPM/流程平台。

**结构：** 20 章连续正文 + 附录 A–K（配置、API、布局/贝塞尔、curl、交叉索引等）；约 3400+ 行，无 30 天合订 filler。
