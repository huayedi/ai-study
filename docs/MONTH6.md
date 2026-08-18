# 第 6 个月

整月**逐日详版**教材（与第1月同级，一天一份完整讲义，非概述）：

→ [MONTH6_DAY1-30_COMBINED.md](./lessons/MONTH6_DAY1-30_COMBINED.md)

**学习库 MySQL：** [MYSQL.md](./MYSQL.md)（`154.8.183.10:3306/ai`，测试库非生产）

**口述标准答案：** [ORAL_ANSWERS.md](./ORAL_ANSWERS.md)（合订本口述节亦有就地答案）

**技术节点：** T10 Port/Adapter / 契约测试  
**技术前置：** 合订/完整教材内每个 Day（或第8月每章）开头有「此时应当学会 … 后再进行阅读」。可选 WireMock、Testcontainers。  
总图：[TECH_ROADMAP.md](./TECH_ROADMAP.md) · 入口：[TECH.md](./TECH.md)

**建议视频：** 合订本每个 Day（第8月每章）开头有「建议视频」行（含 B 站链接或搜索词）。  
总目录：[BILIBILI.md](./BILIBILI.md)（纪律：先讲义后视频；MONTH1～4 勿改 Spring AI 主栈）

每天结构：为什么 → 概念加深 → 怎么做 → 代码骨架 → 坑与排障 → 当天验收。  

主题：**真适配器接口稳定化**——业务代码只依赖稳定 Port/Adapter 接口；学习期用 Fake/Sandbox 实现；将来换真实 ERP 只换适配器，不改 Chat/RAG/Flow/WriteGateway 主编排。  

**前置：** 第1～5月（Chat/RAG、Hybrid/Gate/HITL、Store/eval/baseline、ACL/反馈/多租户/console、WriteGateway/假账本/受控写入）。  

约定：自行复制改造；**不接公司生产库/SSO/真实账套**；「真适配器」= 稳定契约 + 本地 Fake/Sandbox 假适配器 + 契约测试，**不是**接学员公司系统。
