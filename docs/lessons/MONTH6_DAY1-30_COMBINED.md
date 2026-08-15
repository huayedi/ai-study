# 第 6 个月合并讲义（Day1～Day30）【逐日详版 · 与第1月同级 · 非概述】

> **定位：** 纯学习；通用 ERP 教材口径；**不接公司生产库、不接 SSO、不直连真实账套/过账接口。**
> **形式：** 与第 1 月 **同等详细**；本文件为 30 天正文合订，**不是缩写版大纲。** 可复制代码均在本文；由你自行粘贴改造到 `erp-ai-assistant`。
> **前置：** 第1月 Chat/Prompt/RAG + 第2月 Hybrid/Gate/Rerank、Flow HITL、Eval 入门 + 第3月 Store/reindex/audit/baseline + 第4月 ACL/反馈/多租户/console + 第5月 WriteGateway/假账本/受控写入。
> **每天结构（固定六段）：** 为什么 → 概念加深 → 怎么做 → 代码骨架 → 坑与排障 → 当天验收。
> **入口：** `docs/MONTH6.md`
> **技术节点：** 本月对齐 **T10（Port/Adapter）；可选 WireMock/Testcontainers** · 逐日前置见各 Day 开头 · 总图 [TECH_ROADMAP](../TECH_ROADMAP.md)  
> **建议视频（本月）：** MONTH6：多搜「六边形/Port Adapter/WireMock」；链接触发少 · 逐日见各 Day/章「建议视频」· 总表 [BILIBILI.md](../BILIBILI.md)
> **本月主题：** **真适配器接口稳定化（Port/Adapter 契约 + Fake/Sandbox + 契约测试）**
> **核心产品句（全文反复强调）：**
> **业务代码只依赖稳定 Port/Adapter 接口；学习期用 Fake/Sandbox 实现；将来换真实 ERP 只换适配器，不改 Chat/RAG/Flow/WriteGateway 主编排。本月不接公司生产。**

---

## 第 6 月总目标（学完应能对外讲 8～10 分钟）

1. **Port/Adapter 心智：** Domain ← Port（接口）← Adapter（实现）；业务层禁止拼 ERP URL。
2. **读写端口拆分：** `ItemQueryPort` / `InventoryQueryPort` / `PeriodQueryPort`；`InventoryWritePort` / `PostingPort`。
3. **统一错误模型：** `ErpAdapterException` + `TIMEOUT` / `CONFLICT` / `PERIOD_CLOSED` / `UNAUTHORIZED` / `VALIDATION` / `UNAVAILABLE`。
4. **配置切换：** `ai.erp.adapter: fake|sandbox|http-stub`；业务代码无 `if(adapter==)`。
5. **契约测试：** 同一套测试跑 Fake 与 Stub，断言行为一致；OpenAPI 最小片段。
6. **韧性：** 超时/重试/幂等/熔断/健康检查在适配器层对齐。
7. **收官：** fake→sandbox 切换演练；三场景串测；PORTFOLIO「适配器稳定化」；架构终图（第1～6月）。

### 和第 1～5 月的关系

```text
第1月  会生成、会 RAG、懂 Prompt / few-shot
第2月  会 Hybrid/Gate、HITL 最小流、eval 入门
第3月  会持久化/重建、审计回放、baseline 门禁
第4月  会 ACL/租户/反馈/console 演示
第5月  会受控写入：Gateway + 假账本 + APPROVE 后才写 + 审计
第6月  会把第5月 InMemory 绑死升级为 Port 契约 + Fake/Sandbox 适配器
         ↑ 从「能写假账」升级到「换 ERP 不换编排」
```

### 能力对照（第5月末 → 第6月末）

| 能力 | 第5月末常见状态 | 第6月末目标 |
|---|---|---|
| 账本依赖 | `DefaultWriteGateway` 直绑 `InMemoryInventoryLedger` | Gateway 依赖 `InventoryWritePort` / `PostingPort` |
| 查询 | Service 可能散落假数据 | 统一 `*QueryPort` + Fake/Sandbox |
| 错误 | 各层自定义异常 | `ErpAdapterException` + ERP 码映射 |
| 切换 ERP | 改业务代码 | 只换 Adapter + 配置 |
| 测试 | 单元测 InMemory | 契约测 Fake vs Stub 行为一致 |
| 韧性 | 部分在 Flow | 超时/重试/熔断在 Adapter 层 |
| 可观测 | 写入审计 | outbound requestId/latency/errorCode |

### 四周路线图

| 周 | Day | 主题 | 结束产出 |
|---|---|---|---|
| 1 | M6-D1～7 | Port/Adapter 心智；读写端口；错误模型；配置切换 | Query/Write Port 接口 + FakeAdapter + 装配 |
| 2 | M6-D8～14 | 契约测试；版本；超时重试幂等；熔断；健康检查 | ContractTest + CircuitBreaker 骨架 + `/health` |
| 3 | M6-D15～21 | SandboxHttpAdapter；OpenAPI；字段映射；ACL 头透传 | Stub server + Mapper + 写路径 E2E |
| 4 | M6-D22～30 | 可观测；切换演练；串测；PORTFOLIO；收官 | 三场景 + 架构终图 + 口述 20 题 |

### 本月编码策略

| 周 | 深挖建议 | 其余 |
|---|---|---|
| 1 | Port 接口 + FakeAdapter + Spring 装配 | 读懂第5月 Gateway 改造点 |
| 2 | ContractTest + 重试/熔断骨架 | 版本头概念读懂即可 |
| 3 | SandboxHttpAdapter + ErpItemMapper | WireMock 文档级亦可 |
| 4 | 切换演练 + 三场景 + PORTFOLIO | D29 口述必做 |

> **纪律：** 业务代码只依赖 Port；**禁止** Service 里 `RestTemplate` 拼 ERP URL；**禁止** `if (adapterType == FAKE)` 散落；**禁止** 接公司生产库。

### 固定回归题（本月每天都应能跑或口述）

| 套件 | 用途 | 建议频率 |
|---|---|---|
| `adapter-contract.jsonl` | Fake vs Stub 行为一致 | 第2周后每日 smoke |
| `write-safety.jsonl` | 写路径安全（延续第5月） | 每周 |
| `acl-forbidden.jsonl` | 读路径越权（延续） | 每周 |
| `baseline` + 核心 rag suite | 防退化 | 每周五 + 收官日 |

### 学习头速查（全月通用）

```text
X-User-Id:         demo-user-01
X-Roles:           FINANCE,INVENTORY_CLERK
X-Tenant-Id:       tenant-a
X-Idempotency-Key: <写路径强烈建议>
X-Trace-Id:        <可选>
X-Request-Id:      <适配器 outbound 关联>
Accept:            application/vnd.erp-ai.v1+json
X-Erp-Api-Version: v1
```

### 周里程碑检查表（建议周五自评）

| 周末 | 必须能演示 | 建议 eval |
|---|---|---|
| 第1周末 | Gateway 经 Port 调 Fake 改库存 | Port 接口 + 装配 yml |
| 第2周末 | ContractTest 绿（Fake=Stub） | adapter-contract |
| 第3周末 | Sandbox 写路径 E2E | OpenAPI 片段对齐 |
| 第4周末 | fake→sandbox 一键切换 + 三场景 | baseline + contract |

### 与第5月 WriteGateway 对照

| 纪律 | 第5月起 | 第6月新增 |
|---|---|---|
| 写入只经 Gateway | ✓ | Gateway 依赖 Port，不绑 InMemory |
| APPROVE 才写 | ✓ | PostingPort(Sandbox) 同样纪律 |
| 幂等键 | ✓ | Idempotency-Key 透传到 Adapter HTTP |
| 审计 | ✓ | Adapter 日志含 requestId/latency |
| 不接生产 | ✓ | Fake/Sandbox only |

### 与第5月 WriteGateway/Ledger/Posting 迁移说明

第5月 `DefaultWriteGateway` 典型依赖：

```text
WriteGateway → InMemoryInventoryLedger
            → InMemoryPostingService
            → PostingPeriodGuard
```

第6月目标依赖：

```text
WriteGateway → InventoryWritePort  → FakeInventoryWriteAdapter → (内部 InMemory)
            → PostingPort          → FakePostingAdapter / SandboxPostingAdapter
            → PeriodQueryPort      → FakePeriodAdapter
```

**关键：** InMemory 可继续存在，但**躲在 Adapter 实现内**；主编排只见 Port。

### HTTP 路径 × Port 对照（全月贴墙）

| 路径 | Port | 写？ |
|---|---|---|
| `/api/ai/rag/ask` | InventoryQueryPort（可选） | 否 |
| `/api/ai/items/{sku}` | ItemQueryPort | 否 |
| Flow APPROVE | PostingPort 经 Gateway | 是 |
| `/api/ai/erp/health` | HealthIndicator | 否 |

### Fake vs Sandbox 契约对齐（全月）

| 场景 | Fake | Sandbox | 契约 id |
|---|---|---|---|
| 查 SKU-100 | qty=10 | qty=10 | ac-001 |
| 关期间过账 | PERIOD_CLOSED | PERIOD_CLOSED | ac-002 |
| 重复幂等键 | DUPLICATE | DUPLICATE | ac-003 |

### Port/Adapter ASCII 总图（全月锚点）

```text
  Chat / RAG / Flow / WriteGateway（主编排 — 本月不改职责）
         │
         ▼ 只依赖 Port 接口（稳定契约）
  ┌──────────────────────────────────────┐
  │ ItemQueryPort   InventoryQueryPort   │
  │ PeriodQueryPort InventoryWritePort    │
  │ PostingPort     MasterDataQueryPort   │
  └──────────────────────────────────────┘
         ▲                    ▲
         │                    │
   FakeInventoryAdapter   SandboxHttpAdapter
   (学习默认 in-memory)    (本地 stub HTTP)
         │                    │
         ▼                    ▼
   内存 Map / 假账         JDK HttpServer / WireMock 文档级
   （不接公司生产）         （仍不接公司生产）
```

---

# 第 1 周｜Port/Adapter 心智 · 读写端口 · 错误模型 · 配置切换

---

## M6-D1 为何「接口不稳」比「模型不行」更毁项目；本月边界

> **技术前置：** 此时应当学会 **第5月 WriteGateway/假账本/HITL 写（T9）** 后再进行阅读。 节点：**T9→T10** · [TECH_ROADMAP](../TECH_ROADMAP.md)
> **建议视频：** B 站搜 `防腐层 ACL Anti-Corruption` · 接口不稳危害 · 总表 [BILIBILI.md](../BILIBILI.md)




### 为什么

第5月已能经 WriteGateway 写假账，但 `DefaultWriteGateway` 直绑 `InMemoryInventoryLedger` 时，换 ERP 必改编排。本月让主编排只认 Port，Adapter 可替换。**不接公司生产库/SSO/真实账套。**

### 概念加深

**接口不稳 vs 模型不行：** 模型答错可重试；Port 漂移则全链路返工。

| 症状 | 根因 | 本月解法 |
|---|---|---|
| Service 里拼 ERP URL | 无 Port 边界 | 抽 Query/Write Port |
| 换 stub 改 20 个类 | 契约未冻结 | ContractTest |
| 超时散落各层 | 无 Adapter 统一策略 | 重试/熔断在 Adapter |
| 字段名随 ERP 变 | 无 Mapper | ErpItemMapper |

```text
主编排 ──只依赖 Port──► Fake / Sandbox Adapter（学习期）
将来换真 ERP：只换 Adapter，编排零改动
```

### 怎么做

1. 填差距表（Gateway 是否直绑 InMemory）。
2. 画 Port/Adapter 图。
3. 列「明确不做」清单。
4. 背诵核心产品句。

### 代码骨架

