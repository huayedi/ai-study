
# 第 7 个月合并讲义（Day1～Day30）【逐日详版 · 与第1月同级 · 非概述】

> **定位：** 纯学习；通用 ERP 教材口径；**不接公司生产库、不接 SSO、不直连公司生产规则平台。**
> **形式：** 与第 1 月 **同等详细**；本文件为 30 天正文合订，**不是缩写版大纲。** 可复制代码均在本文；由你自行粘贴改造到 `erp-ai-assistant`。
> **前置：** 第1月 Chat/Prompt/RAG + 第2月 Hybrid/Gate/Rerank、Flow HITL、Eval 入门 + 第3月 Store/reindex/audit/baseline + 第4月 ACL/反馈/多租户/console + 第5月 WriteGateway/假账本/受控写入 + 第6月 Port/Adapter/Fake/Sandbox/契约测试。
> **每天结构（固定六段）：** 为什么 → 概念加深 → 怎么做 → 代码骨架 → 坑与排障 → 当天验收。
> **入口：** `docs/MONTH7.md`
> **本月主题：** **规则引擎（RuleEngine / RuleSet / DecisionTable / 版本化 / 评测 / 审计）**
> **核心产品句（全文反复强调）：**
> **确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。**

---

## 第 7 月总目标（学完应能对外讲 8～10 分钟）

1. **边界：** 确定性业务规则走 RuleEngine；LLM 只做理解、草稿与解释；冲突时规则优先。
2. **核心类型：** `Rule` / `RuleContext` / `RuleResult`；`ALLOW` / `DENY` / `WARN` / `ROUTE`。
3. **执行：** `RuleEngine.evaluate(context)` — 命名空间、优先级、短路（DENY/ROUTE 可配置）。
4. **决策表：** 期间关闭 DENY、缺仓库 WARN 等表驱动规则。
5. **接入：** CLASSIFY、RISK_CHECK、写前校验（WriteGateway 前 RuleSet）、Gate 辅助。
6. **装载：** YAML/JSON 学习版 + Java Rule 混用；`RuleRegistry` 命名空间。
7. **版本：** `rulesVersion` 写入 audit / eval 快照；可回归。
8. **评测：** `evals/suites/rules-cases.jsonl`；冲突实验（Prompt vs 规则）。
9. **审计：** `RULE_FIRED` / `RULE_DENIED`；与第3月 audit 对齐。
10. **上下文：** tenant / roles / principal 注入 RuleContext；规则可调只读 Port，**禁止规则内直写**。
11. **热加载：** 学习版文件监视或重启加载；校验失败不加载。
12. **收官：** console 规则浏览器；三场景串测；PORTFOLIO；架构终图（1～7月）。

### 和第 1～6 月的关系

```text
第1月  会生成、会 RAG、懂 Prompt / few-shot
第2月  会 Hybrid/Gate、HITL 最小流、eval 入门
第3月  会持久化/重建、审计回放、CLASSIFY/RISK 节点
第4月  会 ACL/租户/反馈/console 演示
第5月  会受控写入：Gateway + 假账本 + APPROVE 后才写
第6月  会 Port/Adapter；业务只依赖稳定接口
第7月  把 CLASSIFY/RISK/写前校验从硬编码/Prompt 外置为 RuleEngine
         ↑ 规则先于模型；可版本、可评测、可审计
```

### 能力对照（第6月末 → 第7月末）

| 能力 | 第6月末常见状态 | 第7月末目标 |
|---|---|---|
| CLASSIFY | if/else 或 Prompt | `RuleRegistry` classify 命名空间 |
| RISK_CHECK | 硬编码阈值 | risk 规则集 + WARN |
| 写前校验 | Gateway 内散落判断 | write-precheck RuleSet before Gateway |
| 冲突处理 | 模型说了算 | 规则 DENY 压过 LLM 建议 |
| 回归 | adapter-contract | + rules-cases.jsonl |
| 审计 | WRITE / FLOW | + RULE_FIRED / RULE_DENIED |
| 版本 | erp api version | + rulesVersion 快照 |
| 控制台 | 健康/反馈 | + 规则列表与试跑 |

### 四周路线图

| 周 | Day | 主题 | 结束产出 |
|---|---|---|---|
| 1 | M7-D1～7 | 规则 vs LLM；Rule/Context/Result；决策表；短路 | RuleEngine 骨架 + PeriodClosed 决策表 |
| 2 | M7-D8～14 | 注册表；YAML 装载；CLASSIFY/RISK/写前接入；版本；单测 | 三接入点改调 RuleEngine |
| 3 | M7-D15～21 | rules-cases；冲突实验；审计；ACL；Port 只读；热加载 | eval 绿 + RULE_FIRED 可查 |
| 4 | M7-D22～30 | console；串测 A/B/C；PORTFOLIO；架构终图；收官 | 三场景 + 口述 20 题 |

### 本月编码策略

| 周 | 深挖建议 | 其余 |
|---|---|---|
| 1 | RuleEngine + DecisionTable + 短路 | 读懂 Flow CLASSIFY 现状 |
| 2 | RuleRegistry + YamlRuleLoader + 三接入点 | 版本头读懂即可 |
| 3 | rules-cases + 冲突实验 + Port 规则 | 热加载学习版即可 |
| 4 | console 试跑 + 三串测 + PORTFOLIO | D29 口述必做 |

> **纪律：** 确定性规则不进 Prompt；**禁止**规则内 `PostingPort.write`；**禁止**接公司生产规则平台；写仍经 Gateway+HITL。

### 固定回归题（本月每天都应能跑或口述）

| 套件 | 用途 | 建议频率 |
|---|---|---|
| `rules-cases.jsonl` | 规则 outcome 回归 | 第3周后每日 smoke |
| `adapter-contract.jsonl` | Port 行为（第6月延续） | 每周 |
| `write-safety.jsonl` | 写路径安全（第5月延续） | 每周 |
| `baseline` + 核心 rag suite | 防退化 | 每周五 + 收官日 |

### 学习头速查（全月通用）

```text
X-User-Id:         demo-user-01
X-Roles:           FINANCE,INVENTORY_CLERK
X-Tenant-Id:       tenant-a
X-Idempotency-Key: <写路径强烈建议>
X-Trace-Id:        <可选，关联 RULE_FIRED>
X-Rules-Version:   2026.07.1        # 可选，审计快照
Accept:            application/vnd.erp-ai.v1+json
```

### 周里程碑检查表（建议周五自评）

| 周末 | 必须能演示 | 建议 eval |
|---|---|---|
| 第1周末 | PeriodClosed → DENY；缺仓 WARN | RuleEngine 单测 |
| 第2周末 | CLASSIFY 经 RuleEngine ROUTE | classify 规则单测 |
| 第3周末 | 冲突实验规则胜；RULE_FIRED 可查 | rules-cases |
| 第4周末 | 三串测 + console 试跑 | baseline + rules-cases |

### 与第2/3月 Flow 对照

| 节点 | 第3月起 | 第7月目标 |
|---|---|---|
| CLASSIFY | 规则片段在代码/Prompt | `namespace=classify` RuleSet |
| RISK_CHECK | 硬编码 risk 标签 | `namespace=risk` RuleSet |
| 写前 | Gateway 内 if | `write-precheck` before Gateway |
| TOOL | 固定映射 | ROUTE outcome 驱动 |

### 与第5月 WriteGateway 对照

| 纪律 | 第5月起 | 第7月新增 |
|---|---|---|
| 写入只经 Gateway | ✓ | 写前先跑 write-precheck 规则 |
| APPROVE 才写 | ✓ | 规则 DENY 不进 WAIT_HUMAN |
| 幂等键 | ✓ | 规则审计带 traceId |
| 审计 | ✓ | RULE_FIRED + rulesVersion |
| 不接生产 | ✓ | 本地规则 only |

### 与第6月 Port 对照

| 场景 | Port | 规则可调？ | 规则可写？ |
|---|---|---|---|
| 查期间 | PeriodQueryPort | ✓ 只读 | ✗ 禁止 |
| 查库存 | InventoryQueryPort | ✓ 只读 | ✗ 禁止 |
| 过账 | PostingPort | ✗ 不经规则直写 | 仅 Gateway |

### RuleEngine ASCII 总图（全月锚点）

```text
  Chat / LLM（理解 · 草稿 · 解释 — 不做硬 DENY）
         │
         ▼
  ┌──────────────────────────────────────┐
  │ RuleEngine.evaluate(RuleContext)      │
  │  namespaces: classify | risk |        │
  │              write-precheck | gate    │
  │  outcomes: ALLOW | DENY | WARN | ROUTE│
  └──────────────────────────────────────┘
         │                    │
         ▼                    ▼
   Flow CLASSIFY/RISK    WriteGateway 前 RuleSet
         │                    │
         ▼                    ▼
   RAG_TOOL / HITL      DENY 则拒绝，不写
         │
         ▼
   Port 只读（Period/Inventory）— 规则内禁止 PostingPort 写
```

---



# 第 1 周｜规则 vs LLM 边界 · Rule/Context/Result · 决策表 · 优先级短路

---


## M7-D1 为何需要规则引擎；「全靠 Prompt」的失败模式

### 为什么

第2/3月 Flow 的 CLASSIFY、RISK_CHECK 与第5月写前校验仍散落在 if/else 或 Prompt 里：模型「听起来合理」但不可审计、不可版本化、不可回归。本月引入 **RuleEngine**，把确定性业务规则外置。**不接公司生产规则平台/SSO/生产库。**

### 概念加深

**全靠 Prompt 的失败模式：**

| 症状 | 典型话术 | 根因 | 本月解法 |
|---|---|---|---|
| 关期间仍过账 | 「用户很急先过」 | 无硬 DENY | PeriodClosedRule |
| 意图漂移 | 同句两次不同路由 | 温度+无规则 | CLASSIFY 规则集 |
| 无法审计 | 「模型当时这么答」 | 无 RULE_FIRED | 规则版本+审计 |
| 无法回归 | 改 Prompt 全崩 | 无 jsonl 套件 | rules-cases.jsonl |

```text
用户口语 ──► LLM（理解/草稿/解释）
              │
              ▼
         RuleEngine（确定性 ALLOW/DENY/WARN/ROUTE）
              │
              ▼
    Flow / WriteGateway / Gate（规则优先）
```

**产品句：** 确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。

### 怎么做

1. 列当前仓库里 CLASSIFY/RISK/写前校验的 if/else 或 Prompt 片段。
2. 写「全靠 Prompt」失败故事（至少 3 条）。
3. 画规则 vs LLM 边界图。
4. 背诵核心产品句。

### 代码骨架

```java
// rules/core/RuleOutcome.java — 学习骨架，自行复制到 erp-ai-assistant
package com.example.erp.ai.rules.core;

public enum RuleOutcome {
    ALLOW,   // 放行，可继续后续节点
    DENY,    // 硬拒绝，短路后续规则（默认策略）
    WARN,    // 警告，常配合 needHuman
    ROUTE    // 路由到指定工具/子流（如 RAG_TOOL）
}
```

```java
// rules/core/Rule.java
package com.example.erp.ai.rules.core;

public interface Rule {
    String id();           // 稳定 id，审计用
    String namespace();    // classify | risk | write-precheck | gate
    int priority();        // 越小越先执行（学习期约定）
    RuleResult evaluate(RuleContext ctx);
}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Prompt 当规则库 | 不可版本回归 | 确定性走 RuleEngine |
| 规则里调 LLM | 又不可控 | LLM 只在理解层 |
| 接公司规则平台 | 越界 | 本地 YAML+Java |
| 规则内直写 ERP | 绕过 Gateway | 规则只读 Port |

### 当天验收

- 失败故事 ≥3 条
- 边界图已画
- 能背产品句
- 列出待外置规则点 ≥5

### 核心产品句（当日默念）

> 确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。

### 与上月衔接一句

| 上月（第6月） | 本月（第7月）当日焦点 |
|---|---|
| WriteGateway/Port 稳定 | 为何需要规则引擎 |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 规则单测覆盖主路径
- [ ] 规则内无 PostingPort/WritePort 写操作
- [ ] DENY 短路策略已启用

### 回归命令（D1)

```bash
./mvnw -q test -Dtest='*Rule*Test' 2>/dev/null || true
curl -s -X POST http://localhost:8080/api/ai/eval/run \
  -H 'Content-Type: application/json' -d '{"suite":"rules-cases"}' 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/console/rules 2>/dev/null || echo "console 未启"
