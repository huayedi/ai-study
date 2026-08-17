# 第 5 个月

整月**逐日详版**教材（与第1月同级，一天一份完整讲义，非概述）：

→ [MONTH5_DAY1-30_COMBINED.md](./lessons/MONTH5_DAY1-30_COMBINED.md)

**口述标准答案：** [ORAL_ANSWERS.md](./ORAL_ANSWERS.md)（合订本口述节亦有就地答案）

**技术节点：** T9 受控写入 / WriteGateway / 假账本  
**技术前置：** 合订/完整教材内每个 Day（或第8月每章）开头有「此时应当学会 … 后再进行阅读」。完成后进入 Spring AI（T13）对照窗口。  
总图：[TECH_ROADMAP.md](./TECH_ROADMAP.md) · 入口：[TECH.md](./TECH.md)

**建议视频：** 合订本每个 Day（第8月每章）开头有「建议视频」行（含 B 站链接或搜索词）。  
总目录：[BILIBILI.md](./BILIBILI.md)（纪律：先讲义后视频；MONTH1～4 勿改 Spring AI 主栈）

每天结构：为什么 → 概念加深 → 怎么做 → 代码骨架 → 坑与排障 → 当天验收。  

主题：**受控写入与模拟过账**——假账本 + 强制 HITL + 审计；模型永不直接持有无审批写库存/过账工具。  

**前置：** 第1～4月（Chat/RAG、Hybrid/Gate/HITL、Store/eval/baseline、ACL/反馈/多租户/console）。  

约定：自行复制改造；**不接公司生产库/SSO**；学习仓仅用内存假账本；写入只发生在 APPROVE 之后且经 WriteGateway。