```java
adapter/port/ItemQueryPort.java
adapter/port/InventoryWritePort.java
adapter/fake/FakeInventoryAdapter.java
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| 觉得接真 ERP 才算学会 | 生产风险 | Fake/Sandbox+契约 |
| Service 拼 ERP URL | 编排与集成耦合 | 只依赖 Port |
| 接公司 VPN/账套 | 违反学习边界 | 本地 stub only |
| 业务代码 if(adapter==) | 换实现要改遍 | Spring 条件装配 |

### 当天验收

- 差距表已填
- 能画 Port/Adapter 图
- 背出核心产品句
- 明确不做清单

### 核心产品句（当日默念）

> 业务代码只依赖稳定 Port/Adapter 接口；学习期用 Fake/Sandbox 实现；将来换真实 ERP 只换适配器，不改 Chat/RAG/Flow/WriteGateway 主编排。本月不接公司生产。

### 与上月衔接一句

| 上月（第5月） | 本月（第6月）当日焦点 |
|---|---|
| WriteGateway/假账本/受控写入 | 为何「接口不稳」比「模型不行」更毁项目 |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 契约测/单测覆盖主路径
- [ ] 无 RestTemplate 直打 ERP
- [ ] 无业务 if(adapter)


### 回归命令（D1）

```bash
./mvnw -q test -Dtest=ContractInventoryQueryTest 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/erp/health || echo "服务未启"
```
---

## M6-D2 Hexagonal：Domain ← Port ← Adapter；反模式清单

> **技术前置：** 此时应当学会 **接口不稳危害；本日开始学 Hexagonal Port/Adapter（T10）** 后再进行阅读。 节点：**T10** · [TECH_ROADMAP](../TECH_ROADMAP.md)
> **建议视频：** B 站搜 `六边形架构 Hexagonal Port Adapter` · 六边形架构 · 总表 [BILIBILI.md](../BILIBILI.md)




### 为什么

第5月已能经 WriteGateway 写假账，但 `DefaultWriteGateway` 直绑 `InMemoryInventoryLedger` 时，换 ERP 必改编排。本月让主编排只认 Port，Adapter 可替换。**不接公司生产库/SSO/真实账套。**

### 概念加深

**六边形架构：**

```text
Domain/UseCase (Gateway, Rag, Flow)
      │ depends on
   Ports (稳定接口)
      │ implemented by
FakeAdapter | SandboxAdapter | (future Http)
```

**反模式：** `restTemplate.getForObject("http://erp/api/items/" + sku)` 出现在 Service 层。

### 怎么做

1. 新建 `adapter/port/`。
2. grep 反模式 RestTemplate。
3. 画六边形图。
4. README 增 Port 边界段。

### 代码骨架

```java
package com.example.erp.ai.adapter.port;
public interface ItemQueryPort {
    java.util.Optional<com.example.erp.ai.domain.ErpItem> findBySku(String tenantId, String sku);
}
// 反模式: restTemplate.getForObject(erpUrl + sku, ...);
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Service 拼 ERP URL | 编排与集成耦合 | 只依赖 Port |
| 接公司 VPN/账套 | 违反学习边界 | 本地 stub only |
| 业务代码 if(adapter==) | 换实现要改遍 | Spring 条件装配 |

### 当天验收

- 代码/骨架已粘贴
- 单测或手测通过
- 笔记含核心产品句
- grep 无反模式

### 核心产品句（当日默念）

> 业务代码只依赖稳定 Port/Adapter 接口；学习期用 Fake/Sandbox 实现；将来换真实 ERP 只换适配器，不改 Chat/RAG/Flow/WriteGateway 主编排。本月不接公司生产。

### 与上月衔接一句

| 上月（第5月） | 本月（第6月）当日焦点 |
|---|---|
| WriteGateway/假账本/受控写入 | Hexagonal：Domain ← Port ← Adapter |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 契约测/单测覆盖主路径
- [ ] 无 RestTemplate 直打 ERP
- [ ] 无业务 if(adapter)


### 回归命令（D2）

```bash
./mvnw -q test -Dtest=ContractInventoryQueryTest 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/erp/health || echo "服务未启"
```
---

## M6-D3 读端口：ItemQueryPort / InventoryQueryPort / PeriodQueryPort

> **技术前置：** 此时应当学会 **六边形心智；本日学读端口 Item/Inventory/Period** 后再进行阅读。 节点：**T10** · [TECH_ROADMAP](../TECH_ROADMAP.md)
> **建议视频：** B 站搜 `Repository 接口 分离` · 查询端口 · 总表 [BILIBILI.md](../BILIBILI.md)




### 为什么

第5月已能经 WriteGateway 写假账，但 `DefaultWriteGateway` 直绑 `InMemoryInventoryLedger` 时，换 ERP 必改编排。本月让主编排只认 Port，Adapter 可替换。**不接公司生产库/SSO/真实账套。**

### 概念加深

| Port | 职责 | 返回 |
|---|---|---|
| `ItemQueryPort` | 物料主数据只读 | `ErpItem` |
| `InventoryQueryPort` | 库存余额只读 | `InventoryBalance` |
| `PeriodQueryPort` | 会计期间状态 | `FiscalPeriodStatus` |

读端口**无副作用**；Chat/RAG 查询只走读 Port。

### 怎么做

1. 定义三 Query Port。
2. 定义 ErpItem/InventoryBalance。
3. Fake 实现。
4. 单测无副作用。

### 代码骨架

```java
public interface ItemQueryPort {
    Optional<ErpItem> findBySku(String tenantId, String sku);
}
public interface InventoryQueryPort {
    Optional<InventoryBalance> getBalance(String tenantId, String sku, String warehouseCode);
}
public interface PeriodQueryPort {
    FiscalPeriodStatus currentPeriod(String tenantId);
}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Service 拼 ERP URL | 编排与集成耦合 | 只依赖 Port |
| 接公司 VPN/账套 | 违反学习边界 | 本地 stub only |
| 业务代码 if(adapter==) | 换实现要改遍 | Spring 条件装配 |

### 当天验收

- 代码/骨架已粘贴
- 单测或手测通过
- 笔记含核心产品句
- grep 无反模式

### 核心产品句（当日默念）

> 业务代码只依赖稳定 Port/Adapter 接口；学习期用 Fake/Sandbox 实现；将来换真实 ERP 只换适配器，不改 Chat/RAG/Flow/WriteGateway 主编排。本月不接公司生产。

### 与上月衔接一句

| 上月（第5月） | 本月（第6月）当日焦点 |
|---|---|
| WriteGateway/假账本/受控写入 | 读端口：ItemQueryPort / InventoryQueryPort / PeriodQueryPort |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 契约测/单测覆盖主路径
- [ ] 无 RestTemplate 直打 ERP
- [ ] 无业务 if(adapter)


### 回归命令（D3）

```bash
./mvnw -q test -Dtest=ContractInventoryQueryTest 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/erp/health || echo "服务未启"
```
---

## M6-D4 写端口：InventoryWritePort / PostingPort（对接第5月 Gateway）

> **技术前置：** 此时应当学会 **读 Port；本日学写端口对接 Gateway** 后再进行阅读。 节点：**T10** · [TECH_ROADMAP](../TECH_ROADMAP.md)
> **建议视频：** B 站搜 `Port Adapter 写模型` · 写端口 · 总表 [BILIBILI.md](../BILIBILI.md)




### 为什么

第5月已能经 WriteGateway 写假账，但 `DefaultWriteGateway` 直绑 `InMemoryInventoryLedger` 时，换 ERP 必改编排。本月让主编排只认 Port，Adapter 可替换。**不接公司生产库/SSO/真实账套。**

### 概念加深

```text
Flow APPROVE → WriteGateway.apply
      → InventoryWritePort.adjust / PostingPort.postDocument
```

Gateway **不再** `new InMemoryInventoryLedger()`，改为注入写 Port。

### 怎么做

1. 定义写 Port。
2. 改 Gateway 注入。
3. Fake 委托 InMemory。
4. write-safety smoke。

### 代码骨架

```java
public interface InventoryWritePort {
    LedgerAdjustmentResult adjust(AdapterContext ctx, InventoryAdjustCommand cmd);
}
public interface PostingPort {
    PostingResult postDocument(AdapterContext ctx, PostingCommand cmd);
}
// DefaultWriteGateway: dispatch ADJUST_QTY -> inventoryWrite.adjust(...)
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Service 拼 ERP URL | 编排与集成耦合 | 只依赖 Port |
| 接公司 VPN/账套 | 违反学习边界 | 本地 stub only |
| 业务代码 if(adapter==) | 换实现要改遍 | Spring 条件装配 |

### 当天验收

- 代码/骨架已粘贴
- 单测或手测通过
- 笔记含核心产品句
- grep 无反模式

### 核心产品句（当日默念）

> 业务代码只依赖稳定 Port/Adapter 接口；学习期用 Fake/Sandbox 实现；将来换真实 ERP 只换适配器，不改 Chat/RAG/Flow/WriteGateway 主编排。本月不接公司生产。

### 与上月衔接一句

| 上月（第5月） | 本月（第6月）当日焦点 |
|---|---|
| WriteGateway/假账本/受控写入 | 写端口：InventoryWritePort / PostingPort（对接第5月 Gateway） |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 契约测/单测覆盖主路径
- [ ] 无 RestTemplate 直打 ERP
- [ ] 无业务 if(adapter)


### 回归命令（D4）

```bash
./mvnw -q test -Dtest=ContractInventoryQueryTest 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/erp/health || echo "服务未启"
```
---

## M6-D5 统一 ErpAdapterException：TIMEOUT / CONFLICT / PERIOD_CLOSED 等

> **技术前置：** 此时应当学会 **写 Port；本日学统一 ErpAdapterException** 后再进行阅读。 节点：**T10** · [TECH_ROADMAP](../TECH_ROADMAP.md)
> **建议视频：** B 站搜 `异常码 设计` · 统一异常模型 · 总表 [BILIBILI.md](../BILIBILI.md)




### 为什么

第5月已能经 WriteGateway 写假账，但 `DefaultWriteGateway` 直绑 `InMemoryInventoryLedger` 时，换 ERP 必改编排。本月让主编排只认 Port，Adapter 可替换。**不接公司生产库/SSO/真实账套。**

### 概念加深

| Code | 含义 | 重试 |
|---|---|---|
| TIMEOUT | 下游超时 | 可（有限） |
| CONFLICT | 冲突 | 否 |
| PERIOD_CLOSED | 期间关闭 | 否 |
| UNAUTHORIZED | 401 | 否 |
| VALIDATION | 校验失败 | 否 |
| UNAVAILABLE | 503/熔断 | 可降级 |

### 怎么做

1. 定义 ErpAdapterException。
2. Gateway 映射 WriteResult。
3. 单测 PERIOD_CLOSED。
4. 文档错误码表。

### 代码骨架

```java
public enum ErpErrorCode { TIMEOUT, CONFLICT, PERIOD_CLOSED, UNAUTHORIZED, VALIDATION, UNAVAILABLE }
public class ErpAdapterException extends RuntimeException {
    private final ErpErrorCode code;
    public ErpErrorCode getCode() { return code; }
}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Service 拼 ERP URL | 编排与集成耦合 | 只依赖 Port |
| 接公司 VPN/账套 | 违反学习边界 | 本地 stub only |
| 业务代码 if(adapter==) | 换实现要改遍 | Spring 条件装配 |

### 当天验收

- 代码/骨架已粘贴
- 单测或手测通过
- 笔记含核心产品句
- grep 无反模式

### 核心产品句（当日默念）

> 业务代码只依赖稳定 Port/Adapter 接口；学习期用 Fake/Sandbox 实现；将来换真实 ERP 只换适配器，不改 Chat/RAG/Flow/WriteGateway 主编排。本月不接公司生产。

### 与上月衔接一句

| 上月（第5月） | 本月（第6月）当日焦点 |
|---|---|
| WriteGateway/假账本/受控写入 | 统一 ErpAdapterException：TIMEOUT / CONFLICT / PERIOD_CLOSED 等 |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 契约测/单测覆盖主路径
- [ ] 无 RestTemplate 直打 ERP
- [ ] 无业务 if(adapter)


### 回归命令（D5）

```bash
./mvnw -q test -Dtest=ContractInventoryQueryTest 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/erp/health || echo "服务未启"
```
---

## M6-D6 ai.erp.adapter: fake|sandbox 装配；禁止业务 if(adapter==)

> **技术前置：** 此时应当学会 **错误模型；本日 ai.erp.adapter 装配（禁业务 if）** 后再进行阅读。 节点：**T10** · [TECH_ROADMAP](../TECH_ROADMAP.md)
> **建议视频：** B 站搜 `Spring @Profile 多实现` · 策略/装配切换 · 总表 [BILIBILI.md](../BILIBILI.md)




### 为什么

第5月已能经 WriteGateway 写假账，但 `DefaultWriteGateway` 直绑 `InMemoryInventoryLedger` 时，换 ERP 必改编排。本月让主编排只认 Port，Adapter 可替换。**不接公司生产库/SSO/真实账套。**

### 概念加深

```yaml
ai:
  erp:
    adapter: fake   # fake | sandbox