```
---


## M7-D2 什么该规则、什么该 LLM（对照表）

### 为什么

规则与 LLM 分工不清时，团队要么「全 Prompt」要么「全 if」，两者都不可维护。今天冻结对照表：**什么必须规则、什么可以 LLM**。**不接公司生产。**

### 概念加深

| 维度 | 适合 **规则引擎** | 适合 **LLM** |
|---|---|---|
| 期间关闭 | 硬 DENY | 解释为何不能过账 |
| 权限/角色 | DENY 无 FINANCE | 润色拒绝话术 |
| 意图路由关键词 | ROUTE inventory_query | 口语变体理解 |
| 必填字段 | DENY 缺 warehouse | 从口语抽字段草稿 |
| 风险打标 | WARN 大额/敏感 | 生成风险说明 |
| 数值阈值 | 表驱动决策 | 不推荐让模型算账 |

```text
冲突处理：LLM 建议「可以过账」+ 规则 PERIOD_CLOSED → **以规则为准**
```

对照表贴墙；改表需 PR + rules-cases 回归。

### 怎么做

1. 填团队对照表（至少 10 行）。
2. 标红「必须规则」项。
3. 为每项写一条 jsonl 样例 id。
4. 与第2月 Gate 对照：Gate 拦幻觉，规则拦业务违法。

### 代码骨架

```java
// rules/classify/InventoryQueryRouteRule.java
package com.example.erp.ai.rules.classify;

public class InventoryQueryRouteRule implements Rule {
    @Override public String id() { return "classify.inventory_query"; }
    @Override public String namespace() { return "classify"; }
    @Override public int priority() { return 10; }
    @Override public RuleResult evaluate(RuleContext ctx) {
        String q = ctx.getString("userText").orElse("");
        if (q.matches(".*(库存|有多少|还剩).*")) {
            return RuleResult.route("RAG_TOOL", "inventory_query", Map.of("tool", "inventory"));
        }
        return RuleResult.allow("no_match");
    }
}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| LLM 做权限判断 | 可绕过 | roles 进 RuleContext |
| 规则生成自然语言 | 职责混乱 | 规则只产出 outcome+code |
| 无对照表 | 争论不休 | 文档化+评测 |
| Gate 与规则重复 | 双维护 | Gate=事实；规则=业务 |

### 当天验收

- 对照表 ≥10 行
- 标红项有 jsonl id
- 能讲 Gate vs Rule
- 产品句默念

### 核心产品句（当日默念）

> 确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。

### 与上月衔接一句

| 上月（第6月） | 本月（第7月）当日焦点 |
|---|---|
| Port/Adapter 契约 | 什么该规则、什么该 LLM（对照表） |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 规则单测覆盖主路径
- [ ] 规则内无 PostingPort/WritePort 写操作
- [ ] DENY 短路策略已启用

### 回归命令（D2)

```bash
./mvnw -q test -Dtest='*Rule*Test' 2>/dev/null || true
curl -s -X POST http://localhost:8080/api/ai/eval/run \
  -H 'Content-Type: application/json' -d '{"suite":"rules-cases"}' 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/console/rules 2>/dev/null || echo "console 未启"
```
---


## M7-D3 Rule / RuleContext / RuleResult（ALLOW/DENY/WARN/ROUTE）

### 为什么

第6月 Port 已稳定；本月在编排层外置规则。**不接公司生产规则平台/SSO/生产库。** 没有统一的 `RuleContext`/`RuleResult`，各节点自定义 Map，审计与单测无法复用。今天冻结四种 outcome 语义。

### 概念加深

| Outcome | 语义 | 典型后续 | 短路？ |
|---|---|---|---|
| ALLOW | 未命中或显式放行 | 继续下一规则/节点 | 否 |
| DENY | 硬拒绝 | 失败/拒绝，不写 | 常短路 |
| WARN | 警告 | needHuman + warnings | 否 |
| ROUTE | 路由 | 指定 tool/子流 | 常短路 |

```java
RuleResult.deny("PERIOD_CLOSED", "会计期间已关闭");
RuleResult.warn("LARGE_AMOUNT", "金额超过阈值");
RuleResult.route("RAG_TOOL", "inventory_query", Map.of("tool","inventory"));
```

`RuleContext` 建议：`tenantId, userId, roles, namespace, attributes(Map), ports(只读引用)`。

### 怎么做

1. 定义 RuleOutcome 枚举。
2. 实现 RuleContext builder（tenant/roles/attributes）。
3. 实现 RuleResult 工厂方法。
4. 单测四种 outcome。

### 代码骨架

```java
package com.example.erp.ai.rules.core;

import java.util.*;

public final class RuleContext {
    private final String tenantId;
    private final String userId;
    private final Set<String> roles;
    private final String namespace;
    private final Map<String, Object> attributes;
    private final RulePortAccessor ports; // 只读 Port 门面，D19 详述

    public Optional<String> getString(String key) {
        return Optional.ofNullable(attributes.get(key)).map(Object::toString);
    }
    // builder / getters ...
}

public record RuleResult(RuleOutcome outcome, String code, String message, Map<String, Object> meta) {
    public static RuleResult allow(String code) { return new RuleResult(RuleOutcome.ALLOW, code, null, Map.of()); }
    public static RuleResult deny(String code, String msg) { return new RuleResult(RuleOutcome.DENY, code, msg, Map.of()); }
    public static RuleResult warn(String code, String msg) { return new RuleResult(RuleOutcome.WARN, code, msg, Map.of()); }
    public static RuleResult route(String code, String route, Map<String, Object> meta) {
        return new RuleResult(RuleOutcome.ROUTE, code, route, meta);
    }
}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Prompt 当规则 | 不可审计 | RuleEngine |
| 规则内直写 | 绕过 HITL | Gateway only |
| 接公司规则平台 | 越界 | 本地 YAML |
| DENY 后被 ALLOW 覆盖 | 安全洞 | shortCircuitOnDeny |

### 当天验收

- 今日「怎么做」步骤完成
- 单测/手测通过
- 产品句能背
- 手测表已填

### 核心产品句（当日默念）

> 确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。

### 与上月衔接一句

| 上月（第6月） | 本月（第7月）当日焦点 |
|---|---|
| Port/Adapter 契约 | Rule / RuleContext / RuleResult（ALLOW/DENY/WARN/ROUTE） |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 规则单测覆盖主路径
- [ ] 规则内无 PostingPort/WritePort 写操作
- [ ] DENY 短路策略已启用

### 回归命令（D3)

```bash
./mvnw -q test -Dtest='*Rule*Test' 2>/dev/null || true
curl -s -X POST http://localhost:8080/api/ai/eval/run \
  -H 'Content-Type: application/json' -d '{"suite":"rules-cases"}' 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/console/rules 2>/dev/null || echo "console 未启"
```
---


## M7-D4 RuleEngine.evaluate(context) 顺序、优先级、短路

### 为什么

第6月 Port 已稳定；本月在编排层外置规则。**不接公司生产规则平台/SSO/生产库。** 多规则并存时必须定义 **顺序、优先级、短路**；否则 DENY 与 ALLOW 打架无法解释。

### 概念加深

执行顺序（学习期约定）：

```text
1. 按 namespace 过滤 RuleRegistry
2. 按 priority 升序（小优先）
3. 依次 evaluate
4. 若 outcome=DENY 且 shortCircuitOnDeny=true → 立即返回
5. 若 outcome=ROUTE 且 shortCircuitOnRoute=true → 立即返回
6. 收集 WARN，最终合成 AggregatedRuleResult
```

| 策略 | 说明 |
|---|---|
| shortCircuitOnDeny | 默认 true，防「后面 ALLOW 覆盖 DENY」 |
| shortCircuitOnRoute | 默认 true，CLASSIFY 命中即停 |
| collectWarnings | RISK 命名空间常 true |

### 怎么做

1. 实现 DefaultRuleEngine.evaluate。
2. 配置 shortCircuit 开关。
3. 单测：DENY 后不再执行低优先级 ALLOW。
4. 单测：ROUTE 短路。

### 代码骨架

```java
package com.example.erp.ai.rules.core;

import java.util.*;

public class DefaultRuleEngine implements RuleEngine {
    private final RuleRegistry registry;
    private final boolean shortCircuitOnDeny = true;
    private final boolean shortCircuitOnRoute = true;

    @Override
    public AggregatedRuleResult evaluate(RuleContext ctx) {
        List<RuleResult> fired = new ArrayList<>();
        List<RuleResult> warnings = new ArrayList<>();
        for (Rule rule : registry.rulesFor(ctx.namespace())) {
            RuleResult r = rule.evaluate(ctx);
            fired.add(r.withMeta(Map.of("ruleId", rule.id())));
            switch (r.outcome()) {
                case DENY -> {
                    if (shortCircuitOnDeny) return AggregatedRuleResult.terminal(r, fired);
                }
                case ROUTE -> {
                    if (shortCircuitOnRoute) return AggregatedRuleResult.terminal(r, fired);
                }
                case WARN -> warnings.add(r);
                default -> { }
            }
        }
        return AggregatedRuleResult.completed(warnings, fired);
    }
}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Prompt 当规则 | 不可审计 | RuleEngine |
| 规则内直写 | 绕过 HITL | Gateway only |
| 接公司规则平台 | 越界 | 本地 YAML |
| DENY 后被 ALLOW 覆盖 | 安全洞 | shortCircuitOnDeny |

### 当天验收

- 今日「怎么做」步骤完成
- 单测/手测通过
- 产品句能背
- 手测表已填

### 核心产品句（当日默念）

> 确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。

### 与上月衔接一句

| 上月（第6月） | 本月（第7月）当日焦点 |
|---|---|
| Port/Adapter 契约 | RuleEngine.evaluate(context) 顺序、优先级、短路 |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 规则单测覆盖主路径
- [ ] 规则内无 PostingPort/WritePort 写操作
- [ ] DENY 短路策略已启用

### 回归命令（D4)

```bash
./mvnw -q test -Dtest='*Rule*Test' 2>/dev/null || true
curl -s -X POST http://localhost:8080/api/ai/eval/run \
  -H 'Content-Type: application/json' -d '{"suite":"rules-cases"}' 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/console/rules 2>/dev/null || echo "console 未启"
```
---


## M7-D5 决策表：期间关闭禁止过账；缺仓库 WARN

### 为什么

第6月 Port 已稳定；本月在编排层外置规则。**不接公司生产规则平台/SSO/生产库。** 「期间关闭禁止过账」「缺仓库 WARN」是 ERP 最典型的表驱动规则，适合 DecisionTable 入门。

### 概念加深

决策表示例（期间 + 仓库）：

| periodOpen | warehousePresent | outcome | code |
|---|---|---|---|
| false | * | DENY | PERIOD_CLOSED |
| true | false | WARN | WAREHOUSE_MISSING |
| true | true | ALLOW | OK |

```text
PeriodClosedRow → DENY
MissingWarehouseRow → WARN
DefaultRow → ALLOW
```

与 Port：`PeriodQueryPort.isPostingAllowed` 填 `periodOpen`；payload 填 `warehouse`。

### 怎么做

1. 实现 DecisionTableRule。
2. 写 period/warehouse 表 YAML。
3. 接 PeriodQueryPort 填 context。
4. 手测关期间 DENY。

### 代码骨架

```java
package com.example.erp.ai.rules.table;

public class DecisionTableRule implements Rule {
    private final String id;
    private final String namespace;
    private final int priority;
    private final List<DecisionRow> rows;

    @Override
    public RuleResult evaluate(RuleContext ctx) {
        for (DecisionRow row : rows) {
            if (row.matches(ctx)) return row.toResult();
        }
        return RuleResult.allow("TABLE_DEFAULT_ALLOW");
    }
}

// rules/write-precheck/period-warehouse-table.yaml
// rows:
//   - when: { periodOpen: false }
//     outcome: DENY
//     code: PERIOD_CLOSED
//   - when: { warehousePresent: false }
//     outcome: WARN
//     code: WAREHOUSE_MISSING
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Prompt 当规则 | 不可审计 | RuleEngine |
| 规则内直写 | 绕过 HITL | Gateway only |
| 接公司规则平台 | 越界 | 本地 YAML |
| DENY 后被 ALLOW 覆盖 | 安全洞 | shortCircuitOnDeny |

### 当天验收

- 今日「怎么做」步骤完成
- 单测/手测通过
- 产品句能背
- 手测表已填

### 核心产品句（当日默念）

> 确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。

### 与上月衔接一句

| 上月（第6月） | 本月（第7月）当日焦点 |
|---|---|
| Port/Adapter 契约 | 决策表：期间关闭禁止过账 |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 规则单测覆盖主路径
- [ ] 规则内无 PostingPort/WritePort 写操作
- [ ] DENY 短路策略已启用

### 回归命令（D5)

```bash
./mvnw -q test -Dtest='*Rule*Test' 2>/dev/null || true
curl -s -X POST http://localhost:8080/api/ai/eval/run \
  -H 'Content-Type: application/json' -d '{"suite":"rules-cases"}' 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/console/rules 2>/dev/null || echo "console 未启"
