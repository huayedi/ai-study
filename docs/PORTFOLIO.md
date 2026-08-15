# 作品集 PORTFOLIO（学习仓模板）

> 用途：面试/自评口述底稿；与代码、eval 证据对齐。  
> 纪律：**明确不做**必须始终可见；第5月起写路径只写**假账本**，不接公司库。  
> 连贯说明：[lessons/CURRICULUM_CONTINUITY.md](./lessons/CURRICULUM_CONTINUITY.md)

---

## Elevator pitch（三句，随月更新）

这是一个学习用 ERP AI 助手：教材 RAG + Gate + 带审计的 HITL；第4月起角色/租户隔离与反馈进评测；第5月起 APPROVE 后经 WriteGateway 写**内存假账**；第6～7月 Port/规则外置。  
它明确不做：公司 SSO、生产过账、模型无审批直写、接公司规则/BPM 平台。  
我负责的核心模块是：________。

---

## 明确不做（全局）

- 不接公司生产库 / VPN / 真实账套 / SSO  
- 模型永不持有「无审批写库存/过账」工具  
- 反馈不自动改 prompt / 模型上线  
- 前端不发明魔法写库接口  
- OCR 结果不直接驱动 ERP 写库  

---

## 第1～2月：会生成、会检索、会拦

**问题：**  
**方案：** Chat + Prompt + RAG；Hybrid/Gate；最小 HITL。  
**证据：**（curl / 截图 / eval runId）  
**边界：** APPROVE 当时不写库。  
**贡献：**  

---

## 第3月：可追溯、可回归

**问题：**  
**方案：** Store / reindex / audit / baseline。  
**证据：**  
**边界：** 无写库存 Tool。  
**贡献：**  

---

## 第4月：隔离、反馈、可演示

**问题：**  
**方案：** ACL + tenant + feedback→题集 + `console.html`。  
**证据：** acl-forbidden / tenant-isolation。  
**边界：** 无 SSO；APPROVE 仍不写库（至第4月末）。  
**贡献：**  

---

## 第5月：受控写入

**问题：**  
**方案：** WriteGateway + 假账本 + 幂等 + WRITE 审计；APPROVE → APPLY_WRITE。  
**证据：** write-safety；串测关期间/越权。  
**边界：** 假账 ≠ 公司库；Chat 禁写。  
**贡献：**  

---

## 第6月：适配器稳定化

**问题：**  
**方案：** Port/Adapter；Fake/Sandbox；契约测。  
**证据：** adapter-contract；切换演练。  
**边界：** 不接公司生产 Adapter。  
**贡献：**  

---

## 第7月：规则引擎

**问题：**  
**方案：** RuleEngine + DecisionTable；classify/risk/write-precheck；rulesVersion。  
**证据：** rules-cases；冲突实验（规则胜）。  
**边界：** 规则不直写 Port；不接公司规则平台。  
**贡献：**  

---

## 第8月 / Web（可选填）

**工作流可视化 / Vue 控制台：** 图示状态机；proxy + 身份头；APPROVE≠生产过账。  
**证据：**  

---

## 架构终图（可贴 ASCII）

```text
L1 Chat/RAG → L2 Gate/HITL → L3 Store/Audit → L4 ACL/Tenant
 → L5 WriteGateway/假账 → L6 Port/Adapter → L7 RuleEngine
 → L8 Flow 图（可选）∥ Vue / static console
```

---

## 回归证据速查

| 套件 | runId / 日期 | 结果 |
|---|---|---|
| baseline |  |  |
| write-safety |  |  |
| adapter-contract |  |  |
| rules-cases |  |  |