```

用 `@ConditionalOnProperty` 装配 Bean；**禁止** Gateway 内 `if (fake)`。

### 怎么做

1. 写 fake/sandbox yml。
2. @ConditionalOnProperty。
3. 验证切换。
4. grep 无 if(adapter)。

### 代码骨架

```java
@Configuration
@ConditionalOnProperty(name = "ai.erp.adapter", havingValue = "fake", matchIfMissing = true)
public class FakeErpAdapterConfiguration {
    @Bean InventoryQueryPort inventoryQueryPort() { return new FakeInventoryQueryAdapter(); }
}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Service 拼 ERP URL | 编排与集成耦合 | 只依赖 Port |
| 接公司 VPN/账套 | 违反学习边界 | 本地 stub only |
| 业务代码 if(adapter==) | 换实现要改遍 | Spring 条件装配 |

### 当天验收

- 代码/骨架已粘贴
- 单测或手测通过
- 笔记含核心产品句
- grep 无反模式

### 核心产品句（当日默念）

> 业务代码只依赖稳定 Port/Adapter 接口；学习期用 Fake/Sandbox 实现；将来换真实 ERP 只换适配器，不改 Chat/RAG/Flow/WriteGateway 主编排。本月不接公司生产。

### 与上月衔接一句

| 上月（第5月） | 本月（第6月）当日焦点 |
|---|---|
| WriteGateway/假账本/受控写入 | ai.erp.adapter: fake|sandbox 装配 |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 契约测/单测覆盖主路径
- [ ] 无 RestTemplate 直打 ERP
- [ ] 无业务 if(adapter)


### 回归命令（D6）

```bash
./mvnw -q test -Dtest=ContractInventoryQueryTest 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/erp/health || echo "服务未启"
```
---

## M6-D7 第 1 周复盘（Port 基础 + Fake 装配）

> **技术前置：** 此时应当学会 **Port 基础 + Fake 装配（T10 第1周）** 后再进行阅读。 节点：**T10** · [TECH_ROADMAP](../TECH_ROADMAP.md)
> **建议视频：** B 站搜 `依赖倒置` · Port 周复盘 · 总表 [BILIBILI.md](../BILIBILI.md)




### 为什么

第5月已能经 WriteGateway 写假账，但 `DefaultWriteGateway` 直绑 `InMemoryInventoryLedger` 时，换 ERP 必改编排。本月让主编排只认 Port，Adapter 可替换。**不接公司生产库/SSO/真实账套。**

### 概念加深

| 检查项 | 标准 |
|---|---|
| 读/写 Port 定义 | 齐全 |
| FakeAdapter | 单测绿 |
| Gateway | 无 InMemory 直引 |
| ErpAdapterException | 六码齐全 |

### 怎么做

1. 跑 W1 检查表。
2. 手测 Fake。
3. 补笔记。
4. 预习契约测。

### 代码骨架

```java
package com.example.erp.ai.adapter;
public record AdapterContext(String tenantId, String userId) {}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Service 拼 ERP URL | 编排与集成耦合 | 只依赖 Port |
| 接公司 VPN/账套 | 违反学习边界 | 本地 stub only |
| 业务代码 if(adapter==) | 换实现要改遍 | Spring 条件装配 |

### 当天验收

- W1 检查表全勾
- Gateway 经 Port
- yml 可切 fake

### 核心产品句（当日默念）

> 业务代码只依赖稳定 Port/Adapter 接口；学习期用 Fake/Sandbox 实现；将来换真实 ERP 只换适配器，不改 Chat/RAG/Flow/WriteGateway 主编排。本月不接公司生产。

### 与上月衔接一句

| 上月（第5月） | 本月（第6月）当日焦点 |
|---|---|
| WriteGateway/假账本/受控写入 | 第 1 周复盘（Port 基础 + Fake 装配） |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 契约测/单测覆盖主路径
- [ ] 无 RestTemplate 直打 ERP
- [ ] 无业务 if(adapter)

### 周复盘自测清单

见「概念加深」检查表；周五必跑 ContractTest smoke。

### 回归命令（D7）

```bash
./mvnw -q test -Dtest=ContractInventoryQueryTest 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/erp/health || echo "服务未启"
```
---


# 第 2 周｜契约测试 · 版本 · 超时重试幂等 · 熔断 · 健康检查

---

## M6-D8 契约测试：同一套测试跑 Fake 与 Stub，断言行为一致

> **技术前置：** 此时应当学会 **Fake 可切换；本日开始学契约测试（可选 WireMock）** 后再进行阅读。 节点：**T10** · [TECH_ROADMAP](../TECH_ROADMAP.md)
> **建议视频：** B 站搜 `契约测试 Pact WireMock` · 契约测试 · 总表 [BILIBILI.md](../BILIBILI.md)




### 为什么

第5月已能经 WriteGateway 写假账，但 `DefaultWriteGateway` 直绑 `InMemoryInventoryLedger` 时，换 ERP 必改编排。本月让主编排只认 Port，Adapter 可替换。**不接公司生产库/SSO/真实账套。**

### 概念加深

同一测试注入 Fake 与 Stub，断言行为一致：

```text
ContractTest → FakeAdapter  → qty=10
            → StubAdapter  → qty=10
```

### 怎么做

1. ContractInventoryQueryTest。
2. 参数化 Fake/Stub。
3. 纳入 CI。
4. adapter-contract.jsonl。

### 代码骨架

```java
@ParameterizedTest
@EnumSource(AdapterFlavor.class)
void contract_sameBalance(AdapterFlavor flavor) {
    var port = fixtures.inventoryQuery(flavor);
    assertEquals(10, port.getBalance("tenant-a","SKU-100","WH-01").orElseThrow().quantity());
}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Service 拼 ERP URL | 编排与集成耦合 | 只依赖 Port |
| 接公司 VPN/账套 | 违反学习边界 | 本地 stub only |
| 业务代码 if(adapter==) | 换实现要改遍 | Spring 条件装配 |

### 当天验收

- 代码/骨架已粘贴
- 单测或手测通过
- 笔记含核心产品句
- grep 无反模式

### 核心产品句（当日默念）

> 业务代码只依赖稳定 Port/Adapter 接口；学习期用 Fake/Sandbox 实现；将来换真实 ERP 只换适配器，不改 Chat/RAG/Flow/WriteGateway 主编排。本月不接公司生产。

### 与上月衔接一句

| 上月（第5月） | 本月（第6月）当日焦点 |
|---|---|
| WriteGateway/假账本/受控写入 | 契约测试：同一套测试跑 Fake 与 Stub，断言行为一致 |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 契约测/单测覆盖主路径
- [ ] 无 RestTemplate 直打 ERP
- [ ] 无业务 if(adapter)


### 回归命令（D8）

```bash
./mvnw -q test -Dtest=ContractInventoryQueryTest 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/erp/health || echo "服务未启"
```
---

## M6-D9 版本化：ErpApiVersion / Accept header；破坏性变更 checklist

> **技术前置：** 此时应当学会 **契约测试；本日 API 版本/破坏性变更 checklist** 后再进行阅读。 节点：**T10** · [TECH_ROADMAP](../TECH_ROADMAP.md)
> **建议视频：** B 站搜 `API versioning` · API 版本 · 总表 [BILIBILI.md](../BILIBILI.md)




### 为什么

第5月已能经 WriteGateway 写假账，但 `DefaultWriteGateway` 直绑 `InMemoryInventoryLedger` 时，换 ERP 必改编排。本月让主编排只认 Port，Adapter 可替换。**不接公司生产库/SSO/真实账套。**

### 概念加深

`Accept: application/vnd.erp-ai.v1+json`

破坏性变更 checklist：Port 签名、错误码、OpenAPI major version。

### 怎么做

1. 阅读概念。
2. 粘贴骨架。
3. 单测/手测。
4. 填验收表。

### 代码骨架

```java
package com.example.erp.ai.adapter;
public record AdapterContext(String tenantId, String userId) {}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Service 拼 ERP URL | 编排与集成耦合 | 只依赖 Port |
| 接公司 VPN/账套 | 违反学习边界 | 本地 stub only |
| 业务代码 if(adapter==) | 换实现要改遍 | Spring 条件装配 |

### 当天验收

- 代码/骨架已粘贴
- 单测或手测通过
- 笔记含核心产品句
- grep 无反模式

### 核心产品句（当日默念）

> 业务代码只依赖稳定 Port/Adapter 接口；学习期用 Fake/Sandbox 实现；将来换真实 ERP 只换适配器，不改 Chat/RAG/Flow/WriteGateway 主编排。本月不接公司生产。

### 与上月衔接一句

| 上月（第5月） | 本月（第6月）当日焦点 |
|---|---|
| WriteGateway/假账本/受控写入 | 版本化：ErpApiVersion / Accept header |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 契约测/单测覆盖主路径
- [ ] 无 RestTemplate 直打 ERP
- [ ] 无业务 if(adapter)


### 回归命令（D9）

```bash
./mvnw -q test -Dtest=ContractInventoryQueryTest 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/erp/health || echo "服务未启"
```
---

## M6-D10 超时与重试策略放适配器层；401/期间关闭不重试

> **技术前置：** 此时应当学会 **版本意识；本日超时重试放适配器层** 后再进行阅读。 节点：**T10** · [TECH_ROADMAP](../TECH_ROADMAP.md)
> **建议视频：** B 站搜 `Spring Retry 超时` · 超时重试 · 总表 [BILIBILI.md](../BILIBILI.md)




### 为什么

第5月已能经 WriteGateway 写假账，但 `DefaultWriteGateway` 直绑 `InMemoryInventoryLedger` 时，换 ERP 必改编排。本月让主编排只认 Port，Adapter 可替换。**不接公司生产库/SSO/真实账套。**

### 概念加深

| 场景 | 重试 |
|---|---|
| TIMEOUT | 最多 2 次退避 |
| UNAUTHORIZED | **否** |
| PERIOD_CLOSED | **否** |

### 怎么做

1. 阅读概念。
2. 粘贴骨架。
3. 单测/手测。
4. 填验收表。

### 代码骨架

```java
package com.example.erp.ai.adapter;
public record AdapterContext(String tenantId, String userId) {}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Service 拼 ERP URL | 编排与集成耦合 | 只依赖 Port |
| 接公司 VPN/账套 | 违反学习边界 | 本地 stub only |
| 业务代码 if(adapter==) | 换实现要改遍 | Spring 条件装配 |

### 当天验收

- 代码/骨架已粘贴
- 单测或手测通过
- 笔记含核心产品句
- grep 无反模式

### 核心产品句（当日默念）

> 业务代码只依赖稳定 Port/Adapter 接口；学习期用 Fake/Sandbox 实现；将来换真实 ERP 只换适配器，不改 Chat/RAG/Flow/WriteGateway 主编排。本月不接公司生产。

### 与上月衔接一句

| 上月（第5月） | 本月（第6月）当日焦点 |
|---|---|
| WriteGateway/假账本/受控写入 | 超时与重试策略放适配器层 |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 契约测/单测覆盖主路径
- [ ] 无 RestTemplate 直打 ERP
- [ ] 无业务 if(adapter)


### 回归命令（D10）

```bash
./mvnw -q test -Dtest=ContractInventoryQueryTest 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/erp/health || echo "服务未启"
```
---

## M6-D11 幂等键从 Flow 传到 Port（Idempotency-Key）

> **技术前置：** 此时应当学会 **韧性策略；本日幂等键传到 Port** 后再进行阅读。 节点：**T9+T10** · [TECH_ROADMAP](../TECH_ROADMAP.md)
> **建议视频：** B 站搜 `Idempotency 传播` · 幂等传到下游 · 总表 [BILIBILI.md](../BILIBILI.md)




### 为什么

第5月已能经 WriteGateway 写假账，但 `DefaultWriteGateway` 直绑 `InMemoryInventoryLedger` 时，换 ERP 必改编排。本月让主编排只认 Port，Adapter 可替换。**不接公司生产库/SSO/真实账套。**

### 概念加深

`X-Idempotency-Key` → `WriteCommand.commandId` → Adapter HTTP header。

### 怎么做

1. 阅读概念。
2. 粘贴骨架。
3. 单测/手测。
4. 填验收表。

### 代码骨架