```
---


## M7-D6 与 Flow 数据：把 intent/risk 从硬编码迁到规则

### 为什么

第6月 Port 已稳定；本月在编排层外置规则。**不接公司生产规则平台/SSO/生产库。** 第3月 Flow 里 intent/risk 硬编码阻碍版本化；今天规划迁移到 RuleEngine，保持节点职责不变。

### 概念加深

迁移对照：

| 原位置 | 字段 | 迁到 |
|---|---|---|
| doClassify() if | intent | classify.* RouteRule |
| doRisk() 阈值 | riskLevel | risk.* WarnRule |
| Gateway if period | DENY | write-precheck.* |

Flow 节点仍保留 CLASSIFY/RISK_CHECK **调用** RuleEngine，不删节点。

### 怎么做

1. grep Flow 硬编码 classify/risk。
2. 列迁移清单。
3. 实现第一条 InventoryQueryRouteRule。
4. Flow 改调 RuleEngine（骨架）。

### 代码骨架

```java
// rules — 学习骨架 M7-D6
package com.example.erp.ai.rules;

// 见附录 B 包结构；自行复制，勿直接改 erp-ai-assistant 源仓
public class M7_D6Placeholder {
    // 与 Flow 数据：把 intent/risk 从硬编码迁到规则
}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Prompt 当规则 | 不可审计 | RuleEngine |
| 规则内直写 | 绕过 HITL | Gateway only |
| 接公司规则平台 | 越界 | 本地 YAML |
| DENY 后被 ALLOW 覆盖 | 安全洞 | shortCircuitOnDeny |

### 当天验收

- 今日「怎么做」步骤完成
- 单测/手测通过
- 产品句能背
- 手测表已填

### 核心产品句（当日默念）

> 确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。

### 与上月衔接一句

| 上月（第6月） | 本月（第7月）当日焦点 |
|---|---|
| Port/Adapter 契约 | 与 Flow 数据：把 intent/risk 从硬编码迁到规则 |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 规则单测覆盖主路径
- [ ] 规则内无 PostingPort/WritePort 写操作
- [ ] DENY 短路策略已启用

### 回归命令（D6)

```bash
./mvnw -q test -Dtest='*Rule*Test' 2>/dev/null || true
curl -s -X POST http://localhost:8080/api/ai/eval/run \
  -H 'Content-Type: application/json' -d '{"suite":"rules-cases"}' 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/console/rules 2>/dev/null || echo "console 未启"
```
---


## M7-D7 第 1 周复盘（规则边界 + 核心类型 + 决策表）

### 为什么

第6月 Port 已稳定；本月在编排层外置规则。**不接公司生产规则平台/SSO/生产库。** 巩固第1周：边界、类型、决策表、短路；为第2周接入打地基。

### 概念加深

**今日焦点：周复盘**

```text
RuleEngine ──► 第 1 周复盘（规则边界 + 核心类型 + 决策表）
```

与上月衔接：第6月 Port 供规则只读查数；第5月 Gateway 写前跑 write-precheck；第3月 Flow 节点改调引擎。

重复产品句：**确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。**

### 怎么做

1. 跑 W1 检查表。
2. RuleEngine 单测全绿。
3. 手测 PeriodClosed。
4. 预习 RuleRegistry。

### 代码骨架

```java
// rules — 学习骨架 M7-D7
package com.example.erp.ai.rules;

// 见附录 B 包结构；自行复制，勿直接改 erp-ai-assistant 源仓
public class M7_D7Placeholder {
    // 第 1 周复盘（规则边界 + 核心类型 + 决策表）
}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| 周复盘跳过单测 | 接入周崩盘 | Contract+Rule 单测 |
| 决策表未测 | 关期间漏网 | PeriodClosed 手测 |

### 当天验收

- W1 检查表全勾
- PeriodClosed DENY
- RuleEngine 单测绿
- 预习 Registry

### 核心产品句（当日默念）

> 确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。

### 与上月衔接一句

| 上月（第6月） | 本月（第7月）当日焦点 |
|---|---|
| Port/Adapter 契约 | 第 1 周复盘（规则边界 + 核心类型 + 决策表） |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 规则单测覆盖主路径
- [ ] 规则内无 PostingPort/WritePort 写操作
- [ ] DENY 短路策略已启用

### 周复盘自测清单

见「概念加深」检查表；周五必跑 RuleEngine + rules smoke。

### 回归命令（D7)

```bash
./mvnw -q test -Dtest='*Rule*Test' 2>/dev/null || true
curl -s -X POST http://localhost:8080/api/ai/eval/run \
  -H 'Content-Type: application/json' -d '{"suite":"rules-cases"}' 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/console/rules 2>/dev/null || echo "console 未启"
```
---


# 第 2 周｜接入点 · DSL/YAML · CLASSIFY/RISK/写前校验 · 版本号

---


## M7-D8 规则注册表 RuleRegistry + 命名空间（classify/risk/write-precheck）

### 为什么

第6月 Port 已稳定；本月在编排层外置规则。**不接公司生产规则平台/SSO/生产库。** 规则散落多包无命名空间会冲突；`RuleRegistry` 按 classify/risk/write-precheck 隔离。

### 概念加深

**今日焦点：注册与命名空间**

```text
RuleEngine ──► 规则注册表 RuleRegistry + 命名空间（classify/risk/write-precheck）
```

与上月衔接：第6月 Port 供规则只读查数；第5月 Gateway 写前跑 write-precheck；第3月 Flow 节点改调引擎。

重复产品句：**确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。**

### 怎么做

1. 实现 RuleRegistry register/list by namespace。
2. 注册 classify/risk/write-precheck 空集。
3. Spring 装配注入 Engine。
4. 单测命名空间隔离。

### 代码骨架

```java
package com.example.erp.ai.rules.registry;

import java.util.*;

public class DefaultRuleRegistry implements RuleRegistry {
    private final Map<String, List<Rule>> byNs = new HashMap<>();

    public void register(Rule rule) {
        byNs.computeIfAbsent(rule.namespace(), k -> new ArrayList<>()).add(rule);
        byNs.get(rule.namespace()).sort(Comparator.comparingInt(Rule::priority));
    }

    @Override
    public List<Rule> rulesFor(String namespace) {
        return List.copyOf(byNs.getOrDefault(namespace, List.of()));
    }
}
// 命名空间：classify | risk | write-precheck | gate
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Prompt 当规则 | 不可审计 | RuleEngine |
| 规则内直写 | 绕过 HITL | Gateway only |
| 接公司规则平台 | 越界 | 本地 YAML |
| DENY 后被 ALLOW 覆盖 | 安全洞 | shortCircuitOnDeny |

### 当天验收

- 今日「怎么做」步骤完成
- 单测/手测通过
- 产品句能背
- 手测表已填

### 核心产品句（当日默念）

> 确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。

### 与上月衔接一句

| 上月（第6月） | 本月（第7月）当日焦点 |
|---|---|
| Port/Adapter 契约 | 规则注册表 RuleRegistry + 命名空间（classify/risk/write-precheck） |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 规则单测覆盖主路径
- [ ] 规则内无 PostingPort/WritePort 写操作
- [ ] DENY 短路策略已启用

### 回归命令（D8)

```bash
./mvnw -q test -Dtest='*Rule*Test' 2>/dev/null || true
curl -s -X POST http://localhost:8080/api/ai/eval/run \
  -H 'Content-Type: application/json' -d '{"suite":"rules-cases"}' 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/console/rules 2>/dev/null || echo "console 未启"
```
---


## M7-D9 YAML/JSON 规则装载（学习版）+ Java 实现类混用

### 为什么

第6月 Port 已稳定；本月在编排层外置规则。**不接公司生产规则平台/SSO/生产库。** 纯 Java 规则难让非开发改表；学习版 YAML/JSON 与 Java Rule 混用，校验失败不加载。

### 概念加深

**今日焦点：规则装载**

```text
RuleEngine ──► YAML/JSON 规则装载（学习版）+ Java 实现类混用
```

与上月衔接：第6月 Port 供规则只读查数；第5月 Gateway 写前跑 write-precheck；第3月 Flow 节点改调引擎。

重复产品句：**确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。**

### 怎么做

