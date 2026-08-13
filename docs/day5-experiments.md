# Day5 实验记录表

> 主题：多轮会话与历史裁剪、问题清单补齐、第 1 周收口  
> 对照代码：`SessionStore`、`ChatService#buildMessages`、`application.yml` → `ai.session.max-messages`

---

## 实验 1：多轮连贯（带本系统术语）— 已完成

结论：
- 会根据上文回答  
- 正确使用本系统术语  
- `totalTokens` 持续拉高（历史重发导致输入变大）

---

## 实验 2：历史裁剪（max-messages）— 已完成

操作：临时改为 `4` 后观察。

结论：
- `max-messages=4` 时，会话里大约只保留最近折合 **4 条消息**量级的历史；更早的对答会被丢掉，再追问早期细节会“遗忘”  
- 裁剪从**队头（最旧）**删除消息  
- 目的是控制**后续请求的输入 token / 上下文长度**  
- 实验后应恢复 `max-messages=20`

订正理解（便于复习）：
- 每轮成功对话通常写入 2 条（user + assistant）  
- `max-messages=4` ≈ 大约保留最近 **2 轮**完整对答（外加每次请求仍会带 system）  
- system 提示每次单独组装，一般不占 SessionStore 配额

---

## 实验 3：问题清单补齐到 20 — 已完成

文件：`erp-ai-assistant/samples/week1-questions.md`  
完成数：**20 / 20**（已按本系统术语生成，可按现场再改）

---

## 实验 4：第 1～2 周自检 — 已完成

- ✅ 能画出 messages 组装顺序  
- ✅ 知道成功才写入 SessionStore  
- ✅ 知道 401 不应盲重试  
- ✅ 本系统采购主链路能默写  
- ✅ temperature 更倾向 0.2～0.3 的原因能说清  

---

## Day5 复盘（3 行）

- 搞懂了：多轮靠重发历史；历史越长越贵；`max-messages` 从队头裁剪控成本  
- 和预期不符：（无则写无）  
- 下周想开始：第 3 周 Prompt 深化（术语解释器 / 少样本），并可用 20 题做对比集  