```java
package com.example.erp.ai.adapter;
public record AdapterContext(String tenantId, String userId) {}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Service 拼 ERP URL | 编排与集成耦合 | 只依赖 Port |
| 接公司 VPN/账套 | 违反学习边界 | 本地 stub only |
| 业务代码 if(adapter==) | 换实现要改遍 | Spring 条件装配 |

### 当天验收

- 代码/骨架已粘贴
- 单测或手测通过
- 笔记含核心产品句
- grep 无反模式

### 核心产品句（当日默念）

> 业务代码只依赖稳定 Port/Adapter 接口；学习期用 Fake/Sandbox 实现；将来换真实 ERP 只换适配器，不改 Chat/RAG/Flow/WriteGateway 主编排。本月不接公司生产。

### 与上月衔接一句

| 上月（第5月） | 本月（第6月）当日焦点 |
|---|---|
| WriteGateway/假账本/受控写入 | 幂等键从 Flow 传到 Port（Idempotency-Key） |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 契约测/单测覆盖主路径
- [ ] 无 RestTemplate 直打 ERP
- [ ] 无业务 if(adapter)


### 回归命令（D11）

```bash
./mvnw -q test -Dtest=ContractInventoryQueryTest 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/erp/health || echo "服务未启"
```
---

## M6-D12 熔断/舱壁概念 + 简易 CircuitBreaker 骨架（学习版）

> **技术前置：** 此时应当学会 **幂等透传；本日简易熔断骨架** 后再进行阅读。 节点：**T10** · [TECH_ROADMAP](../TECH_ROADMAP.md)
> **建议视频：** B 站搜 `Resilience4j 熔断` · 熔断 · 总表 [BILIBILI.md](../BILIBILI.md)




### 为什么

第5月已能经 WriteGateway 写假账，但 `DefaultWriteGateway` 直绑 `InMemoryInventoryLedger` 时，换 ERP 必改编排。本月让主编排只认 Port，Adapter 可替换。**不接公司生产库/SSO/真实账套。**

### 概念加深

`CLOSED → OPEN → HALF_OPEN → CLOSED`；OPEN 时抛 `UNAVAILABLE`。

### 怎么做

1. 阅读概念。
2. 粘贴骨架。
3. 单测/手测。
4. 填验收表。

### 代码骨架

```java
public class SimpleCircuitBreaker {
    public <T> T run(java.util.function.Supplier<T> s) {
        if (state == State.OPEN) throw new ErpAdapterException(ErpErrorCode.UNAVAILABLE, "open");
        return s.get();
    }
}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Service 拼 ERP URL | 编排与集成耦合 | 只依赖 Port |
| 接公司 VPN/账套 | 违反学习边界 | 本地 stub only |
| 业务代码 if(adapter==) | 换实现要改遍 | Spring 条件装配 |

### 当天验收

- 代码/骨架已粘贴
- 单测或手测通过
- 笔记含核心产品句
- grep 无反模式

### 核心产品句（当日默念）

> 业务代码只依赖稳定 Port/Adapter 接口；学习期用 Fake/Sandbox 实现；将来换真实 ERP 只换适配器，不改 Chat/RAG/Flow/WriteGateway 主编排。本月不接公司生产。

### 与上月衔接一句

| 上月（第5月） | 本月（第6月）当日焦点 |
|---|---|
| WriteGateway/假账本/受控写入 | 熔断/舱壁概念 + 简易 CircuitBreaker 骨架（学习版） |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 契约测/单测覆盖主路径
- [ ] 无 RestTemplate 直打 ERP
- [ ] 无业务 if(adapter)


### 回归命令（D12）

```bash
./mvnw -q test -Dtest=ContractInventoryQueryTest 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/erp/health || echo "服务未启"
```
---

## M6-D13 GET /api/ai/erp/health 聚合适配器健康

> **技术前置：** 此时应当学会 **熔断概念；本日适配器健康聚合** 后再进行阅读。 节点：**T10** · [TECH_ROADMAP](../TECH_ROADMAP.md)
> **建议视频：** B 站搜 `Spring Actuator health` · 健康检查 · 总表 [BILIBILI.md](../BILIBILI.md)




### 为什么

第5月已能经 WriteGateway 写假账，但 `DefaultWriteGateway` 直绑 `InMemoryInventoryLedger` 时，换 ERP 必改编排。本月让主编排只认 Port，Adapter 可替换。**不接公司生产库/SSO/真实账套。**

### 概念加深

`GET /api/ai/erp/health` 返回各 Port probe 状态与熔断器状态。

### 怎么做

1. 阅读概念。
2. 粘贴骨架。
3. 单测/手测。
4. 填验收表。

### 代码骨架

```java
package com.example.erp.ai.adapter;
public record AdapterContext(String tenantId, String userId) {}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Service 拼 ERP URL | 编排与集成耦合 | 只依赖 Port |
| 接公司 VPN/账套 | 违反学习边界 | 本地 stub only |
| 业务代码 if(adapter==) | 换实现要改遍 | Spring 条件装配 |

### 当天验收

- 代码/骨架已粘贴
- 单测或手测通过
- 笔记含核心产品句
- grep 无反模式

### 核心产品句（当日默念）

> 业务代码只依赖稳定 Port/Adapter 接口；学习期用 Fake/Sandbox 实现；将来换真实 ERP 只换适配器，不改 Chat/RAG/Flow/WriteGateway 主编排。本月不接公司生产。

### 与上月衔接一句

| 上月（第5月） | 本月（第6月）当日焦点 |
|---|---|
| WriteGateway/假账本/受控写入 | GET /api/ai/erp/health 聚合适配器健康 |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 契约测/单测覆盖主路径
- [ ] 无 RestTemplate 直打 ERP
- [ ] 无业务 if(adapter)


### 回归命令（D13）

```bash
./mvnw -q test -Dtest=ContractInventoryQueryTest 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/erp/health || echo "服务未启"
```
---

## M6-D14 第 2 周复盘（契约 + 韧性）

> **技术前置：** 此时应当学会 **契约 + 韧性（T10 第2周）** 后再进行阅读。 节点：**T10** · [TECH_ROADMAP](../TECH_ROADMAP.md)
> **建议视频：** B 站搜 `微服务 韧性` · 韧性复盘 · 总表 [BILIBILI.md](../BILIBILI.md)




### 为什么

第5月已能经 WriteGateway 写假账，但 `DefaultWriteGateway` 直绑 `InMemoryInventoryLedger` 时，换 ERP 必改编排。本月让主编排只认 Port，Adapter 可替换。**不接公司生产库/SSO/真实账套。**

### 概念加深

ContractTest 绿；重试矩阵口述；health 可用；无业务 if(adapter)。

### 怎么做

1. 阅读概念。
2. 粘贴骨架。
3. 单测/手测。
4. 填验收表。

### 代码骨架

```java
package com.example.erp.ai.adapter;
public record AdapterContext(String tenantId, String userId) {}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Service 拼 ERP URL | 编排与集成耦合 | 只依赖 Port |
| 接公司 VPN/账套 | 违反学习边界 | 本地 stub only |
| 业务代码 if(adapter==) | 换实现要改遍 | Spring 条件装配 |

### 当天验收

- ContractTest 绿
- 重试矩阵口述
- health 可用

### 核心产品句（当日默念）

> 业务代码只依赖稳定 Port/Adapter 接口；学习期用 Fake/Sandbox 实现；将来换真实 ERP 只换适配器，不改 Chat/RAG/Flow/WriteGateway 主编排。本月不接公司生产。

### 与上月衔接一句

| 上月（第5月） | 本月（第6月）当日焦点 |
|---|---|
| WriteGateway/假账本/受控写入 | 第 2 周复盘（契约 + 韧性） |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 契约测/单测覆盖主路径
- [ ] 无 RestTemplate 直打 ERP
- [ ] 无业务 if(adapter)

### 周复盘自测清单

见「概念加深」检查表；周五必跑 ContractTest smoke。

### 回归命令（D14）

```bash
./mvnw -q test -Dtest=ContractInventoryQueryTest 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/erp/health || echo "服务未启"
```
---


# 第 3 周｜Sandbox 适配器 · OpenAPI · 字段映射 · ACL 透传

---

## M6-D15 Sandbox 适配器：对本地 stub HTTP（JDK HttpServer 概念）

> **技术前置：** 此时应当学会 **韧性可讲；本日 Sandbox + JDK HttpServer stub（可选 WireMock）** 后再进行阅读。 节点：**T10** · [TECH_ROADMAP](../TECH_ROADMAP.md)
> **建议视频：** B 站搜 `WireMock 入门` · HttpServer / WireMock stub · 总表 [BILIBILI.md](../BILIBILI.md)




### 为什么

第5月已能经 WriteGateway 写假账，但 `DefaultWriteGateway` 直绑 `InMemoryInventoryLedger` 时，换 ERP 必改编排。本月让主编排只认 Port，Adapter 可替换。**不接公司生产库/SSO/真实账套。**

### 概念加深

`SandboxInventoryAdapter` → `JdkHttpErpClient` → `localhost:18080` stub。

### 怎么做

1. 阅读概念。
2. 粘贴骨架。
3. 单测/手测。
4. 填验收表。

### 代码骨架

```java
public class SandboxInventoryQueryAdapter implements InventoryQueryPort {
    public Optional<InventoryBalance> getBalance(String t, String sku, String wh) {
        String json = client.get("/erp/v1/inventory/" + sku + "?warehouse=" + wh, ctx(t));
        return Optional.of(mapper.toInventoryBalance(json));
    }
}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Service 拼 ERP URL | 编排与集成耦合 | 只依赖 Port |
| 接公司 VPN/账套 | 违反学习边界 | 本地 stub only |
| 业务代码 if(adapter==) | 换实现要改遍 | Spring 条件装配 |

### 当天验收

- 代码/骨架已粘贴
- 单测或手测通过
- 笔记含核心产品句
- grep 无反模式

### 核心产品句（当日默念）

> 业务代码只依赖稳定 Port/Adapter 接口；学习期用 Fake/Sandbox 实现；将来换真实 ERP 只换适配器，不改 Chat/RAG/Flow/WriteGateway 主编排。本月不接公司生产。

### 与上月衔接一句

| 上月（第5月） | 本月（第6月）当日焦点 |
|---|---|
| WriteGateway/假账本/受控写入 | Sandbox 适配器：对本地 stub HTTP（JDK HttpServer 概念） |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 契约测/单测覆盖主路径
- [ ] 无 RestTemplate 直打 ERP
- [ ] 无业务 if(adapter)


### 回归命令（D15）

```bash
./mvnw -q test -Dtest=ContractInventoryQueryTest 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/erp/health || echo "服务未启"
```
---

## M6-D16 OpenAPI/契约 YAML 最小片段（库存查询、过账）

> **技术前置：** 此时应当学会 **Sandbox；本日 OpenAPI 最小契约片段** 后再进行阅读。 节点：**T10** · [TECH_ROADMAP](../TECH_ROADMAP.md)
> **建议视频：** B 站搜 `OpenAPI 3 入门` · OpenAPI 最小契约 · 总表 [BILIBILI.md](../BILIBILI.md)




### 为什么

第5月已能经 WriteGateway 写假账，但 `DefaultWriteGateway` 直绑 `InMemoryInventoryLedger` 时，换 ERP 必改编排。本月让主编排只认 Port，Adapter 可替换。**不接公司生产库/SSO/真实账套。**

### 概念加深

`docs/erp-contract-v1.yaml` 定义库存查询与过账 path；与 stub 对齐。

### 怎么做

1. 阅读概念。
2. 粘贴骨架。
3. 单测/手测。
4. 填验收表。

### 代码骨架

```yaml
paths:
  /erp/v1/inventory/{sku}:
    get: { responses: { '200': { description: balance } } }
  /erp/v1/posting/documents:
    post:
      parameters: [{ name: Idempotency-Key, in: header, required: true }]
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Service 拼 ERP URL | 编排与集成耦合 | 只依赖 Port |
| 接公司 VPN/账套 | 违反学习边界 | 本地 stub only |
| 业务代码 if(adapter==) | 换实现要改遍 | Spring 条件装配 |

### 当天验收

- 代码/骨架已粘贴
- 单测或手测通过
- 笔记含核心产品句
- grep 无反模式

### 核心产品句（当日默念）

> 业务代码只依赖稳定 Port/Adapter 接口；学习期用 Fake/Sandbox 实现；将来换真实 ERP 只换适配器，不改 Chat/RAG/Flow/WriteGateway 主编排。本月不接公司生产。

### 与上月衔接一句

| 上月（第5月） | 本月（第6月）当日焦点 |
|---|---|
| WriteGateway/假账本/受控写入 | OpenAPI/契约 YAML 最小片段（库存查询、过账） |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 契约测/单测覆盖主路径
- [ ] 无 RestTemplate 直打 ERP
- [ ] 无业务 if(adapter)


### 回归命令（D16）

```bash
./mvnw -q test -Dtest=ContractInventoryQueryTest 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/erp/health || echo "服务未启"
```
---

## M6-D17 字段映射：ErpItemMapper（外部编码 ↔ 学习域模型）