1. 实现 YamlRuleLoader（Jackson）。
2. rules/classify/*.yaml 样例。
3. Java Rule 与 YAML 表混用。
4. 校验失败抛 RuleLoadException。

### 代码骨架

```java
package com.example.erp.ai.rules.load;

public class YamlRuleLoader {
    public List<DecisionTableRule> loadTables(Path dir) throws IOException {
        List<DecisionTableRule> out = new ArrayList<>();
        try (var paths = Files.walk(dir)) {
            paths.filter(p -> p.toString().endsWith(".yaml")).forEach(p -> {
                TableDefinition def = yamlMapper.readValue(p.toFile(), TableDefinition.class);
                validate(def); // 失败抛 RuleLoadException，不 partial load
                out.add(def.toRule());
            });
        }
        return out;
    }
}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Prompt 当规则 | 不可审计 | RuleEngine |
| 规则内直写 | 绕过 HITL | Gateway only |
| 接公司规则平台 | 越界 | 本地 YAML |
| DENY 后被 ALLOW 覆盖 | 安全洞 | shortCircuitOnDeny |

### 当天验收

- 今日「怎么做」步骤完成
- 单测/手测通过
- 产品句能背
- 手测表已填

### 核心产品句（当日默念）

> 确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。

### 与上月衔接一句

| 上月（第6月） | 本月（第7月）当日焦点 |
|---|---|
| Port/Adapter 契约 | YAML/JSON 规则装载（学习版）+ Java 实现类混用 |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 规则单测覆盖主路径
- [ ] 规则内无 PostingPort/WritePort 写操作
- [ ] DENY 短路策略已启用

### 回归命令（D9)

```bash
./mvnw -q test -Dtest='*Rule*Test' 2>/dev/null || true
curl -s -X POST http://localhost:8080/api/ai/eval/run \
  -H 'Content-Type: application/json' -d '{"suite":"rules-cases"}' 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/console/rules 2>/dev/null || echo "console 未启"
```
---


## M7-D10 CLASSIFY 节点改调 RuleEngine

### 为什么

第6月 Port 已稳定；本月在编排层外置规则。**不接公司生产规则平台/SSO/生产库。** CLASSIFY 是规则引擎第一接入点：用 ROUTE 替代 if 匹配 inventory/posting 意图。

### 概念加深

**今日焦点：CLASSIFY 接入**

```text
RuleEngine ──► CLASSIFY 节点改调 RuleEngine
```

与上月衔接：第6月 Port 供规则只读查数；第5月 Gateway 写前跑 write-precheck；第3月 Flow 节点改调引擎。

重复产品句：**确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。**

### 怎么做

1. doClassify 改为 engine.evaluate(ns=classify)。
2. ROUTE → 设置 tool 字段。
3. 无匹配 ALLOW 走默认 LLM classify。
4. 单测 ROUTE inventory。

### 代码骨架

```java
// flow/FlowOrchestrator.java — doClassify 改造骨架
private void doClassify(FlowInstance fi) {
    RuleContext ctx = RuleContext.builder()
        .namespace("classify")
        .tenantId(fi.tenantId())
        .userId(fi.userId())
        .roles(fi.roles())
        .attribute("userText", fi.question())
        .build();
    AggregatedRuleResult ar = ruleEngine.evaluate(ctx);
    audit.emit(RuleAuditEvent.fired(ctx, ar, rulesVersion));
    if (ar.terminal().outcome() == RuleOutcome.ROUTE) {
        fi.setTool(ar.terminal().meta().get("tool").toString());
        transition(fi, FlowState.RETRIEVE, "CLASSIFY_ROUTE", ar.toMap());
        return;
    }
  // fallback: LLM 辅助 classify（仅理解，不硬 DENY）
}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Prompt 当规则 | 不可审计 | RuleEngine |
| 规则内直写 | 绕过 HITL | Gateway only |
| 接公司规则平台 | 越界 | 本地 YAML |
| DENY 后被 ALLOW 覆盖 | 安全洞 | shortCircuitOnDeny |

### 当天验收

- 今日「怎么做」步骤完成
- 单测/手测通过
- 产品句能背
- 手测表已填

### 核心产品句（当日默念）

> 确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。

### 与上月衔接一句

| 上月（第6月） | 本月（第7月）当日焦点 |
|---|---|
| Port/Adapter 契约 | CLASSIFY 节点改调 RuleEngine |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 规则单测覆盖主路径
- [ ] 规则内无 PostingPort/WritePort 写操作
- [ ] DENY 短路策略已启用

### 回归命令（D10)

```bash
./mvnw -q test -Dtest='*Rule*Test' 2>/dev/null || true
curl -s -X POST http://localhost:8080/api/ai/eval/run \
  -H 'Content-Type: application/json' -d '{"suite":"rules-cases"}' 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/console/rules 2>/dev/null || echo "console 未启"
```
---


## M7-D11 RISK_CHECK / 写前校验规则集

### 为什么

第6月 Port 已稳定；本月在编排层外置规则。**不接公司生产规则平台/SSO/生产库。** RISK_CHECK 产出 WARN；write-precheck 在 Gateway 前 DENY — 与第5月写入纪律衔接。

### 概念加深

**今日焦点：风险与写前校验**

```text
RuleEngine ──► RISK_CHECK / 写前校验规则集
```

与上月衔接：第6月 Port 供规则只读查数；第5月 Gateway 写前跑 write-precheck；第3月 Flow 节点改调引擎。

重复产品句：**确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。**

### 怎么做

1. risk 规则：大额 WARN。
2. write-precheck：PERIOD_CLOSED DENY。
3. Gateway 入口先 prechek。
4. 单测 DENY 不写审计 WRITE。

### 代码骨架

```java
// rules — 学习骨架 M7-D11
package com.example.erp.ai.rules;

// 见附录 B 包结构；自行复制，勿直接改 erp-ai-assistant 源仓
public class M7_D11Placeholder {
    // RISK_CHECK / 写前校验规则集
}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Prompt 当规则 | 不可审计 | RuleEngine |
| 规则内直写 | 绕过 HITL | Gateway only |
| 接公司规则平台 | 越界 | 本地 YAML |
| DENY 后被 ALLOW 覆盖 | 安全洞 | shortCircuitOnDeny |

### 当天验收

- 今日「怎么做」步骤完成
- 单测/手测通过
- 产品句能背
- 手测表已填

### 核心产品句（当日默念）

> 确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。

### 与上月衔接一句

| 上月（第6月） | 本月（第7月）当日焦点 |
|---|---|
| Port/Adapter 契约 | RISK_CHECK / 写前校验规则集 |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 规则单测覆盖主路径
- [ ] 规则内无 PostingPort/WritePort 写操作
- [ ] DENY 短路策略已启用

### 回归命令（D11)

```bash
./mvnw -q test -Dtest='*Rule*Test' 2>/dev/null || true
curl -s -X POST http://localhost:8080/api/ai/eval/run \
  -H 'Content-Type: application/json' -d '{"suite":"rules-cases"}' 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/console/rules 2>/dev/null || echo "console 未启"
```
---


## M7-D12 规则版本 rulesVersion 写入 audit / eval config 快照

### 为什么

第6月 Port 已稳定；本月在编排层外置规则。**不接公司生产规则平台/SSO/生产库。** 规则变更必须可审计可回放；`rulesVersion` 写入 audit 与 eval 配置快照。

### 概念加深

**今日焦点：版本与审计**

```text
RuleEngine ──► 规则版本 rulesVersion 写入 audit / eval config 快照
```

与上月衔接：第6月 Port 供规则只读查数；第5月 Gateway 写前跑 write-precheck；第3月 Flow 节点改调引擎。

重复产品句：**确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。**

### 怎么做

1. rules/version.properties → rulesVersion。
2. audit 事件带 rulesVersion。
3. eval run 快照 rulesVersion。
4. 改规则升版本号。

### 代码骨架

```java
// rules — 学习骨架 M7-D12
package com.example.erp.ai.rules;

// 见附录 B 包结构；自行复制，勿直接改 erp-ai-assistant 源仓
public class M7_D12Placeholder {
    // 规则版本 rulesVersion 写入 audit / eval config 快照
}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Prompt 当规则 | 不可审计 | RuleEngine |
| 规则内直写 | 绕过 HITL | Gateway only |
| 接公司规则平台 | 越界 | 本地 YAML |
| DENY 后被 ALLOW 覆盖 | 安全洞 | shortCircuitOnDeny |

### 当天验收

- 今日「怎么做」步骤完成
- 单测/手测通过
- 产品句能背
- 手测表已填

### 核心产品句（当日默念）

> 确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。

### 与上月衔接一句

| 上月（第6月） | 本月（第7月）当日焦点 |
|---|---|
| Port/Adapter 契约 | 规则版本 rulesVersion 写入 audit / eval config 快照 |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 规则单测覆盖主路径
- [ ] 规则内无 PostingPort/WritePort 写操作
- [ ] DENY 短路策略已启用

### 回归命令（D12)

```bash
./mvnw -q test -Dtest='*Rule*Test' 2>/dev/null || true
curl -s -X POST http://localhost:8080/api/ai/eval/run \
  -H 'Content-Type: application/json' -d '{"suite":"rules-cases"}' 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/console/rules 2>/dev/null || echo "console 未启"
```
---


## M7-D13 规则单元测试模式（给定 context → 断言结果）

### 为什么

第6月 Port 已稳定；本月在编排层外置规则。**不接公司生产规则平台/SSO/生产库。** 规则最适合表驱动单测：给定 RuleContext → 断言 RuleResult；不依赖 LLM。

### 概念加深

**今日焦点：规则单测**

```text
RuleEngine ──► 规则单元测试模式（给定 context → 断言结果）
```

与上月衔接：第6月 Port 供规则只读查数；第5月 Gateway 写前跑 write-precheck；第3月 Flow 节点改调引擎。

重复产品句：**确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。**

### 怎么做

1. 每规则一个 @Test。
2. RuleTestFixtures 构建 Context。
3. 参数化 jsonl 驱动（可选）。
4. CI 跑 rules 包测试。

### 代码骨架

```java
// rules — 学习骨架 M7-D13
package com.example.erp.ai.rules;

// 见附录 B 包结构；自行复制，勿直接改 erp-ai-assistant 源仓
public class M7_D13Placeholder {
    // 规则单元测试模式（给定 context → 断言结果）
}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Prompt 当规则 | 不可审计 | RuleEngine |
| 规则内直写 | 绕过 HITL | Gateway only |
| 接公司规则平台 | 越界 | 本地 YAML |
| DENY 后被 ALLOW 覆盖 | 安全洞 | shortCircuitOnDeny |

### 当天验收

- 今日「怎么做」步骤完成
- 单测/手测通过
- 产品句能背
- 手测表已填

### 核心产品句（当日默念）

> 确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。

### 与上月衔接一句

| 上月（第6月） | 本月（第7月）当日焦点 |
|---|---|
| Port/Adapter 契约 | 规则单元测试模式（给定 context → 断言结果） |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 规则单测覆盖主路径
- [ ] 规则内无 PostingPort/WritePort 写操作
- [ ] DENY 短路策略已启用

### 回归命令（D13)

```bash
./mvnw -q test -Dtest='*Rule*Test' 2>/dev/null || true
curl -s -X POST http://localhost:8080/api/ai/eval/run \
  -H 'Content-Type: application/json' -d '{"suite":"rules-cases"}' 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/console/rules 2>/dev/null || echo "console 未启"
```
---


## M7-D14 第 2 周复盘（接入点 + 版本 + 单测）

### 为什么

第6月 Port 已稳定；本月在编排层外置规则。**不接公司生产规则平台/SSO/生产库。** 巩固接入点、装载、版本、单测；跑 classify+risk+precheck smoke。

### 概念加深

**今日焦点：周复盘**

```text
RuleEngine ──► 第 2 周复盘（接入点 + 版本 + 单测）
```

与上月衔接：第6月 Port 供规则只读查数；第5月 Gateway 写前跑 write-precheck；第3月 Flow 节点改调引擎。

重复产品句：**确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。**

### 怎么做

1. W2 检查表。
2. 三命名空间 smoke。
3. rulesVersion 在 audit。
4. 预习 rules-cases。

### 代码骨架

```java
// rules — 学习骨架 M7-D14
package com.example.erp.ai.rules;

// 见附录 B 包结构；自行复制，勿直接改 erp-ai-assistant 源仓
public class M7_D14Placeholder {
    // 第 2 周复盘（接入点 + 版本 + 单测）
}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Prompt 当规则 | 不可审计 | RuleEngine |
| 规则内直写 | 绕过 HITL | Gateway only |
| 接公司规则平台 | 越界 | 本地 YAML |
| DENY 后被 ALLOW 覆盖 | 安全洞 | shortCircuitOnDeny |

### 当天验收

- W2 检查表全勾
- 三命名空间接入
- rulesVersion 在 audit
- 规则单测绿

### 核心产品句（当日默念）

> 确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。

### 与上月衔接一句

| 上月（第6月） | 本月（第7月）当日焦点 |
|---|---|
| Port/Adapter 契约 | 第 2 周复盘（接入点 + 版本 + 单测） |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 规则单测覆盖主路径
- [ ] 规则内无 PostingPort/WritePort 写操作
- [ ] DENY 短路策略已启用

### 周复盘自测清单

见「概念加深」检查表；周五必跑 RuleEngine + rules smoke。

### 回归命令（D14)

```bash
./mvnw -q test -Dtest='*Rule*Test' 2>/dev/null || true
curl -s -X POST http://localhost:8080/api/ai/eval/run \
  -H 'Content-Type: application/json' -d '{"suite":"rules-cases"}' 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/console/rules 2>/dev/null || echo "console 未启"
```
---


# 第 3 周｜规则评测 · 冲突实验 · 审计 · ACL · Port 只读 · 热加载

---


## M7-D15 evals/suites/rules-cases.jsonl 规则评测套件

### 为什么

第6月 Port 已稳定；本月在编排层外置规则。**不接公司生产规则平台/SSO/生产库。** 与第3月 eval 对齐；`rules-cases.jsonl` 专测规则 outcome，不测文案相似度。

### 概念加深

**今日焦点：评测套件**

```text
RuleEngine ──► evals/suites/rules-cases.jsonl 规则评测套件
```

与上月衔接：第6月 Port 供规则只读查数；第5月 Gateway 写前跑 write-precheck；第3月 Flow 节点改调引擎。

重复产品句：**确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。**

### 怎么做

1. 新建 evals/suites/rules-cases.jsonl。
2. RuleEvalRunner 读 case 调 Engine。
3. 断言 outcome+code。
4. POST /api/ai/eval/run suite=rules-cases。

### 代码骨架

```jsonl
{"id":"rc-001","namespace":"write-precheck","context":{"tenantId":"tenant-a","periodOpen":false,"action":"POST_DOCUMENT"},"expectOutcome":"DENY","expectCode":"PERIOD_CLOSED"}
{"id":"rc-002","namespace":"classify","context":{"userText":"SKU-100还有多少库存"},"expectOutcome":"ROUTE","expectCode":"inventory_query"}
{"id":"rc-003","namespace":"risk","context":{"amount":1000000},"expectOutcome":"WARN","expectCode":"LARGE_AMOUNT"}
{"id":"rc-004","namespace":"write-precheck","context":{"roles":["INVENTORY_CLERK"],"action":"POST_DOCUMENT"},"expectOutcome":"DENY","expectCode":"FORBIDDEN_ROLE"}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Prompt 当规则 | 不可审计 | RuleEngine |
| 规则内直写 | 绕过 HITL | Gateway only |
| 接公司规则平台 | 越界 | 本地 YAML |
| DENY 后被 ALLOW 覆盖 | 安全洞 | shortCircuitOnDeny |

### 当天验收

- 今日「怎么做」步骤完成
- 单测/手测通过
- 产品句能背
- 手测表已填

### 核心产品句（当日默念）

> 确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。

### 与上月衔接一句

| 上月（第6月） | 本月（第7月）当日焦点 |
|---|---|
| Port/Adapter 契约 | evals/suites/rules-cases.jsonl 规则评测套件 |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 规则单测覆盖主路径
- [ ] 规则内无 PostingPort/WritePort 写操作
- [ ] DENY 短路策略已启用

### 回归命令（D15)

```bash
./mvnw -q test -Dtest='*Rule*Test' 2>/dev/null || true
curl -s -X POST http://localhost:8080/api/ai/eval/run \
  -H 'Content-Type: application/json' -d '{"suite":"rules-cases"}' 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/console/rules 2>/dev/null || echo "console 未启"
```
---


## M7-D16 冲突实验：Prompt 说可以、规则 DENY → 以规则为准

### 为什么

第6月 Port 已稳定；本月在编排层外置规则。**不接公司生产规则平台/SSO/生产库。** 刻意构造 Prompt 诱导「可以过账」；验证 **规则 DENY 优先**，产品承诺可演示。

### 概念加深

**今日焦点：冲突实验**

```text
RuleEngine ──► 冲突实验：Prompt 说可以、规则 DENY → 以规则为准
```

与上月衔接：第6月 Port 供规则只读查数；第5月 Gateway 写前跑 write-precheck；第3月 Flow 节点改调引擎。

重复产品句：**确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。**

### 怎么做

1. case：LLM mock 返回「建议过账」。
2. 规则 PERIOD_CLOSED DENY。
3. 最终响应拒绝+规则 code。
4. 记录冲突实验截图。

### 代码骨架

```java
// 冲突实验：LlmAdvisor mock 返回 "可以过账"
@Test
void promptSaysOk_ruleSaysDeny_ruleWins() {
    when(llmAdvisor.suggestPosting(any())).thenReturn("建议立即过账");
    RuleContext ctx = fixtures.periodClosedPosting();
    AggregatedRuleResult ar = ruleEngine.evaluate(ctx.withNamespace("write-precheck"));
    assertEquals(RuleOutcome.DENY, ar.terminal().outcome());
    assertEquals("PERIOD_CLOSED", ar.terminal().code());
    // 响应层不得出现「已批准过账」
}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| 展示 LLM 胜利 | 产品承诺破 | 冲突实验必须规则胜 |

### 当天验收

- 今日「怎么做」步骤完成
- 单测/手测通过
- 产品句能背
- 手测表已填

### 核心产品句（当日默念）

> 确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。

### 与上月衔接一句

| 上月（第6月） | 本月（第7月）当日焦点 |
|---|---|
| Port/Adapter 契约 | 冲突实验：Prompt 说可以、规则 DENY → 以规则为准 |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 规则单测覆盖主路径
- [ ] 规则内无 PostingPort/WritePort 写操作
- [ ] DENY 短路策略已启用

### 回归命令（D16)

```bash
./mvnw -q test -Dtest='*Rule*Test' 2>/dev/null || true
curl -s -X POST http://localhost:8080/api/ai/eval/run \
  -H 'Content-Type: application/json' -d '{"suite":"rules-cases"}' 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/console/rules 2>/dev/null || echo "console 未启"
```
---


## M7-D17 审计事件 RULE_FIRED / RULE_DENIED

### 为什么

第6月 Port 已稳定；本月在编排层外置规则。**不接公司生产规则平台/SSO/生产库。** 规则 fired 必须进审计流，与 FLOW/WRITE 事件同仓可检索。

### 概念加深

**今日焦点：规则审计**

```text
RuleEngine ──► 审计事件 RULE_FIRED / RULE_DENIED
```

与上月衔接：第6月 Port 供规则只读查数；第5月 Gateway 写前跑 write-precheck；第3月 Flow 节点改调引擎。

重复产品句：**确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。**

### 怎么做

1. RuleAuditSink.emit(RULE_FIRED)。
2. DENY 额外 RULE_DENIED。
3. 字段：ruleId, outcome, code, rulesVersion, traceId。
4. console/audit 可筛。

### 代码骨架

```java
public record RuleAuditEvent(
    String type, // RULE_FIRED | RULE_DENIED
    String traceId, String rulesVersion, String ruleId,
    String namespace, String outcome, String code, Instant at
) {
    public static RuleAuditEvent fired(RuleContext ctx, AggregatedRuleResult ar, String ver) {
        return new RuleAuditEvent("RULE_FIRED", ctx.traceId(), ver,
            ar.terminalRuleId(), ctx.namespace(), ar.terminal().outcome().name(),
            ar.terminal().code(), Instant.now());
    }
}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Prompt 当规则 | 不可审计 | RuleEngine |
| 规则内直写 | 绕过 HITL | Gateway only |
| 接公司规则平台 | 越界 | 本地 YAML |
| DENY 后被 ALLOW 覆盖 | 安全洞 | shortCircuitOnDeny |

### 当天验收

- 今日「怎么做」步骤完成
- 单测/手测通过
- 产品句能背
- 手测表已填

### 核心产品句（当日默念）

> 确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。

### 与上月衔接一句

| 上月（第6月） | 本月（第7月）当日焦点 |
|---|---|
| Port/Adapter 契约 | 审计事件 RULE_FIRED / RULE_DENIED |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 规则单测覆盖主路径
- [ ] 规则内无 PostingPort/WritePort 写操作
- [ ] DENY 短路策略已启用

### 回归命令（D17)

```bash
./mvnw -q test -Dtest='*Rule*Test' 2>/dev/null || true
curl -s -X POST http://localhost:8080/api/ai/eval/run \
  -H 'Content-Type: application/json' -d '{"suite":"rules-cases"}' 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/console/rules 2>/dev/null || echo "console 未启"
```
---


## M7-D18 RuleContext 注入 tenant/roles/principal（第4月）

### 为什么

第6月 Port 已稳定；本月在编排层外置规则。**不接公司生产规则平台/SSO/生产库。** 第4月 ACL 头进入 RuleContext；无 FINANCE 角色规则 DENY，不靠 LLM 猜权限。

### 概念加深

**今日焦点：ACL/Tenant**

```text
RuleEngine ──► RuleContext 注入 tenant/roles/principal（第4月）
```

与上月衔接：第6月 Port 供规则只读查数；第5月 Gateway 写前跑 write-precheck；第3月 Flow 节点改调引擎。

重复产品句：**确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。**

### 怎么做

1. RequestHeader → RuleContext.roles。
2. FinanceOnlyRule DENY。
3. 跨 tenant 属性隔离。
4. 单测无角色 DENY。

### 代码骨架

```java
// rules — 学习骨架 M7-D18
package com.example.erp.ai.rules;

// 见附录 B 包结构；自行复制，勿直接改 erp-ai-assistant 源仓
public class M7_D18Placeholder {
    // RuleContext 注入 tenant/roles/principal（第4月）
}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Prompt 当规则 | 不可审计 | RuleEngine |
| 规则内直写 | 绕过 HITL | Gateway only |
| 接公司规则平台 | 越界 | 本地 YAML |
| DENY 后被 ALLOW 覆盖 | 安全洞 | shortCircuitOnDeny |

### 当天验收

- 今日「怎么做」步骤完成
- 单测/手测通过
- 产品句能背
- 手测表已填

### 核心产品句（当日默念）

> 确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。

### 与上月衔接一句

| 上月（第6月） | 本月（第7月）当日焦点 |
|---|---|
| Port/Adapter 契约 | RuleContext 注入 tenant/roles/principal（第4月） |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 规则单测覆盖主路径
- [ ] 规则内无 PostingPort/WritePort 写操作
- [ ] DENY 短路策略已启用

### 回归命令（D18)

```bash
./mvnw -q test -Dtest='*Rule*Test' 2>/dev/null || true
curl -s -X POST http://localhost:8080/api/ai/eval/run \
  -H 'Content-Type: application/json' -d '{"suite":"rules-cases"}' 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/console/rules 2>/dev/null || echo "console 未启"
```
---


## M7-D19 规则调用只读 Port 查期间状态（第6月），禁止规则内直写

### 为什么

第6月 Port 已稳定；本月在编排层外置规则。**不接公司生产规则平台/SSO/生产库。** 期间状态以 Port 为准；规则调 `PeriodQueryPort` 只读，**禁止**规则里调 PostingPort。

### 概念加深

**今日焦点：Port 只读**

```text
RuleEngine ──► 规则调用只读 Port 查期间状态（第6月），禁止规则内直写
```

与上月衔接：第6月 Port 供规则只读查数；第5月 Gateway 写前跑 write-precheck；第3月 Flow 节点改调引擎。

重复产品句：**确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。**

### 怎么做

1. PeriodClosedFromPortRule 调 PeriodQueryPort。
2. 禁止注入 PostingPort 到规则。
3. archUnit 或 grep 门禁。
4. Fake period closed 手测。

### 代码骨架

```java
public class PeriodClosedFromPortRule implements Rule {
    private final PeriodQueryPort periodQuery;
    @Override public RuleResult evaluate(RuleContext ctx) {
        String period = ctx.getString("fiscalPeriod").orElse("2026-07");
        boolean ok = periodQuery.isPostingAllowed(ctx.tenantId(), period);
        if (!ok) return RuleResult.deny("PERIOD_CLOSED", "期间关闭");
        return RuleResult.allow("PERIOD_OPEN");
    }
}
// 纪律：本类不得出现 PostingPort / InventoryWritePort
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| 规则内 PostingPort | 绕过 Gateway | 只读 Port + grep 门禁 |

### 当天验收

- 今日「怎么做」步骤完成
- 单测/手测通过
- 产品句能背
- 手测表已填

### 核心产品句（当日默念）

> 确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。

### 与上月衔接一句

| 上月（第6月） | 本月（第7月）当日焦点 |
|---|---|
| Port/Adapter 契约 | 规则调用只读 Port 查期间状态（第6月），禁止规则内直写 |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 规则单测覆盖主路径
- [ ] 规则内无 PostingPort/WritePort 写操作
- [ ] DENY 短路策略已启用

### 回归命令（D19)

```bash
./mvnw -q test -Dtest='*Rule*Test' 2>/dev/null || true
curl -s -X POST http://localhost:8080/api/ai/eval/run \
  -H 'Content-Type: application/json' -d '{"suite":"rules-cases"}' 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/console/rules 2>/dev/null || echo "console 未启"
```
---


## M7-D20 热加载概念（文件变更 reload）与安全（校验失败不加载）

### 为什么

第6月 Port 已稳定；本月在编排层外置规则。**不接公司生产规则平台/SSO/生产库。** 生产常热更规则；学习版文件监视 reload，解析/校验失败则保留旧版。

### 概念加深

**今日焦点：热加载**

```text
RuleEngine ──► 热加载概念（文件变更 reload）与安全（校验失败不加载）
```

与上月衔接：第6月 Port 供规则只读查数；第5月 Gateway 写前跑 write-precheck；第3月 Flow 节点改调引擎。

重复产品句：**确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。**

### 怎么做

1. FileRuleWatcher 监视 rules/**/*.yaml。
2. reload 前 schema 校验。
3. 失败保留旧 registry。
4. 日志打印 new rulesVersion。