> **技术前置：** 此时应当学会 **OpenAPI；本日字段映射 Mapper** 后再进行阅读。 节点：**T10** · [TECH_ROADMAP](../TECH_ROADMAP.md)
> **建议视频：** B 站搜 `MapStruct 入门` · 对象映射 MapStruct 概念 · 总表 [BILIBILI.md](../BILIBILI.md)




### 为什么

第5月已能经 WriteGateway 写假账，但 `DefaultWriteGateway` 直绑 `InMemoryInventoryLedger` 时，换 ERP 必改编排。本月让主编排只认 Port，Adapter 可替换。**不接公司生产库/SSO/真实账套。**

### 概念加深

| 外部 | 领域 |
|---|---|
| item_code | sku |
| item_name | displayName |

Mapper **只在 Adapter 内**。

### 怎么做

1. 阅读概念。
2. 粘贴骨架。
3. 单测/手测。
4. 填验收表。

### 代码骨架

```java
package com.example.erp.ai.adapter;
public record AdapterContext(String tenantId, String userId) {}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Service 拼 ERP URL | 编排与集成耦合 | 只依赖 Port |
| 接公司 VPN/账套 | 违反学习边界 | 本地 stub only |
| 业务代码 if(adapter==) | 换实现要改遍 | Spring 条件装配 |

### 当天验收

- 代码/骨架已粘贴
- 单测或手测通过
- 笔记含核心产品句
- grep 无反模式

### 核心产品句（当日默念）

> 业务代码只依赖稳定 Port/Adapter 接口；学习期用 Fake/Sandbox 实现；将来换真实 ERP 只换适配器，不改 Chat/RAG/Flow/WriteGateway 主编排。本月不接公司生产。

### 与上月衔接一句

| 上月（第5月） | 本月（第6月）当日焦点 |
|---|---|
| WriteGateway/假账本/受控写入 | 字段映射：ErpItemMapper（外部编码 ↔ 学习域模型） |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 契约测/单测覆盖主路径
- [ ] 无 RestTemplate 直打 ERP
- [ ] 无业务 if(adapter)


### 回归命令（D17）

```bash
./mvnw -q test -Dtest=ContractInventoryQueryTest 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/erp/health || echo "服务未启"
```
---

## M6-D18 主数据适配：供应商/仓库/存货查询 Port

> **技术前置：** 此时应当学会 **Mapper；本日主数据查询 Port** 后再进行阅读。 节点：**T10** · [TECH_ROADMAP](../TECH_ROADMAP.md)
> **建议视频：** B 站搜 `主数据管理 MDM 入门` · 主数据查询 · 总表 [BILIBILI.md](../BILIBILI.md)




### 为什么

第5月已能经 WriteGateway 写假账，但 `DefaultWriteGateway` 直绑 `InMemoryInventoryLedger` 时，换 ERP 必改编排。本月让主编排只认 Port，Adapter 可替换。**不接公司生产库/SSO/真实账套。**

### 概念加深

`VendorQueryPort` / `WarehouseQueryPort` 扩展主数据只读查询。

### 怎么做

1. 阅读概念。
2. 粘贴骨架。
3. 单测/手测。
4. 填验收表。

### 代码骨架

```java
package com.example.erp.ai.adapter;
public record AdapterContext(String tenantId, String userId) {}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Service 拼 ERP URL | 编排与集成耦合 | 只依赖 Port |
| 接公司 VPN/账套 | 违反学习边界 | 本地 stub only |
| 业务代码 if(adapter==) | 换实现要改遍 | Spring 条件装配 |

### 当天验收

- 代码/骨架已粘贴
- 单测或手测通过
- 笔记含核心产品句
- grep 无反模式

### 核心产品句（当日默念）

> 业务代码只依赖稳定 Port/Adapter 接口；学习期用 Fake/Sandbox 实现；将来换真实 ERP 只换适配器，不改 Chat/RAG/Flow/WriteGateway 主编排。本月不接公司生产。

### 与上月衔接一句

| 上月（第5月） | 本月（第6月）当日焦点 |
|---|---|
| WriteGateway/假账本/受控写入 | 主数据适配：供应商/仓库/存货查询 Port |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 契约测/单测覆盖主路径
- [ ] 无 RestTemplate 直打 ERP
- [ ] 无业务 if(adapter)


### 回归命令（D18）

```bash
./mvnw -q test -Dtest=ContractInventoryQueryTest 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/erp/health || echo "服务未启"
```
---

## M6-D19 透传学习头：tenant/user/roles → 适配器审计上下文

> **技术前置：** 此时应当学会 **主数据 Port；本日透传 tenant/user 审计上下文** 后再进行阅读。 节点：**T8+T10** · [TECH_ROADMAP](../TECH_ROADMAP.md)
> **建议视频：** B 站搜 `MDC traceId 透传` · 审计上下文透传 · 总表 [BILIBILI.md](../BILIBILI.md)




### 为什么

第5月已能经 WriteGateway 写假账，但 `DefaultWriteGateway` 直绑 `InMemoryInventoryLedger` 时，换 ERP 必改编排。本月让主编排只认 Port，Adapter 可替换。**不接公司生产库/SSO/真实账套。**

### 概念加深

`AdapterContext(tenantId, userId, roles, traceId, requestId)` 出站 HTTP 头透传（学习头）。

### 怎么做

1. 阅读概念。
2. 粘贴骨架。
3. 单测/手测。
4. 填验收表。

### 代码骨架

```java
package com.example.erp.ai.adapter;
public record AdapterContext(String tenantId, String userId) {}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Service 拼 ERP URL | 编排与集成耦合 | 只依赖 Port |
| 接公司 VPN/账套 | 违反学习边界 | 本地 stub only |
| 业务代码 if(adapter==) | 换实现要改遍 | Spring 条件装配 |

### 当天验收

- 代码/骨架已粘贴
- 单测或手测通过
- 笔记含核心产品句
- grep 无反模式

### 核心产品句（当日默念）

> 业务代码只依赖稳定 Port/Adapter 接口；学习期用 Fake/Sandbox 实现；将来换真实 ERP 只换适配器，不改 Chat/RAG/Flow/WriteGateway 主编排。本月不接公司生产。

### 与上月衔接一句

| 上月（第5月） | 本月（第6月）当日焦点 |
|---|---|
| WriteGateway/假账本/受控写入 | 透传学习头：tenant/user/roles → 适配器审计上下文 |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 契约测/单测覆盖主路径
- [ ] 无 RestTemplate 直打 ERP
- [ ] 无业务 if(adapter)


### 回归命令（D19）

```bash
./mvnw -q test -Dtest=ContractInventoryQueryTest 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/erp/health || echo "服务未启"
```
---

## M6-D20 写路径端到端：APPROVE → Gateway → PostingPort(Sandbox)

> **技术前置：** 此时应当学会 **上下文透传；本日 E2E：APPROVE→Gateway→PostingPort** 后再进行阅读。 节点：**T9+T10** · [TECH_ROADMAP](../TECH_ROADMAP.md)
> **建议视频：** B 站搜 `端到端测试` · E2E 过账 · 总表 [BILIBILI.md](../BILIBILI.md)




### 为什么

第5月已能经 WriteGateway 写假账，但 `DefaultWriteGateway` 直绑 `InMemoryInventoryLedger` 时，换 ERP 必改编排。本月让主编排只认 Port，Adapter 可替换。**不接公司生产库/SSO/真实账套。**

### 概念加深

Flow APPROVE → Gateway → `PostingPort(Sandbox)` → stub POSTED；全程 Port 边界。

### 怎么做

1. 阅读概念。
2. 粘贴骨架。
3. 单测/手测。
4. 填验收表。

### 代码骨架

```text
// E2E: APPROVE -> WriteGateway -> PostingPort(Sandbox) -> stub POSTED
curl -X POST .../decide -H 'X-Idempotency-Key: flow-{id}-approve' -d '{"decision":"APPROVE"}'
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Service 拼 ERP URL | 编排与集成耦合 | 只依赖 Port |
| 接公司 VPN/账套 | 违反学习边界 | 本地 stub only |
| 业务代码 if(adapter==) | 换实现要改遍 | Spring 条件装配 |

### 当天验收

- 代码/骨架已粘贴
- 单测或手测通过
- 笔记含核心产品句
- grep 无反模式

### 核心产品句（当日默念）

> 业务代码只依赖稳定 Port/Adapter 接口；学习期用 Fake/Sandbox 实现；将来换真实 ERP 只换适配器，不改 Chat/RAG/Flow/WriteGateway 主编排。本月不接公司生产。

### 与上月衔接一句

| 上月（第5月） | 本月（第6月）当日焦点 |
|---|---|
| WriteGateway/假账本/受控写入 | 写路径端到端：APPROVE → Gateway → PostingPort(Sandbox) |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 契约测/单测覆盖主路径
- [ ] 无 RestTemplate 直打 ERP
- [ ] 无业务 if(adapter)


### 回归命令（D20）

```bash
./mvnw -q test -Dtest=ContractInventoryQueryTest 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/erp/health || echo "服务未启"
```
---

## M6-D21 第 3 周复盘（Sandbox + 映射 + E2E）

> **技术前置：** 此时应当学会 **Sandbox + 映射 + E2E（T10 第3周）** 后再进行阅读。 节点：**T10** · [TECH_ROADMAP](../TECH_ROADMAP.md)
> **建议视频：** B 站搜 `测试替身 Test Double` · Sandbox 复盘 · 总表 [BILIBILI.md](../BILIBILI.md)




### 为什么

第5月已能经 WriteGateway 写假账，但 `DefaultWriteGateway` 直绑 `InMemoryInventoryLedger` 时，换 ERP 必改编排。本月让主编排只认 Port，Adapter 可替换。**不接公司生产库/SSO/真实账套。**

### 概念加深

Sandbox E2E 绿；OpenAPI 对齐；Mapper 单测绿。

### 怎么做

1. 阅读概念。
2. 粘贴骨架。
3. 单测/手测。
4. 填验收表。

### 代码骨架

```java
package com.example.erp.ai.adapter;
public record AdapterContext(String tenantId, String userId) {}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Service 拼 ERP URL | 编排与集成耦合 | 只依赖 Port |
| 接公司 VPN/账套 | 违反学习边界 | 本地 stub only |
| 业务代码 if(adapter==) | 换实现要改遍 | Spring 条件装配 |

### 当天验收

- Sandbox E2E 绿
- OpenAPI 对齐
- Mapper 单测绿

### 核心产品句（当日默念）

> 业务代码只依赖稳定 Port/Adapter 接口；学习期用 Fake/Sandbox 实现；将来换真实 ERP 只换适配器，不改 Chat/RAG/Flow/WriteGateway 主编排。本月不接公司生产。

### 与上月衔接一句

| 上月（第5月） | 本月（第6月）当日焦点 |
|---|---|
| WriteGateway/假账本/受控写入 | 第 3 周复盘（Sandbox + 映射 + E2E） |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 契约测/单测覆盖主路径
- [ ] 无 RestTemplate 直打 ERP
- [ ] 无业务 if(adapter)

### 周复盘自测清单

见「概念加深」检查表；周五必跑 ContractTest smoke。

### 回归命令（D21）

```bash
./mvnw -q test -Dtest=ContractInventoryQueryTest 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/erp/health || echo "服务未启"
```
---


# 第 4 周｜可观测 · 切换演练 · 串测 · PORTFOLIO · 收官

---

## M6-D22 适配器日志：outbound requestId、latency、errorCode（脱敏）

> **技术前置：** 此时应当学会 **E2E 可演示；本日适配器日志（脱敏）** 后再进行阅读。 节点：**T6+T10** · [TECH_ROADMAP](../TECH_ROADMAP.md)
> **建议视频：** B 站搜 `日志脱敏` · 出站日志脱敏 · 总表 [BILIBILI.md](../BILIBILI.md)




### 为什么

第5月已能经 WriteGateway 写假账，但 `DefaultWriteGateway` 直绑 `InMemoryInventoryLedger` 时，换 ERP 必改编排。本月让主编排只认 Port，Adapter 可替换。**不接公司生产库/SSO/真实账套。**

### 概念加深

日志字段：requestId、latencyMs、errorCode；**脱敏**凭证与客户名。

### 怎么做

1. 阅读概念。
2. 粘贴骨架。
3. 单测/手测。
4. 填验收表。

### 代码骨架

```java
package com.example.erp.ai.adapter;
public record AdapterContext(String tenantId, String userId) {}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Service 拼 ERP URL | 编排与集成耦合 | 只依赖 Port |
| 接公司 VPN/账套 | 违反学习边界 | 本地 stub only |
| 业务代码 if(adapter==) | 换实现要改遍 | Spring 条件装配 |

### 当天验收

- 代码/骨架已粘贴
- 单测或手测通过
- 笔记含核心产品句
- grep 无反模式

### 核心产品句（当日默念）

> 业务代码只依赖稳定 Port/Adapter 接口；学习期用 Fake/Sandbox 实现；将来换真实 ERP 只换适配器，不改 Chat/RAG/Flow/WriteGateway 主编排。本月不接公司生产。

### 与上月衔接一句

| 上月（第5月） | 本月（第6月）当日焦点 |
|---|---|
| WriteGateway/假账本/受控写入 | 适配器日志：outbound requestId、latency、errorCode（脱敏） |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 契约测/单测覆盖主路径
- [ ] 无 RestTemplate 直打 ERP
- [ ] 无业务 if(adapter)


### 回归命令（D22）

```bash
./mvnw -q test -Dtest=ContractInventoryQueryTest 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/erp/health || echo "服务未启"
```
---

## M6-D23 切换演练：fake → sandbox 一键；对比行为

> **技术前置：** 此时应当学会 **适配器日志；本日 fake→sandbox 切换演练** 后再进行阅读。 节点：**T10** · [TECH_ROADMAP](../TECH_ROADMAP.md)
> **建议视频：** B 站搜 `配置中心 概念` · 环境切换演练 · 总表 [BILIBILI.md](../BILIBILI.md)




### 为什么

第5月已能经 WriteGateway 写假账，但 `DefaultWriteGateway` 直绑 `InMemoryInventoryLedger` 时，换 ERP 必改编排。本月让主编排只认 Port，Adapter 可替换。**不接公司生产库/SSO/真实账套。**

### 概念加深

改 `ai.erp.adapter: sandbox` 重启；同一 curl 剧本；ContractTest 仍绿。

### 怎么做

1. 备 fake/sandbox profile。
2. 重启切换。
3. 同剧本对比。
4. ContractTest 双 profile。

### 代码骨架

```java
package com.example.erp.ai.adapter;
public record AdapterContext(String tenantId, String userId) {}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Service 拼 ERP URL | 编排与集成耦合 | 只依赖 Port |
| 接公司 VPN/账套 | 违反学习边界 | 本地 stub only |
| 业务代码 if(adapter==) | 换实现要改遍 | Spring 条件装配 |

### 当天验收

- 代码/骨架已粘贴
- 单测或手测通过
- 笔记含核心产品句
- grep 无反模式

### 核心产品句（当日默念）

> 业务代码只依赖稳定 Port/Adapter 接口；学习期用 Fake/Sandbox 实现；将来换真实 ERP 只换适配器，不改 Chat/RAG/Flow/WriteGateway 主编排。本月不接公司生产。

### 与上月衔接一句

| 上月（第5月） | 本月（第6月）当日焦点 |
|---|---|
| WriteGateway/假账本/受控写入 | 切换演练：fake → sandbox 一键 |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 契约测/单测覆盖主路径
- [ ] 无 RestTemplate 直打 ERP
- [ ] 无业务 if(adapter)


### 回归命令（D23）

```bash
./mvnw -q test -Dtest=ContractInventoryQueryTest 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/erp/health || echo "服务未启"
```
---

## M6-D24 串测 A：只读查询走 Port

> **技术前置：** 此时应当学会 **切换演练；本日串测只读查询走 Port** 后再进行阅读。 节点：**T10** · [TECH_ROADMAP](../TECH_ROADMAP.md)
> **建议视频：** B 站搜 `适配器 演示` · 只读走 Port 演示 · 总表 [BILIBILI.md](../BILIBILI.md)




### 为什么

第5月已能经 WriteGateway 写假账，但 `DefaultWriteGateway` 直绑 `InMemoryInventoryLedger` 时，换 ERP 必改编排。本月让主编排只认 Port，Adapter 可替换。**不接公司生产库/SSO/真实账套。**

### 概念加深

只读 API + RAG 问库存；断言无 WRITE 审计。

### 怎么做

1. 阅读概念。
2. 粘贴骨架。
3. 单测/手测。
4. 填验收表。

### 代码骨架

```java
package com.example.erp.ai.adapter;
public record AdapterContext(String tenantId, String userId) {}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Service 拼 ERP URL | 编排与集成耦合 | 只依赖 Port |
| 接公司 VPN/账套 | 违反学习边界 | 本地 stub only |
| 业务代码 if(adapter==) | 换实现要改遍 | Spring 条件装配 |

### 当天验收

- 代码/骨架已粘贴
- 单测或手测通过
- 笔记含核心产品句
- grep 无反模式

### 核心产品句（当日默念）

> 业务代码只依赖稳定 Port/Adapter 接口；学习期用 Fake/Sandbox 实现；将来换真实 ERP 只换适配器，不改 Chat/RAG/Flow/WriteGateway 主编排。本月不接公司生产。

### 与上月衔接一句

| 上月（第5月） | 本月（第6月）当日焦点 |
|---|---|
| WriteGateway/假账本/受控写入 | 串测 A：只读查询走 Port |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 契约测/单测覆盖主路径
- [ ] 无 RestTemplate 直打 ERP
- [ ] 无业务 if(adapter)


### 回归命令（D24）

```bash
./mvnw -q test -Dtest=ContractInventoryQueryTest 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/erp/health || echo "服务未启"
```
---

## M6-D25 串测 B：受控过账走 Port + 幂等

> **技术前置：** 此时应当学会 **场景 A；本日串测受控过账+幂等** 后再进行阅读。 节点：**T9+T10** · [TECH_ROADMAP](../TECH_ROADMAP.md)
> **建议视频：** B 站搜 `幂等 演示` · 过账+幂等演示 · 总表 [BILIBILI.md](../BILIBILI.md)




### 为什么

第5月已能经 WriteGateway 写假账，但 `DefaultWriteGateway` 直绑 `InMemoryInventoryLedger` 时，换 ERP 必改编排。本月让主编排只认 Port，Adapter 可替换。**不接公司生产库/SSO/真实账套。**

### 概念加深

APPROVE → PostingPort；重复幂等键 → DUPLICATE；审计含 requestId。

### 怎么做

1. 阅读概念。
2. 粘贴骨架。
3. 单测/手测。
4. 填验收表。

### 代码骨架

```java
package com.example.erp.ai.adapter;
public record AdapterContext(String tenantId, String userId) {}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Service 拼 ERP URL | 编排与集成耦合 | 只依赖 Port |
| 接公司 VPN/账套 | 违反学习边界 | 本地 stub only |
| 业务代码 if(adapter==) | 换实现要改遍 | Spring 条件装配 |

### 当天验收

- 代码/骨架已粘贴
- 单测或手测通过
- 笔记含核心产品句
- grep 无反模式

### 核心产品句（当日默念）

> 业务代码只依赖稳定 Port/Adapter 接口；学习期用 Fake/Sandbox 实现；将来换真实 ERP 只换适配器，不改 Chat/RAG/Flow/WriteGateway 主编排。本月不接公司生产。

### 与上月衔接一句

| 上月（第5月） | 本月（第6月）当日焦点 |
|---|---|
| WriteGateway/假账本/受控写入 | 串测 B：受控过账走 Port + 幂等 |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 契约测/单测覆盖主路径
- [ ] 无 RestTemplate 直打 ERP
- [ ] 无业务 if(adapter)


### 回归命令（D25）

```bash
./mvnw -q test -Dtest=ContractInventoryQueryTest 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/erp/health || echo "服务未启"
```
---

## M6-D26 串测 C：期间关闭/超时降级

> **技术前置：** 此时应当学会 **场景 B；本日串测期间关闭/超时降级** 后再进行阅读。 节点：**T10** · [TECH_ROADMAP](../TECH_ROADMAP.md)
> **建议视频：** B 站搜 `超时降级 演示` · 降级演示 · 总表 [BILIBILI.md](../BILIBILI.md)




### 为什么

第5月已能经 WriteGateway 写假账，但 `DefaultWriteGateway` 直绑 `InMemoryInventoryLedger` 时，换 ERP 必改编排。本月让主编排只认 Port，Adapter 可替换。**不接公司生产库/SSO/真实账套。**

### 概念加深

stub `periodClosed` / 延迟 / 连续失败 → PERIOD_CLOSED / TIMEOUT / UNAVAILABLE。

### 怎么做

1. 阅读概念。
2. 粘贴骨架。
3. 单测/手测。
4. 填验收表。

### 代码骨架

```java
package com.example.erp.ai.adapter;
public record AdapterContext(String tenantId, String userId) {}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Service 拼 ERP URL | 编排与集成耦合 | 只依赖 Port |
| 接公司 VPN/账套 | 违反学习边界 | 本地 stub only |
| 业务代码 if(adapter==) | 换实现要改遍 | Spring 条件装配 |

### 当天验收

- 代码/骨架已粘贴
- 单测或手测通过
- 笔记含核心产品句
- grep 无反模式

### 核心产品句（当日默念）

> 业务代码只依赖稳定 Port/Adapter 接口；学习期用 Fake/Sandbox 实现；将来换真实 ERP 只换适配器，不改 Chat/RAG/Flow/WriteGateway 主编排。本月不接公司生产。

### 与上月衔接一句

| 上月（第5月） | 本月（第6月）当日焦点 |
|---|---|
| WriteGateway/假账本/受控写入 | 串测 C：期间关闭/超时降级 |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 契约测/单测覆盖主路径
- [ ] 无 RestTemplate 直打 ERP
- [ ] 无业务 if(adapter)


### 回归命令（D26）

```bash
./mvnw -q test -Dtest=ContractInventoryQueryTest 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/erp/health || echo "服务未启"
```
---

## M6-D27 PORTFOLIO 章节模板「适配器稳定化」

> **技术前置：** 此时应当学会 **三场景；本日 PORTFOLIO 适配器稳定化** 后再进行阅读。 节点：**作品集** · [TECH_ROADMAP](../TECH_ROADMAP.md)
> **建议视频：** B 站搜 `六边形架构 简历` · PORTFOLIO 适配器 · 总表 [BILIBILI.md](../BILIBILI.md)




### 为什么

第5月已能经 WriteGateway 写假账，但 `DefaultWriteGateway` 直绑 `InMemoryInventoryLedger` 时，换 ERP 必改编排。本月让主编排只认 Port，Adapter 可替换。**不接公司生产库/SSO/真实账套。**

### 概念加深

PORTFOLIO：问题—Port 方案—契约测证据—明确不接生产。

### 怎么做

1. 阅读概念。
2. 粘贴骨架。
3. 单测/手测。
4. 填验收表。

### 代码骨架

```java
package com.example.erp.ai.adapter;
public record AdapterContext(String tenantId, String userId) {}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Service 拼 ERP URL | 编排与集成耦合 | 只依赖 Port |
| 接公司 VPN/账套 | 违反学习边界 | 本地 stub only |
| 业务代码 if(adapter==) | 换实现要改遍 | Spring 条件装配 |

### 当天验收

- 代码/骨架已粘贴
- 单测或手测通过
- 笔记含核心产品句
- grep 无反模式

### 核心产品句（当日默念）

> 业务代码只依赖稳定 Port/Adapter 接口；学习期用 Fake/Sandbox 实现；将来换真实 ERP 只换适配器，不改 Chat/RAG/Flow/WriteGateway 主编排。本月不接公司生产。

### 与上月衔接一句

| 上月（第5月） | 本月（第6月）当日焦点 |
|---|---|
| WriteGateway/假账本/受控写入 | PORTFOLIO 章节模板「适配器稳定化」 |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 契约测/单测覆盖主路径
- [ ] 无 RestTemplate 直打 ERP
- [ ] 无业务 if(adapter)


### 回归命令（D27）

```bash
./mvnw -q test -Dtest=ContractInventoryQueryTest 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/erp/health || echo "服务未启"
```
---

## M6-D28 架构终图（1～6月叠加；强调 Port 边界）

> **技术前置：** 此时应当学会 **作品集；本日架构终图强调 Port 边界** 后再进行阅读。 节点：**T0～T10** · [TECH_ROADMAP](../TECH_ROADMAP.md)
> **建议视频：** B 站搜 `整洁架构` · 架构终图 Port 边界 · 总表 [BILIBILI.md](../BILIBILI.md)




### 为什么

第5月已能经 WriteGateway 写假账，但 `DefaultWriteGateway` 直绑 `InMemoryInventoryLedger` 时，换 ERP 必改编排。本月让主编排只认 Port，Adapter 可替换。**不接公司生产库/SSO/真实账套。**

### 概念加深

叠加第1～6月层；**紫色标 Port 边界**；脚注不接公司生产。

### 怎么做

1. 阅读概念。
2. 粘贴骨架。
3. 单测/手测。
4. 填验收表。

### 代码骨架

```java
package com.example.erp.ai.adapter;
public record AdapterContext(String tenantId, String userId) {}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Service 拼 ERP URL | 编排与集成耦合 | 只依赖 Port |
| 接公司 VPN/账套 | 违反学习边界 | 本地 stub only |
| 业务代码 if(adapter==) | 换实现要改遍 | Spring 条件装配 |