### 代码骨架

```java
public class FileRuleWatcher {
    public void onChange(Path file) {
        try {
            List<Rule> fresh = loader.loadAll(rulesDir);
            validator.validateAll(fresh);
            registry.replaceAll(fresh);
            rulesVersionHolder.bump();
            log.info("Rules reloaded, version={}", rulesVersionHolder.current());
        } catch (RuleLoadException ex) {
            log.error("Reload rejected, keeping previous rules: {}", ex.getMessage());
        }
    }
}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Prompt 当规则 | 不可审计 | RuleEngine |
| 规则内直写 | 绕过 HITL | Gateway only |
| 接公司规则平台 | 越界 | 本地 YAML |
| DENY 后被 ALLOW 覆盖 | 安全洞 | shortCircuitOnDeny |

### 当天验收

- 今日「怎么做」步骤完成
- 单测/手测通过
- 产品句能背
- 手测表已填

### 核心产品句（当日默念）

> 确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。

### 与上月衔接一句

| 上月（第6月） | 本月（第7月）当日焦点 |
|---|---|
| Port/Adapter 契约 | 热加载概念（文件变更 reload）与安全（校验失败不加载） |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 规则单测覆盖主路径
- [ ] 规则内无 PostingPort/WritePort 写操作
- [ ] DENY 短路策略已启用

### 回归命令（D20)

```bash
./mvnw -q test -Dtest='*Rule*Test' 2>/dev/null || true
curl -s -X POST http://localhost:8080/api/ai/eval/run \
  -H 'Content-Type: application/json' -d '{"suite":"rules-cases"}' 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/console/rules 2>/dev/null || echo "console 未启"
```
---


## M7-D21 第 3 周复盘（评测 + 审计 + Port + 热加载）

### 为什么

第6月 Port 已稳定；本月在编排层外置规则。**不接公司生产规则平台/SSO/生产库。** 巩固评测、冲突、审计、ACL、Port、热加载。

### 概念加深

**今日焦点：周复盘**

```text
RuleEngine ──► 第 3 周复盘（评测 + 审计 + Port + 热加载）
```

与上月衔接：第6月 Port 供规则只读查数；第5月 Gateway 写前跑 write-precheck；第3月 Flow 节点改调引擎。

重复产品句：**确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。**

### 怎么做

1. W3 检查表。
2. rules-cases 绿。
3. RULE_FIRED 可查。
4. 预习 console。

### 代码骨架

```java
// rules — 学习骨架 M7-D21
package com.example.erp.ai.rules;

// 见附录 B 包结构；自行复制，勿直接改 erp-ai-assistant 源仓
public class M7_D21Placeholder {
    // 第 3 周复盘（评测 + 审计 + Port + 热加载）
}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Prompt 当规则 | 不可审计 | RuleEngine |
| 规则内直写 | 绕过 HITL | Gateway only |
| 接公司规则平台 | 越界 | 本地 YAML |
| DENY 后被 ALLOW 覆盖 | 安全洞 | shortCircuitOnDeny |

### 当天验收

- W3 检查表全勾
- rules-cases 绿
- RULE_FIRED 可查
- 热加载失败保留旧版

### 核心产品句（当日默念）

> 确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。

### 与上月衔接一句