### 当天验收

- 代码/骨架已粘贴
- 单测或手测通过
- 笔记含核心产品句
- grep 无反模式

### 核心产品句（当日默念）

> 业务代码只依赖稳定 Port/Adapter 接口；学习期用 Fake/Sandbox 实现；将来换真实 ERP 只换适配器，不改 Chat/RAG/Flow/WriteGateway 主编排。本月不接公司生产。

### 与上月衔接一句

| 上月（第5月） | 本月（第6月）当日焦点 |
|---|---|
| WriteGateway/假账本/受控写入 | 架构终图（1～6月叠加 |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 契约测/单测覆盖主路径
- [ ] 无 RestTemplate 直打 ERP
- [ ] 无业务 if(adapter)


### 回归命令（D28）

```bash
./mvnw -q test -Dtest=ContractInventoryQueryTest 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/erp/health || echo "服务未启"
```
---

## M6-D29 口述自测（20 题）

> **技术前置：** 此时应当学会 **架构；本日口述自测** 后再进行阅读。 节点：**T10** · [TECH_ROADMAP](../TECH_ROADMAP.md)
> **建议视频：** B 站搜 `Port Adapter 面试` · 口述自测 · 总表 [BILIBILI.md](../BILIBILI.md)




### 为什么

第5月已能经 WriteGateway 写假账，但 `DefaultWriteGateway` 直绑 `InMemoryInventoryLedger` 时，换 ERP 必改编排。本月让主编排只认 Port，Adapter 可替换。**不接公司生产库/SSO/真实账套。**

### 概念加深

口述 20 题：Port vs Adapter、契约测、fake/sandbox、幂等透传、熔断、第7月方向等。16/20 过关。

### 怎么做

1. 阅读概念。
2. 粘贴骨架。
3. 单测/手测。
4. 填验收表。

### 代码骨架

```java
package com.example.erp.ai.adapter;
public record AdapterContext(String tenantId, String userId) {}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Service 拼 ERP URL | 编排与集成耦合 | 只依赖 Port |
| 接公司 VPN/账套 | 违反学习边界 | 本地 stub only |
| 业务代码 if(adapter==) | 换实现要改遍 | Spring 条件装配 |

### 当天验收

- 口述 ≥16/20
- 薄弱题回读

### 核心产品句（当日默念）

> 业务代码只依赖稳定 Port/Adapter 接口；学习期用 Fake/Sandbox 实现；将来换真实 ERP 只换适配器，不改 Chat/RAG/Flow/WriteGateway 主编排。本月不接公司生产。

### 与上月衔接一句

| 上月（第5月） | 本月（第6月）当日焦点 |
|---|---|
| WriteGateway/假账本/受控写入 | 口述自测（20 题） |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 契约测/单测覆盖主路径
- [ ] 无 RestTemplate 直打 ERP
- [ ] 无业务 if(adapter)


### 口述 20 题完整清单

1. 为何接口不稳更毁项目？ 2. 核心产品句？ 3. Port vs Adapter？ 4. 读写 Port 为何拆分？
5. Gateway 如何改？ 6. ErpAdapterException 六码？ 7. 哪些不重试？ 8. 契约测价值？
9. fake vs sandbox？ 10. 幂等如何透传？ 11. 熔断 OPEN？ 12. health 用途？
13. Mapper 放哪？ 14. OpenAPI 与 Port？ 15. 为何不接生产？ 16. ACL 头透传？
17. 串测 A/B/C？ 18. 切换演练？ 19. 第7月方向？ 20. 8 分钟 pitch？

评分：16/20 过关。

### 回归命令（D29）

```bash
./mvnw -q test -Dtest=ContractInventoryQueryTest 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/erp/health || echo "服务未启"
```
---

## M6-D30 收官；第7月方向（已开课：规则引擎）

> **技术前置：** 此时应当学会 **第6月收官：T10 完成；可选 Testcontainers；下月 T11 规则引擎** 后再进行阅读。 节点：**T10→T11** · [TECH_ROADMAP](../TECH_ROADMAP.md)
> **建议视频：** [Drools 课先收藏](https://www.bilibili.com/video/BV1G44y1t7B1/) · 下月规则引擎；先看概述集 · 备用搜：`规则引擎` · 总表 [BILIBILI.md](../BILIBILI.md)




### 为什么

第5月已能经 WriteGateway 写假账，但 `DefaultWriteGateway` 直绑 `InMemoryInventoryLedger` 时，换 ERP 必改编排。本月让主编排只认 Port，Adapter 可替换。**不接公司生产库/SSO/真实账套。**

### 概念加深

第7月方向（已开课：**规则引擎**）：

整月教材：[`MONTH7_DAY1-30_COMBINED.md`](./MONTH7_DAY1-30_COMBINED.md) · 入口 [`docs/MONTH7.md`](../MONTH7.md)

主题：确定性业务规则用 RuleEngine；LLM 只做理解/草稿/解释；规则先于模型；可版本化、可评测、可审计；**仍不接公司生产规则平台**。

其它可留第8月（只选一条）：观测大盘 / 灰度演练 / 工作流可视化 / 提示词运营。

### 怎么做

1. 收官清单。
2. baseline 全跑。
3. 进入第7月讲义（规则引擎）。
4. 8 分钟 pitch。

### 代码骨架

```java
package com.example.erp.ai.adapter;
public record AdapterContext(String tenantId, String userId) {}
```

### 坑与排障

| 误区/现象 | 为什么危险 | 今天怎么避 |
|---|---|---|
| Service 拼 ERP URL | 编排与集成耦合 | 只依赖 Port |
| 接公司 VPN/账套 | 违反学习边界 | 本地 stub only |
| 业务代码 if(adapter==) | 换实现要改遍 | Spring 条件装配 |

### 当天验收

- 清单诚实
- 三场景+contract 绿
- 第7月计划
- pitch 三句

### 核心产品句（当日默念）

> 业务代码只依赖稳定 Port/Adapter 接口；学习期用 Fake/Sandbox 实现；将来换真实 ERP 只换适配器，不改 Chat/RAG/Flow/WriteGateway 主编排。本月不接公司生产。

### 与上月衔接一句

| 上月（第5月） | 本月（第6月）当日焦点 |
|---|---|
| WriteGateway/假账本/受控写入 | 收官 |

### 手测记录模板

| 步骤 | 预期 | 实际 | 通过 |
|---|---|---|---|
| 1 |  |  |  |
| 2 |  |  |  |
| 3 |  |  |  |

### 代码落地检查（粘贴后）

- [ ] 未修改 `erp-ai-assistant` 源（自行复制）
- [ ] 包路径与附录 B 一致
- [ ] 契约测/单测覆盖主路径
- [ ] 无 RestTemplate 直打 ERP
- [ ] 无业务 if(adapter)


### 明确不做（再次锁定）

- 公司 SSO / 生产库 / 真实账套
- LLM 直写 ERP / Chat 隐式写
- 跳过契约测 / 无审计静默写

### 归档清单

- [ ] `docs/erp-contract-v1.yaml`
- [ ] `evals/suites/adapter-contract.jsonl`
- [ ] 切换演练记录
- [ ] PORTFOLIO 第6月章

### 回归命令（D30）

```bash
./mvnw -q test -Dtest=ContractInventoryQueryTest 2>/dev/null || true
curl -sf http://localhost:8080/api/ai/erp/health || echo "服务未启"
```
---


# 附录

## 附录 A｜配置总表（第6月）

```yaml
ai:
  provider: mock
  erp:
    adapter: fake                    # fake | sandbox | http-stub
    api-version: v1
    sandbox:
      base-url: http://localhost:18080
      connect-timeout-ms: 2000
      read-timeout-ms: 5000
      max-retries: 2
      backoff-ms: 200
    circuit-breaker:
      enabled: true
      failure-threshold: 5
      open-duration-ms: 30000
    health:
      enabled: true
  write:
    gateway-enabled: true
    require-idempotency-key: true
    audit-dir: data/write-audit
  flow:
    write-on-approve: true
    audit-enabled: true
  acl:
    enabled: true
    mode: learning-headers
  eval:
    suites-dir: evals/suites
    baseline-path: evals/baseline.json
  console:
    show-adapter-health: true
```

## 附录 B｜包结构增量（第6月）

```text
adapter/port/ItemQueryPort.java
adapter/port/InventoryQueryPort.java
adapter/port/PeriodQueryPort.java
adapter/port/VendorQueryPort.java
adapter/port/WarehouseQueryPort.java
adapter/port/InventoryWritePort.java
adapter/port/PostingPort.java
adapter/AdapterContext.java
adapter/ErpErrorCode.java
adapter/ErpAdapterException.java
adapter/ErpApiVersion.java
adapter/AbstractErpAdapter.java
adapter/RetryPolicy.java
adapter/SimpleCircuitBreaker.java
adapter/AdapterCallLog.java
adapter/fake/FakeItemAdapter.java
adapter/fake/FakeInventoryQueryAdapter.java
adapter/fake/FakeInventoryWriteAdapter.java
adapter/fake/FakePostingAdapter.java
adapter/fake/FakePeriodAdapter.java
adapter/fake/FakeVendorAdapter.java
adapter/fake/FakeErpAdapterConfiguration.java
adapter/sandbox/JdkHttpErpClient.java
adapter/sandbox/SandboxInventoryQueryAdapter.java
adapter/sandbox/SandboxInventoryWriteAdapter.java
adapter/sandbox/SandboxPostingAdapter.java
adapter/sandbox/ErpItemMapper.java
adapter/sandbox/SandboxErpAdapterConfiguration.java
adapter/stub/LocalErpStubServer.java
adapter/health/ErpAdapterHealthIndicator.java
controller/ErpHealthController.java
controller/ItemQueryController.java
test/contract/ContractInventoryQueryTest.java
test/contract/ContractPostingTest.java
test/contract/AdapterTestFixtures.java
docs/erp-contract-v1.yaml
evals/suites/adapter-contract.jsonl
docs/PORTFOLIO.md
```

## 附录 C｜术语表（第6月）

| 术语 | 含义 | 易混点 |
|---|---|---|
| Port | 主编排依赖的稳定接口 | ≠ Adapter 实现 |
| Adapter | Port 实现（Fake/Sandbox/将来 Http） | ≠ 公司 ERP |
| FakeAdapter | 内存/封装 InMemory 的学习实现 | ≠ 生产连接器 |
| SandboxAdapter | 对本地 HTTP stub 的适配器 | ≠ 公司 VPN ERP |
| 契约测试 | 多 Adapter 行为一致断言 | ≠ 仅 Mock 单测 |
| ErpAdapterException | 统一适配器异常 | ≠ 随意 RuntimeException |
| AdapterContext | 租户/用户/幂等等出站上下文 | ≠ 公司 SSO token |
| ErpItemMapper | 外部字段↔领域模型 | 只在 Adapter 内 |
| CircuitBreaker | 熔断器 | ≠ 重试本身 |
| Contract YAML | OpenAPI 片段 | Port 是代码契约 |
| 真适配器（学习口径） | 契约+假实现+契约测 | ≠ 接学员公司系统 |
| Idempotency-Key | 透传到 Adapter HTTP | ≠ 仅 HTTP 层 |
| adapter-contract | eval 套件名 | ≠ write-safety |
| Http-stub | 本地 stub 模式配置值 | ≠ 生产 HTTP |

## 附录 D｜文档索引（MONTH1–6）

| 文档 | 路径 |
|---|---|
| 第1月详版 | `docs/lessons/MONTH1_DAY1-30_COMBINED.md` |
| 第1月入口 | `docs/MONTH1.md` |
| 第2月详版 | `docs/lessons/MONTH2_DAY1-30_COMBINED.md` |
| 第2月入口 | `docs/MONTH2.md` |
| 第3月详版 | `docs/lessons/MONTH3_DAY1-30_COMBINED.md` |
| 第3月入口 | `docs/MONTH3.md` |
| 第4月详版 | `docs/lessons/MONTH4_DAY1-30_COMBINED.md` |
| 第4月入口 | `docs/MONTH4.md` |
| 第5月详版 | `docs/lessons/MONTH5_DAY1-30_COMBINED.md` |
| 第5月入口 | `docs/MONTH5.md` |
| 第6月详版 | `docs/lessons/MONTH6_DAY1-30_COMBINED.md` |
| 第6月入口 | `docs/MONTH6.md` |
| 打卡笔记 | `docs/STUDY_NOTES.md` |
| 作品集 | `docs/PORTFOLIO.md` |
| ERP 契约 | `docs/erp-contract-v1.yaml` |

## 附录 E｜学习纪律（第6月）

1. **Port 边界：** Chat/RAG/Flow/WriteGateway 只依赖 Port；禁止 Service 拼 ERP URL。
2. **Fake/Sandbox only：** 不接公司生产库/SSO/真实账套。
3. **无业务 if(adapter)：** 切换靠 `@ConditionalOnProperty` + yml。
4. **契约测试必跑：** Fake 与 Stub 行为不一致则 Port 未冻结。
5. **错误统一：** `ErpAdapterException`；主编排映射业务结果。
6. **韧性在 Adapter：** 超时/重试/熔断不散落 Controller。
7. **幂等透传：** `Idempotency-Key` 到 Port 到 Sandbox HTTP。
8. **明确不做：** 公司 ERP 生产、学员账套、LLM 直写 ERP。

## 附录 F｜常见问题（FAQ）

**Q: 「真适配器」要不要接我公司 ERP？**  
A: **不要。** 「真」= 契约稳定+可替换+契约测；实现用 Fake/Sandbox。

**Q: 第5月 InMemory 还要吗？**  
A: 可保留，由 `FakeInventoryWriteAdapter` 封装；Gateway 不见 InMemory。

**Q: WireMock 必须吗？**  
A: 不必须。JDK `HttpServer` 或文档级 WireMock 二选一。

**Q: Port 能常改吗？**  
A: 尽量少改；破坏性变更走 checklist + ContractTest。

**Q: 401 为何不重试？**  
A: 凭证错误重试无意义；与第1月一致。

**Q: fake 与 sandbox 不一致？**  
A: 修 Adapter/stub 直到 ContractTest 绿。

**Q: write-safety 还跑吗？**  
A: 要。Port 改造不能破坏第5月写入纪律。

**Q: 多个 ERP 同时接？**  
A: 学习期不做；进阶可多 Adapter + 路由。

**Q: 讲义能自动改 erp-ai-assistant 吗？**  
A: **不能。** 自行复制粘贴；本文仅为 MD 骨架。

**Q: 第7月选哪条？**  
A: 观测大盘/规则引擎/灰度/工作流可视化——**只选一条**。

## 附录 G｜第6月 curl/scripts 速查

```bash
# 适配器健康
curl -s http://localhost:8080/api/ai/erp/health