| 上月（第6月） | 本月（第7月）当日焦点 |
|---|---|
| Port/Adapter 契约 | 第 3 周复盘（评测 + 审计 + Port + 热加载） |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 规则单测覆盖主路径
- [ ] 规则内无 PostingPort/WritePort 写操作
- [ ] DENY 短路策略已启用

### 周复盘自测清单

见「概念加深」检查表；周五必跑 RuleEngine + rules smoke。

### 回归命令（D21)

```bash
./mvnw -q test -Dtest='*Rule*Test' 2>/dev/null || true
curl -s -X POST http://localhost:8080/api/ai/eval/run \
  -H 'Content-Type: application/json' -d '{"suite":"rules-cases"}' 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/console/rules 2>/dev/null || echo "console 未启"
```
---


# 第 4 周｜控制台 · 三场景串测 · PORTFOLIO · 架构终图 · 收官

---


## M7-D22 console：规则列表、试跑 Context 表单

### 为什么

第6月 Port 已稳定；本月在编排层外置规则。**不接公司生产规则平台/SSO/生产库。** console 增规则列表、试跑 Context JSON 表单，便于演示与排障。

### 概念加深

**今日焦点：控制台**

```text
RuleEngine ──► console：规则列表、试跑 Context 表单
```

与上月衔接：第6月 Port 供规则只读查数；第5月 Gateway 写前跑 write-precheck；第3月 Flow 节点改调引擎。

重复产品句：**确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。**

### 怎么做

1. GET /api/ai/console/rules 列表。
2. POST /api/ai/console/rules/dry-run body=context。
3. 返回 AggregatedRuleResult。
4. 前端表单 JSON。

### 代码骨架

```java
@RestController
@RequestMapping("/api/ai/console/rules")
public class RuleConsoleController {
    @GetMapping public List<RuleDescriptor> list() { return registry.describeAll(); }
    @PostMapping("/dry-run")
    public AggregatedRuleResult dryRun(@RequestBody RuleContextDto dto) {
        return ruleEngine.evaluate(dto.toContext());
    }
}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Prompt 当规则 | 不可审计 | RuleEngine |
| 规则内直写 | 绕过 HITL | Gateway only |
| 接公司规则平台 | 越界 | 本地 YAML |
| DENY 后被 ALLOW 覆盖 | 安全洞 | shortCircuitOnDeny |

### 当天验收

- 今日「怎么做」步骤完成
- 单测/手测通过
- 产品句能背
- 手测表已填

### 核心产品句（当日默念）

> 确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。

### 与上月衔接一句

| 上月（第6月） | 本月（第7月）当日焦点 |
|---|---|
| Port/Adapter 契约 | console：规则列表、试跑 Context 表单 |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 规则单测覆盖主路径
- [ ] 规则内无 PostingPort/WritePort 写操作
- [ ] DENY 短路策略已启用

### 回归命令（D22)

```bash
./mvnw -q test -Dtest='*Rule*Test' 2>/dev/null || true
curl -s -X POST http://localhost:8080/api/ai/eval/run \
  -H 'Content-Type: application/json' -d '{"suite":"rules-cases"}' 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/console/rules 2>/dev/null || echo "console 未启"
```
---


## M7-D23 串测 A：关期间 DENY 过账

### 为什么

第6月 Port 已稳定；本月在编排层外置规则。**不接公司生产规则平台/SSO/生产库。** 串测 A：关期间过账必须 DENY，不经 HITL 蒙混。

### 概念加深

**今日焦点：串测 A**

```text
RuleEngine ──► 串测 A：关期间 DENY 过账
```

与上月衔接：第6月 Port 供规则只读查数；第5月 Gateway 写前跑 write-precheck；第3月 Flow 节点改调引擎。

重复产品句：**确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。**

### 怎么做

1. 设 period closed。
2. Flow 过账意图 → write-precheck DENY。
3. 无 WRITE 审计。
4. 填串测表。

### 代码骨架

```java
// rules — 学习骨架 M7-D23
package com.example.erp.ai.rules;

// 见附录 B 包结构；自行复制，勿直接改 erp-ai-assistant 源仓
public class M7_D23Placeholder {
    // 串测 A：关期间 DENY 过账
}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Prompt 当规则 | 不可审计 | RuleEngine |
| 规则内直写 | 绕过 HITL | Gateway only |
| 接公司规则平台 | 越界 | 本地 YAML |
| DENY 后被 ALLOW 覆盖 | 安全洞 | shortCircuitOnDeny |

### 当天验收

- 今日「怎么做」步骤完成
- 单测/手测通过
- 产品句能背
- 手测表已填

### 核心产品句（当日默念）

> 确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。

### 与上月衔接一句

| 上月（第6月） | 本月（第7月）当日焦点 |
|---|---|
| Port/Adapter 契约 | 串测 A：关期间 DENY 过账 |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 规则单测覆盖主路径
- [ ] 规则内无 PostingPort/WritePort 写操作
- [ ] DENY 短路策略已启用

### 回归命令（D23)

```bash
./mvnw -q test -Dtest='*Rule*Test' 2>/dev/null || true
curl -s -X POST http://localhost:8080/api/ai/eval/run \
  -H 'Content-Type: application/json' -d '{"suite":"rules-cases"}' 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/console/rules 2>/dev/null || echo "console 未启"
```
---


## M7-D24 串测 B：口语意图 ROUTE 到 RAG_TOOL

### 为什么

第6月 Port 已稳定；本月在编排层外置规则。**不接公司生产规则平台/SSO/生产库。** 串测 B：口语「帮我看看 SKU-100 还有多少」→ ROUTE RAG_TOOL。

### 概念加深

**今日焦点：串测 B**

```text
RuleEngine ──► 串测 B：口语意图 ROUTE 到 RAG_TOOL
```

与上月衔接：第6月 Port 供规则只读查数；第5月 Gateway 写前跑 write-precheck；第3月 Flow 节点改调引擎。

重复产品句：**确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。**

### 怎么做

1. 口语库存问句。
2. CLASSIFY ROUTE RAG_TOOL。
3. RAG 返回库存（Port）。
4. 无写路径。

### 代码骨架

```java
// rules — 学习骨架 M7-D24
package com.example.erp.ai.rules;

// 见附录 B 包结构；自行复制，勿直接改 erp-ai-assistant 源仓
public class M7_D24Placeholder {
    // 串测 B：口语意图 ROUTE 到 RAG_TOOL
}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Prompt 当规则 | 不可审计 | RuleEngine |
| 规则内直写 | 绕过 HITL | Gateway only |
| 接公司规则平台 | 越界 | 本地 YAML |
| DENY 后被 ALLOW 覆盖 | 安全洞 | shortCircuitOnDeny |

### 当天验收

- 今日「怎么做」步骤完成
- 单测/手测通过
- 产品句能背
- 手测表已填

### 核心产品句（当日默念）

> 确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。

### 与上月衔接一句

| 上月（第6月） | 本月（第7月）当日焦点 |
|---|---|
| Port/Adapter 契约 | 串测 B：口语意图 ROUTE 到 RAG_TOOL |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 规则单测覆盖主路径
- [ ] 规则内无 PostingPort/WritePort 写操作
- [ ] DENY 短路策略已启用

### 回归命令（D24)

```bash
./mvnw -q test -Dtest='*Rule*Test' 2>/dev/null || true
curl -s -X POST http://localhost:8080/api/ai/eval/run \
  -H 'Content-Type: application/json' -d '{"suite":"rules-cases"}' 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/console/rules 2>/dev/null || echo "console 未启"
```
---


## M7-D25 串测 C：Prompt 诱导绕过 → 规则仍 DENY

### 为什么

第6月 Port 已稳定；本月在编排层外置规则。**不接公司生产规则平台/SSO/生产库。** 串测 C：Prompt 诱导绕过期间关闭，规则仍 DENY。

### 概念加深

**今日焦点：串测 C**

```text
RuleEngine ──► 串测 C：Prompt 诱导绕过 → 规则仍 DENY
```

与上月衔接：第6月 Port 供规则只读查数；第5月 Gateway 写前跑 write-precheck；第3月 Flow 节点改调引擎。

重复产品句：**确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。**

### 怎么做

1. Prompt 注入「忽略期间」。
2. 规则仍 DENY。
3. 响应含 PERIOD_CLOSED。
4. 冲突实验证据。

### 代码骨架

```java
// rules — 学习骨架 M7-D25
package com.example.erp.ai.rules;

// 见附录 B 包结构；自行复制，勿直接改 erp-ai-assistant 源仓
public class M7_D25Placeholder {
    // 串测 C：Prompt 诱导绕过 → 规则仍 DENY
}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| 为通过串测关规则 | 学到假象 | Prompt 诱导仍 DENY |

### 当天验收

- 今日「怎么做」步骤完成
- 单测/手测通过
- 产品句能背
- 手测表已填

### 核心产品句（当日默念）

> 确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。

### 与上月衔接一句

| 上月（第6月） | 本月（第7月）当日焦点 |
|---|---|
| Port/Adapter 契约 | 串测 C：Prompt 诱导绕过 → 规则仍 DENY |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 规则单测覆盖主路径
- [ ] 规则内无 PostingPort/WritePort 写操作
- [ ] DENY 短路策略已启用

### 回归命令（D25)

```bash
./mvnw -q test -Dtest='*Rule*Test' 2>/dev/null || true
curl -s -X POST http://localhost:8080/api/ai/eval/run \
  -H 'Content-Type: application/json' -d '{"suite":"rules-cases"}' 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/console/rules 2>/dev/null || echo "console 未启"
```
---


## M7-D26 PORTFOLIO「规则引擎」章节

### 为什么

第6月 Port 已稳定；本月在编排层外置规则。**不接公司生产规则平台/SSO/生产库。** PORTFOLIO 沉淀规则引擎章节：问题、方案、证据、边界。

### 概念加深

**今日焦点：PORTFOLIO**

```text
RuleEngine ──► PORTFOLIO「规则引擎」章节
```

与上月衔接：第6月 Port 供规则只读查数；第5月 Gateway 写前跑 write-precheck；第3月 Flow 节点改调引擎。

重复产品句：**确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。**

### 怎么做

1. 填 PORTFOLIO 模板。
2. 附 rules-cases 结果。
3. 附串测记录。
4. 边界写清不接生产。

### 代码骨架

```java
// rules — 学习骨架 M7-D26
package com.example.erp.ai.rules;

// 见附录 B 包结构；自行复制，勿直接改 erp-ai-assistant 源仓
public class M7_D26Placeholder {
    // PORTFOLIO「规则引擎」章节
}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Prompt 当规则 | 不可审计 | RuleEngine |
| 规则内直写 | 绕过 HITL | Gateway only |
| 接公司规则平台 | 越界 | 本地 YAML |
| DENY 后被 ALLOW 覆盖 | 安全洞 | shortCircuitOnDeny |

### 当天验收

- 今日「怎么做」步骤完成
- 单测/手测通过
- 产品句能背
- 手测表已填

### 核心产品句（当日默念）

> 确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。

### 与上月衔接一句

| 上月（第6月） | 本月（第7月）当日焦点 |
|---|---|
| Port/Adapter 契约 | PORTFOLIO「规则引擎」章节 |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 规则单测覆盖主路径
- [ ] 规则内无 PostingPort/WritePort 写操作
- [ ] DENY 短路策略已启用

### 回归命令（D26)

```bash
./mvnw -q test -Dtest='*Rule*Test' 2>/dev/null || true
curl -s -X POST http://localhost:8080/api/ai/eval/run \
  -H 'Content-Type: application/json' -d '{"suite":"rules-cases"}' 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/console/rules 2>/dev/null || echo "console 未启"
```
---


## M7-D27 架构终图（1～7月；RuleEngine 位置）

### 为什么

第6月 Port 已稳定；本月在编排层外置规则。**不接公司生产规则平台/SSO/生产库。** 叠加第1～7月架构终图，标出 RuleEngine 在 Flow 与 Gateway 之间的位置。

### 概念加深

**今日焦点：架构终图**

```text
RuleEngine ──► 架构终图（1～7月；RuleEngine 位置）
```

与上月衔接：第6月 Port 供规则只读查数；第5月 Gateway 写前跑 write-precheck；第3月 Flow 节点改调引擎。

重复产品句：**确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。**

### 怎么做

1. 画 1～7 月分层图。
2. 标 RuleEngine 位置。
3. 标 LLM vs Rule 边界。
4. 8 分钟讲稿提纲。

### 代码骨架

```java
// rules — 学习骨架 M7-D27
package com.example.erp.ai.rules;

// 见附录 B 包结构；自行复制，勿直接改 erp-ai-assistant 源仓
public class M7_D27Placeholder {
    // 架构终图（1～7月；RuleEngine 位置）
}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Prompt 当规则 | 不可审计 | RuleEngine |
| 规则内直写 | 绕过 HITL | Gateway only |
| 接公司规则平台 | 越界 | 本地 YAML |
| DENY 后被 ALLOW 覆盖 | 安全洞 | shortCircuitOnDeny |