# 只读：物料
curl -s http://localhost:8080/api/ai/items/SKU-100 -H 'X-Tenant-Id: tenant-a'

# 只读：库存
curl -s "http://localhost:8080/api/ai/inventory/SKU-100?warehouse=WH-01" -H 'X-Tenant-Id: tenant-a'

# sandbox stub 直测
curl -s "http://localhost:18080/erp/v1/inventory/SKU-100?warehouse=WH-01"

# 写路径仍经 Flow+Gateway
curl -s -X POST http://localhost:8080/api/ai/flow/instances/{id}/decide \
  -H 'Content-Type: application/json' -H 'X-Roles: FINANCE' \
  -H 'X-Tenant-Id: tenant-a' -H 'X-Idempotency-Key: flow-{id}-approve' \
  -d '{"decision":"APPROVE"}'

# 契约 eval
curl -s -X POST http://localhost:8080/api/ai/eval/run \
  -H 'Content-Type: application/json' -d '{"suite":"adapter-contract"}'

# 切换 profile
# java -jar app.jar --spring.profiles.active=erp-sandbox

# 启动本地 stub
# java -cp ... com.example.erp.ai.adapter.stub.LocalErpStubServer

# grep 反模式
grep -rn 'RestTemplate.*erp' src/main/java --include='*.java' | grep -v adapter
grep -rn 'InMemoryInventoryLedger' src/main/java/com/example/erp/ai/write
```

## 附录 H｜第6月与第5月结构对照

| 结构段 | 第5月 | 第6月 |
|---|---|---|
| 为什么 | ✓ | ✓ |
| 概念加深 | ✓ | ✓ |
| 怎么做 | ✓ | ✓ |
| 代码骨架 | Java | Java/YAML/bash |
| 坑与排障 | ✓ | ✓ |
| 当天验收 | ✓ | ✓ |

复盘日（D7/D14/D21）六段齐全；代码骨架可为检查清单。

## 附录 I｜DefaultWriteGateway 经 Port 改造（参考）

```java
package com.example.erp.ai.write;

import com.example.erp.ai.adapter.*;
import com.example.erp.ai.adapter.port.*;

public class DefaultWriteGateway implements WriteGateway {
    private final WriteAuthorization auth;
    private final IdempotencyStore idempotency;
    private final InventoryWritePort inventoryWrite;
    private final PostingPort posting;
    private final PeriodQueryPort periodQuery;
    private final WriteAuditSink audit;

    @Override
    public WriteResult apply(WriteCommand cmd) {
        AdapterContext ctx = AdapterContext.from(cmd);
        audit.emit(WriteAuditEvent.requested(cmd));
        return idempotency.find(cmd.commandId())
            .map(r -> r.withStatus(WriteResult.Status.DUPLICATE))
            .orElseGet(() -> dispatchWithPort(ctx, cmd));
    }

    private WriteResult dispatchWithPort(AdapterContext ctx, WriteCommand cmd) {
        if (!auth.canApply(cmd.requestedBy(), cmd))
            return WriteResult.rejected(cmd.commandId(), "FORBIDDEN");
        try {
            return switch (cmd.type()) {
                case "ADJUST_QTY" -> mapAdjust(inventoryWrite.adjust(ctx, toAdjust(cmd)));
                case "POST_DOCUMENT" -> {
                    String period = String.valueOf(cmd.payload().get("fiscalPeriod"));
                    if (!periodQuery.isPostingAllowed(cmd.tenantId(), period))
                        throw new ErpAdapterException(ErpErrorCode.PERIOD_CLOSED, "closed");
                    yield mapPost(posting.postDocument(ctx, toPost(cmd)));
                }
                default -> WriteResult.rejected(cmd.commandId(), "UNKNOWN_TYPE");
            };
        } catch (ErpAdapterException ex) {
            return mapAdapterError(cmd, ex);
        }
    }
}
```

## 附录 J｜第6月单测/契约测清单

| 测试类 | 覆盖 |
|---|---|
| `ContractInventoryQueryTest` | Fake vs Stub 读 |
| `ContractPostingTest` | 过账/期间/幂等 |
| `ErpItemMapperTest` | 字段映射 |
| `RetryPolicyTest` | 可重试矩阵 |
| `SimpleCircuitBreakerTest` | OPEN/HALF_OPEN |
| `ErpHealthIndicatorTest` | 聚合健康 |
| `DefaultWriteGatewayPortIT` | Gateway 经 Port |

## 附录 K｜LocalErpStubServer（JDK HttpServer）

```java
package com.example.erp.ai.adapter.stub;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class LocalErpStubServer {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(18080), 0);
        server.createContext("/erp/v1/inventory/", ex -> {
            String body = "{\"sku\":\"SKU-100\",\"warehouse\":\"WH-01\",\"quantity\":10,\"uom\":\"EA\"}";
            byte[] b = body.getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(200, b.length);
            ex.getResponseBody().write(b);
            ex.close();
        });
        server.createContext("/erp/v1/posting/documents", ex -> {
            String key = ex.getRequestHeaders().getFirst("Idempotency-Key");
            String body = "{\"status\":\"POSTED\",\"idempotencyKey\":\"" + key + "\"}";
            byte[] b = body.getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(200, b.length);
            ex.getResponseBody().write(b);
            ex.close();
        });
        server.start();
        System.out.println("ERP learning stub on :18080 (not production)");
    }
}
```

## 附录 L｜adapter-contract.jsonl 示例

```jsonl
{"id":"ac-001","action":"query_balance","sku":"SKU-100","warehouse":"WH-01","expectQuantity":10}
{"id":"ac-002","action":"post_document","periodClosed":true,"expectError":"PERIOD_CLOSED"}
{"id":"ac-003","action":"adjust_qty","idempotencyKey":"cmd-dup","repeat":2,"expectSecond":"DUPLICATE"}
{"id":"ac-004","action":"query_vendor","vendorCode":"V-001","expectName":"学习供应商"}
```

## 附录 M｜串测 A/B/C 剧本

### 串测 A — 只读走 Port

| 步骤 | 操作 | 预期 |
|---|---|---|
| A1 | GET items/SKU-100 | 200 ErpItem |
| A2 | GET inventory | qty=10 |
| A3 | RAG 问库存 | 无 WRITE 审计 |

### 串测 B — 过账+幂等

| 步骤 | 操作 | 预期 |
|---|---|---|
| B1 | Flow APPROVE | POSTED |
| B2 | 重复同幂等键 | DUPLICATE |
| B3 | 审计 | requestId 存在 |

### 串测 C — 降级

| 步骤 | 操作 | 预期 |
|---|---|---|
| C1 | periodClosed | PERIOD_CLOSED |
| C2 | stub 延迟 | TIMEOUT |
| C3 | 连续失败 | 熔断 UNAVAILABLE |

## 附录 N｜FakeItemAdapter 参考实现

```java
public class FakeItemAdapter implements ItemQueryPort {
    private final Map<String, Map<String, ErpItem>> catalog = Map.of(
        "tenant-a", Map.of(
            "SKU-100", new ErpItem("SKU-100", "学习物料A", "EA"),
            "SKU-200", new ErpItem("SKU-200", "学习物料B", "KG")
        )
    );
    @Override
    public Optional<ErpItem> findBySku(String tenantId, String sku) {
        return Optional.ofNullable(catalog.getOrDefault(tenantId, Map.of()).get(sku));
    }
}
```

## 附录 O｜架构终图（第1～6月）

```text
┌──────────────────────────────────────────────────────────┐
│ L1 Chat/RAG  L2 Gate/HITL  L3 Store/Audit  L4 ACL/Tenant │
│ L5 WriteGateway/HITL写  L6 Port边界 ◄── 第6月            │
└────────────────────────────┬─────────────────────────────┘
                             │ *Port 接口
              ┌──────────────┴──────────────┐
              ▼                             ▼
       FakeAdapter                   SandboxAdapter
       (in-memory)                   (localhost stub)
              └──────── 不接公司生产 ──────┘
```

## 附录 P｜切换演练 Runbook

1. 启动 `LocalErpStubServer`（:18080）。
2. `--spring.profiles.active=erp-sandbox`。
3. `GET /api/ai/erp/health` → UP。
4. `mvn test -Dtest=Contract*Test`。
5. 串测 A/B/C。
6. 切回 fake profile 对比。
7. 记入 PORTFOLIO。

## 附录 Q｜PORTFOLIO 模板

```markdown
## 第6月：适配器稳定化
**问题：** Gateway 绑 InMemory，换 ERP 改编排。
**方案：** Port/Adapter + ContractTest + fake|sandbox 配置。
**证据：** 契约测绿、切换截图、串测记录。
**边界：** 不接公司生产。
**贡献：** Port 设计 / FakeAdapter / ContractTest / 切换演练。
```

## 附录 R｜grep 反模式合集

```bash
grep -rn 'RestTemplate\|WebClient' src/main/java/.../rag src/main/java/.../chat
grep -rn 'equals("fake")\|equals("sandbox")' src/main/java
grep -rn 'InMemoryInventory\|InMemoryPosting' src/main/java/.../write
ls src/main/java/.../adapter/port/
ls src/test/java/**/contract/
```

---

## 修订

| 日期 | 说明 |
|---|---|
| 2026-08-15 | 第6月首版合并讲义（Port/Adapter / Fake/Sandbox / 契约测试） |
| 2026-08-15 | 逐日详版：30 天完整六段结构 + 附录 A～R |
| 2026-08-15 | 与第5月衔接：Gateway/Ledger/Posting 迁移至 Port |

> **全文收束：** 业务代码只依赖稳定 Port/Adapter 接口；学习期用 Fake/Sandbox；换 ERP 只换适配器，不改主编排。**本月不接公司生产。**