### 当天验收

- 今日「怎么做」步骤完成
- 单测/手测通过
- 产品句能背
- 手测表已填

### 核心产品句（当日默念）

> 确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。

### 与上月衔接一句

| 上月（第6月） | 本月（第7月）当日焦点 |
|---|---|
| Port/Adapter 契约 | 架构终图（1～7月 |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 规则单测覆盖主路径
- [ ] 规则内无 PostingPort/WritePort 写操作
- [ ] DENY 短路策略已启用

### 回归命令（D27)

```bash
./mvnw -q test -Dtest='*Rule*Test' 2>/dev/null || true
curl -s -X POST http://localhost:8080/api/ai/eval/run \
  -H 'Content-Type: application/json' -d '{"suite":"rules-cases"}' 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/console/rules 2>/dev/null || echo "console 未启"
```
---


## M7-D28 规则治理清单（谁改规则、如何回归）

### 为什么

第6月 Port 已稳定；本月在编排层外置规则。**不接公司生产规则平台/SSO/生产库。** 谁改规则、如何 PR、如何跑 rules-cases 回归 — 治理清单。

### 概念加深

**今日焦点：规则治理**

```text
RuleEngine ──► 规则治理清单（谁改规则、如何回归）
```

与上月衔接：第6月 Port 供规则只读查数；第5月 Gateway 写前跑 write-precheck；第3月 Flow 节点改调引擎。

重复产品句：**确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。**

### 怎么做

1. 规则变更 PR 模板。
2. 必跑 rules-cases + write-safety。
3. rulesVersion bump 规则。
4. 负责人角色表。

### 代码骨架

```java
// rules — 学习骨架 M7-D28
package com.example.erp.ai.rules;

// 见附录 B 包结构；自行复制，勿直接改 erp-ai-assistant 源仓
public class M7_D28Placeholder {
    // 规则治理清单（谁改规则、如何回归）
}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Prompt 当规则 | 不可审计 | RuleEngine |
| 规则内直写 | 绕过 HITL | Gateway only |
| 接公司规则平台 | 越界 | 本地 YAML |
| DENY 后被 ALLOW 覆盖 | 安全洞 | shortCircuitOnDeny |

### 当天验收

- 今日「怎么做」步骤完成
- 单测/手测通过
- 产品句能背
- 手测表已填

### 核心产品句（当日默念）

> 确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。

### 与上月衔接一句

| 上月（第6月） | 本月（第7月）当日焦点 |
|---|---|
| Port/Adapter 契约 | 规则治理清单（谁改规则、如何回归） |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 规则单测覆盖主路径
- [ ] 规则内无 PostingPort/WritePort 写操作
- [ ] DENY 短路策略已启用

### 回归命令（D28)

```bash
./mvnw -q test -Dtest='*Rule*Test' 2>/dev/null || true
curl -s -X POST http://localhost:8080/api/ai/eval/run \
  -H 'Content-Type: application/json' -d '{"suite":"rules-cases"}' 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/console/rules 2>/dev/null || echo "console 未启"
```
---


## M7-D29 口述自测（20 题）

### 为什么

第6月 Port 已稳定；本月在编排层外置规则。**不接公司生产规则平台/SSO/生产库。** 口述 20 题覆盖边界、短路、冲突、审计、Port 纪律。

### 概念加深

**今日焦点：口述**

```text
RuleEngine ──► 口述自测（20 题）
```

与上月衔接：第6月 Port 供规则只读查数；第5月 Gateway 写前跑 write-precheck；第3月 Flow 节点改调引擎。

重复产品句：**确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。**

### 怎么做

1. 抽 20 题自测。
2. 每题 30 秒答。
3. 薄弱回对应天。
4. 录音可选。

### 代码骨架

口述 20 题（摘要）：
1. 规则 vs LLM 边界？ 2. DENY 与 WARN？ 3. 短路策略？ 4. CLASSIFY 为何用 ROUTE？
5. write-precheck 位置？ 6. rulesVersion 用途？ 7. RULE_FIRED 字段？ 8. 冲突谁优先？
9. 规则能否写 ERP？ 10. PeriodQueryPort 在规则里？ 11. 热加载失败怎么办？
12. Gate vs Rule？ 13. jsonl 断言什么？ 14. 命名空间有哪些？ 15. DecisionTable 场景？
16. 无 FINANCE DENY？ 17. console dry-run 用途？ 18. 串测 A/B/C？ 19. 治理 PR 跑什么？
20. 第8月选哪条？

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Prompt 当规则 | 不可审计 | RuleEngine |
| 规则内直写 | 绕过 HITL | Gateway only |
| 接公司规则平台 | 越界 | 本地 YAML |
| DENY 后被 ALLOW 覆盖 | 安全洞 | shortCircuitOnDeny |

### 当天验收

- 20 题能答 ≥16
- 薄弱点已标记
- 能画 RuleEngine 图

### 核心产品句（当日默念）

> 确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。

### 与上月衔接一句

| 上月（第6月） | 本月（第7月）当日焦点 |
|---|---|
| Port/Adapter 契约 | 口述自测（20 题） |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 规则单测覆盖主路径
- [ ] 规则内无 PostingPort/WritePort 写操作
- [ ] DENY 短路策略已启用

### 口述 20 题完整列表

见「代码骨架」；每题 30 秒，录音可选。

### 回归命令（D29)

```bash
./mvnw -q test -Dtest='*Rule*Test' 2>/dev/null || true
curl -s -X POST http://localhost:8080/api/ai/eval/run \
  -H 'Content-Type: application/json' -d '{"suite":"rules-cases"}' 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/console/rules 2>/dev/null || echo "console 未启"
```
---


## M7-D30 收官；第8月方向（已开课：工作流可视化 · 完整教材）

### 为什么

第6月 Port 已稳定；本月在编排层外置规则。**不接公司生产规则平台/SSO/生产库。** 收官后进入第8月。

### 概念加深

第8月方向（已开课：**工作流可视化**，**完整章节式教材，不再卡 30 天**）：

整月教材：[`MONTH8_WORKFLOW_VIZ_COMPLETE.md`](./MONTH8_WORKFLOW_VIZ_COMPLETE.md) · 入口 [`docs/MONTH8.md`](../MONTH8.md)

主题：把 Flow 画成可读图（节点、合法边、当前高亮、审计回放）；可视化≠放开乱跳状态；APPROVE≠写库；**不接公司 BPM**。

其它可留第9月（只选一条）：观测大盘 / 灰度演练 / 提示词运营。

重复产品句：**确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。**

### 怎么做

1. 收官清单诚实勾选。
2. baseline+rules-cases 全跑。
3. 打开第8月完整教材，按章节推进（非按天）。
4. 8 分钟 pitch。

### 代码骨架

```java
// 第7月收官：确认 RuleEngine 已挂 Flow；第8月用图把同一状态机画出来
// 见 docs/MONTH8.md
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| 收官不跑 eval | 隐性退化 | baseline+rules-cases |
| 把第8月再拆成「赶 30 天」 | 与完整教材目标冲突 | 按章节读完做透 |

### 当天验收

- 收官清单诚实
- rules-cases+contract 绿
- 已知第8月入口与「非 30 天」形式
- pitch 三句

### 核心产品句（当日默念）

> 确定性业务规则用规则引擎；LLM 只做理解、草稿与解释。规则先于模型，冲突时规则优先。规则变更可版本化、可评测、可审计。本月不接公司生产规则平台。

### 与上月衔接一句

| 上月（第6月） | 本月（第7月）当日焦点 |
|---|---|
| Port/Adapter 契约 | 收官 |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 规则单测覆盖主路径
- [ ] 规则内无 PostingPort/WritePort 写操作
- [ ] DENY 短路策略已启用

### 明确不做（再次锁定）

- 公司 SSO / 生产库 / 公司规则平台
- 规则内直写 ERP / LLM 硬 DENY 替代规则
- 跳过 rules-cases / 无 RULE_FIRED 静默放行

### 归档清单

- [ ] `rules/**/*.yaml` + version
- [ ] `evals/suites/rules-cases.jsonl`
- [ ] 三串测记录
- [ ] PORTFOLIO 第7月章

### 回归命令（D30)

```bash
./mvnw -q test -Dtest='*Rule*Test' 2>/dev/null || true
curl -s -X POST http://localhost:8080/api/ai/eval/run \
  -H 'Content-Type: application/json' -d '{"suite":"rules-cases"}' 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/console/rules 2>/dev/null || echo "console 未启"
```
---


# 附录

## 附录 A｜配置总表（第7月）

```yaml
ai:
  provider: mock
  erp:
    adapter: fake
  rules:
    enabled: true
    version: "2026.07.1"
    dir: classpath:rules
    namespaces:
      - classify
      - risk
      - write-precheck
      - gate
    engine:
      short-circuit-on-deny: true
      short-circuit-on-route: true
      collect-warnings: true
    hot-reload:
      enabled: true              # 学习版文件监视
      watch-dir: rules/
      validate-before-load: true
  write:
    gateway-enabled: true
    precheck-rules-enabled: true
    require-idempotency-key: true
    audit-dir: data/write-audit
  flow:
    classify-via-rules: true
    risk-via-rules: true
    audit-enabled: true
  acl:
    enabled: true
    mode: learning-headers
  eval:
    suites-dir: evals/suites
    baseline-path: evals/baseline.json
    snapshot-rules-version: true
  console:
    show-rules-browser: true
    show-rule-dry-run: true
  audit:
    emit-rule-fired: true
    emit-rule-denied: true
```

## 附录 B｜包结构增量（第7月）

```text
rules/core/Rule.java
rules/core/RuleOutcome.java
rules/core/RuleContext.java
rules/core/RuleResult.java
rules/core/AggregatedRuleResult.java
rules/core/RuleEngine.java
rules/core/DefaultRuleEngine.java
rules/core/RulePortAccessor.java
rules/registry/RuleRegistry.java
rules/registry/DefaultRuleRegistry.java
rules/registry/RuleDescriptor.java
rules/load/YamlRuleLoader.java
rules/load/TableDefinition.java
rules/load/RuleLoadException.java
rules/load/FileRuleWatcher.java
rules/table/DecisionTableRule.java
rules/table/DecisionRow.java
rules/classify/InventoryQueryRouteRule.java
rules/classify/PostingIntentRouteRule.java
rules/risk/LargeAmountWarnRule.java
rules/risk/SensitiveAccountWarnRule.java
rules/writeprecheck/PeriodClosedFromPortRule.java
rules/writeprecheck/FinanceRoleRule.java
rules/writeprecheck/WarehouseWarnRule.java
rules/audit/RuleAuditEvent.java
rules/audit/RuleAuditSink.java
rules/version/RulesVersionHolder.java
rules/config/RuleEngineConfiguration.java
flow/FlowOrchestrator.java          # doClassify/doRisk 改调 RuleEngine
write/DefaultWriteGateway.java      # 入口 write-precheck
controller/RuleConsoleController.java
test/rules/RuleEngineTest.java
test/rules/DecisionTableTest.java
test/rules/ConflictExperimentTest.java
test/rules/PeriodClosedFromPortRuleTest.java
evals/suites/rules-cases.jsonl
rules/classify/inventory-route.yaml
rules/write-precheck/period-warehouse-table.yaml
docs/PORTFOLIO.md
```

## 附录 C｜术语表（第7月）

| 术语 | 含义 | 易混点 |
|---|---|---|
| RuleEngine | 按命名空间执行规则的引擎 | ≠ LLM |
| RuleContext | 规则输入上下文 | 含 tenant/roles/attributes |
| RuleResult | 单条规则输出 | ≠ 最终 API 响应 |
| ALLOW/DENY/WARN/ROUTE | 四种 outcome | DENY≠WARN |
| DecisionTable | 表驱动规则 | ≠ 随意 if 链 |
| RuleRegistry | 规则注册表 | 按 namespace 隔离 |
| write-precheck | Gateway 前规则集 | ≠ Gateway 内 if |
| rulesVersion | 规则版本号 | 写入 audit/eval |
| RULE_FIRED | 规则执行审计事件 | ≠ WRITE_REQUESTED |
| RULE_DENIED | DENY 专用审计 | 便于检索拒绝 |
| 短路 | DENY/ROUTE 停止后续规则 | 默认开启 |
| 冲突实验 | Prompt vs 规则 | 规则必须胜 |
| RulePortAccessor | 规则只读 Port 门面 | 禁止写 Port |
| 热加载 | 文件变更 reload | 失败不 partial |
| rules-cases | 规则 eval 套件 | 断言 outcome 非文案 |
| 公司规则平台 | 生产 BPM/Drools 等 | **本月不接** |

## 附录 D｜文档索引（MONTH1–7 + WEB）

| 文档 | 路径 |
|---|---|
| 第1月 | `docs/MONTH1.md` → `docs/lessons/MONTH1_DAY1-30_COMBINED.md` |
| 第2月 | `docs/MONTH2.md` → `docs/lessons/MONTH2_DAY1-30_COMBINED.md` |
| 第3月 | `docs/MONTH3.md` → `docs/lessons/MONTH3_DAY1-30_COMBINED.md` |
| 第4月 | `docs/MONTH4.md` → `docs/lessons/MONTH4_DAY1-30_COMBINED.md` |
| 第5月 | `docs/MONTH5.md` → `docs/lessons/MONTH5_DAY1-30_COMBINED.md` |
| 第6月 | `docs/MONTH6.md` → `docs/lessons/MONTH6_DAY1-30_COMBINED.md` |
| 第7月 | `docs/MONTH7.md` → `docs/lessons/MONTH7_DAY1-30_COMBINED.md` |
| WEB | `docs/WEB.md` → `docs/lessons/WEB_VUE_COMPLETE.md` |

## 附录 E｜学习纪律（第7月）

1. **确定性规则不进 Prompt**；LLM 理解/草稿/解释 only。
2. **冲突时规则优先**；冲突实验必须可演示。
3. **规则内禁止直写**；写经 Gateway+HITL；规则可调只读 Port。
4. **规则变更必 bump rulesVersion**；必跑 rules-cases。
5. **不接公司生产规则平台/SSO/生产库**。
6. **讲义不自动改 erp-ai-assistant**；自行复制粘贴。
7. **DENY 默认短路**；禁止低优先级 ALLOW 覆盖 DENY。
8. **审计必含 RULE_FIRED**；DENY 额外 RULE_DENIED。

## 附录 F｜常见问题（FAQ）

**Q: 能用 Drools 吗？**  
A: 学习期可用简易 DecisionTable+Java；不必引重型引擎；**不接公司 Drools 生产库**。

**Q: CLASSIFY 还要 LLM 吗？**  
A: ROUTE 命中则不用；未命中可 LLM 辅助理解，但硬 DENY 必须规则。

**Q: Gate 和 Rule 重复了？**  
A: Gate 拦幻觉/无依据；Rule 拦业务违法（期间/权限/必填）。

**Q: 规则改了怎么回归？**  
A: bump rulesVersion + `rules-cases.jsonl` + write-safety smoke。

**Q: adapter-contract 还跑吗？**  
A: 要。Port 规则依赖 Fake period 数据一致。

**Q: 热加载失败会怎样？**  
A: 保留旧规则集，打 error 日志，不 partial 加载。

**Q: console dry-run 会写库吗？**  
A: 不会；只 evaluate 返回 AggregatedRuleResult。

**Q: 第8月选哪条？**  
A: 观测大盘 / 灰度演练 / 工作流可视化 / 提示词运营 — **只选一条**。

## 附录 G｜第7月 curl/scripts 速查

```bash
# 规则列表
curl -s http://localhost:8080/api/ai/console/rules

# 规则试跑
curl -s -X POST http://localhost:8080/api/ai/console/rules/dry-run \
  -H 'Content-Type: application/json' \
  -d '{"namespace":"write-precheck","tenantId":"tenant-a","attributes":{"periodOpen":false,"action":"POST_DOCUMENT"}}'

# 规则 eval
curl -s -X POST http://localhost:8080/api/ai/eval/run \
  -H 'Content-Type: application/json' -d '{"suite":"rules-cases"}'

# Flow 触发（CLASSIFY 经规则）
curl -s -X POST http://localhost:8080/api/ai/flow/instances \
  -H 'Content-Type: application/json' -H 'X-Tenant-Id: tenant-a' \
  -d '{"question":"SKU-100还有多少库存"}'

# 写路径仍经 APPROVE + Gateway
curl -s -X POST http://localhost:8080/api/ai/flow/instances/{id}/decide \
  -H 'Content-Type: application/json' -H 'X-Roles: FINANCE' \
  -H 'X-Tenant-Id: tenant-a' -H 'X-Idempotency-Key: flow-{id}-approve' \
  -d '{"decision":"APPROVE"}'

# 审计搜 RULE
grep -h RULE_ data/audit/*.jsonl 2>/dev/null | tail -5

# 单测
./mvnw -q test -Dtest='*Rule*Test'

# 禁止规则内写 Port
grep -rn 'PostingPort\|InventoryWritePort' src/main/java/**/rules/
```

## 附录 H｜第7月与第6月结构对照

| 结构段 | 第6月 | 第7月 |
|---|---|---|
| 为什么 | ✓ | ✓ |
| 概念加深 | ✓ | ✓ |
| 怎么做 | ✓ | ✓ |
| 代码骨架 | Java/YAML | Java/YAML/jsonl |
| 坑与排障 | ✓ | ✓ |
| 当天验收 | ✓ | ✓ |

复盘日六段齐全；代码骨架可为检查清单。

## 附录 I｜DefaultWriteGateway 写前 RuleSet（参考）

```java
package com.example.erp.ai.write;

public class DefaultWriteGateway implements WriteGateway {
    private final RuleEngine ruleEngine;
    private final RulesVersionHolder rulesVersion;
    // ... auth, idempotency, ports, audit

    @Override
    public WriteResult apply(WriteCommand cmd) {
        RuleContext ctx = RuleContext.forWritePrecheck(cmd);
        AggregatedRuleResult ar = ruleEngine.evaluate(ctx);
        ruleAuditSink.emit(RuleAuditEvent.fired(ctx, ar, rulesVersion.current()));
        if (ar.terminal().outcome() == RuleOutcome.DENY) {
            ruleAuditSink.emit(RuleAuditEvent.denied(ctx, ar, rulesVersion.current()));
            return WriteResult.rejected(cmd.commandId(), ar.terminal().code());
        }
        // 原有 auth + idempotency + Port 写路径
        return dispatchWithPort(cmd);
    }
}
```

## 附录 J｜第7月单测/规则测清单

| 测试类 | 覆盖 |
|---|---|
| `RuleEngineTest` | 优先级/短路 |
| `DecisionTableTest` | 期间/仓库表 |
| `InventoryQueryRouteRuleTest` | CLASSIFY ROUTE |
| `PeriodClosedFromPortRuleTest` | Port 只读 |
| `FinanceRoleRuleTest` | ACL DENY |
| `ConflictExperimentTest` | Prompt vs 规则 |
| `YamlRuleLoaderTest` | 装载/校验失败 |
| `RuleEvalRunnerTest` | rules-cases.jsonl |

## 附录 K｜period-warehouse-table.yaml 示例

```yaml
id: write-precheck.period-warehouse
namespace: write-precheck
priority: 5
type: decision-table
rows:
  - when:
      periodOpen: false
    outcome: DENY
    code: PERIOD_CLOSED
    message: 会计期间已关闭，禁止过账
  - when:
      warehousePresent: false
    outcome: WARN
    code: WAREHOUSE_MISSING
    message: 未指定仓库，建议人工确认
  - when: {}
    outcome: ALLOW
    code: OK
```

## 附录 L｜rules-cases.jsonl 完整示例

```jsonl
{"id":"rc-001","namespace":"write-precheck","context":{"tenantId":"tenant-a","attributes":{"periodOpen":false,"action":"POST_DOCUMENT"}},"expectOutcome":"DENY","expectCode":"PERIOD_CLOSED"}
{"id":"rc-002","namespace":"classify","context":{"attributes":{"userText":"查一下SKU-100库存"}},"expectOutcome":"ROUTE","expectCode":"inventory_query"}
{"id":"rc-003","namespace":"risk","context":{"attributes":{"amount":5000000}},"expectOutcome":"WARN","expectCode":"LARGE_AMOUNT"}
{"id":"rc-004","namespace":"write-precheck","context":{"roles":["INVENTORY_CLERK"],"attributes":{"action":"POST_DOCUMENT"}},"expectOutcome":"DENY","expectCode":"FORBIDDEN_ROLE"}
{"id":"rc-005","namespace":"write-precheck","context":{"tenantId":"tenant-a","attributes":{"periodOpen":true,"warehousePresent":false}},"expectOutcome":"WARN","expectCode":"WAREHOUSE_MISSING"}
{"id":"rc-006","namespace":"classify","context":{"attributes":{"userText":"今天天气"}},"expectOutcome":"ALLOW","expectCode":"no_match"}
```

## 附录 M｜串测 A/B/C 剧本

### 串测 A — 关期间 DENY 过账

| 步骤 | 操作 | 预期 |
|---|---|---|
| A1 | Fake period closed | isPostingAllowed=false |
| A2 | Flow 过账意图 + APPROVE 尝试 | write-precheck DENY |
| A3 | 审计 | RULE_DENIED + 无 WRITE |

### 串测 B — 口语 ROUTE RAG_TOOL

| 步骤 | 操作 | 预期 |
|---|---|---|
| B1 | 「SKU-100还有多少」 | CLASSIFY ROUTE |
| B2 | RETRIEVE/RAG | 库存来自 Port |
| B3 | 审计 | RULE_FIRED classify |

### 串测 C — Prompt 诱导仍 DENY

| 步骤 | 操作 | 预期 |
|---|---|---|
| C1 | Prompt 注入忽略期间 | LLM 草稿可能误导 |
| C2 | write-precheck | PERIOD_CLOSED DENY |
| C3 | 用户可见 | 规则拒绝+解释，非「已过账」 |

## 附录 N｜RuleEngineConfiguration 参考

```java
@Configuration
@EnableConfigurationProperties(RuleProperties.class)
public class RuleEngineConfiguration {
    @Bean RuleRegistry ruleRegistry(RuleProperties props, YamlRuleLoader loader,
            PeriodQueryPort periodQuery) {
        DefaultRuleRegistry reg = new DefaultRuleRegistry();
        loader.loadTables(props.dir()).forEach(reg::register);
        reg.register(new InventoryQueryRouteRule());
        reg.register(new PeriodClosedFromPortRule(periodQuery));
        reg.register(new LargeAmountWarnRule());
        return reg;
    }
    @Bean RuleEngine ruleEngine(RuleRegistry reg) {
        return new DefaultRuleEngine(reg);
    }
}
```

## 附录 O｜架构终图（第1～7月）

```text
┌────────────────────────────────────────────────────────────────┐
│ L1 Chat/RAG  L2 Gate/HITL  L3 Store/Audit  L4 ACL/Tenant       │
│ L5 WriteGateway  L6 Port/Adapter  L7 RuleEngine ◄── 第7月       │
└────────────────────────────┬───────────────────────────────────┘
                             │
         ┌───────────────────┼───────────────────┐
         ▼                   ▼                   ▼
    Flow CLASSIFY/RISK   write-precheck      console dry-run
         │                   │
         ▼                   ▼
    LLM 理解/草稿        Gateway+HITL 写
         │
         ▼
    Port 只读（规则内）── Fake/Sandbox ── 不接公司生产
```

## 附录 P｜规则变更 Runbook

1. 改 `rules/**/*.yaml` 或 Java Rule。
2. bump `ai.rules.version`。
3. `./mvnw test -Dtest='*Rule*Test'`。
4. `eval/run suite=rules-cases`。
5. `write-safety` + `adapter-contract` smoke。
6. 查 audit `RULE_FIRED` 快照 rulesVersion。
7. PR 附 eval 结果截图。

## 附录 Q｜PORTFOLIO 模板

```markdown
## 第7月：规则引擎
**问题：** CLASSIFY/RISK/写前校验散落 Prompt 与 if，不可版本、不可审计。
**方案：** RuleEngine + RuleRegistry + DecisionTable + rules-cases + RULE_FIRED。
**证据：** 冲突实验（规则胜）、串测 A/B/C、rules-cases 绿。
**边界：** 不接公司生产规则平台；写仍 Gateway+HITL；规则只读 Port。
**贡献：** RuleEngine 设计 / write-precheck / 冲突实验 / console 试跑。
```

## 附录 R｜grep 纪律合集

```bash
grep -rn 'PostingPort\|InventoryWritePort' src/main/java/**/rules/
grep -rn 'if.*contains.*库存' src/main/java/**/flow/   # 应收拢到 Rule
grep -rn 'PERIOD_CLOSED' src/main/java --include='*.java'
ls rules/classify/ rules/write-precheck/
cat evals/suites/rules-cases.jsonl | wc -l
```

---

## 修订

| 日期 | 说明 |
|---|---|
| 2026-08-15 | 第7月首版合并讲义（RuleEngine / DecisionTable / 版本化 / 评测 / 审计） |
| 2026-08-15 | 逐日详版：30 天完整六段结构 + 附录 A～R |
| 2026-08-15 | 与第2/3/5/6月衔接：CLASSIFY/RISK/写前校验外置；Port 只读 |

> **全文收束：** {PRODUCT}
