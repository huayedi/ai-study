# 学习库 MySQL（测试环境）

> **定位：** 纯学习测试库，**不是**公司生产库 / SSO / 真实账套。  
> **用途：** 会话历史、工具假主数据、AI/Tool 审计落库；后续 MONTH2～5 的 Store/假账本可继续挂在此库（表可扩展）。

## 连接信息

| 项 | 值 |
|---|---|
| Host | `154.8.183.10` |
| Port | `3306` |
| Database | `ai` |
| User | `root` |
| Password | `password`（学习测试口令；可用环境变量覆盖） |
| 引擎版本 | MySQL 5.7.x |

JDBC URL（应用默认）：

```text
jdbc:mysql://154.8.183.10:3306/ai?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
```

## 环境变量（推荐）

```bash
export MYSQL_HOST=154.8.183.10
export MYSQL_PORT=3306
export MYSQL_DATABASE=ai
export MYSQL_USER=root
export MYSQL_PASSWORD=password
```

对应 `erp-ai-assistant/src/main/resources/application.yml` 的 `spring.datasource.*`（**HikariCP** 连接池）。

## 持久化栈（当前）

| 层 | 技术 |
|---|---|
| 连接池 | HikariCP（`spring.datasource.type=com.zaxxer.hikari.HikariDataSource`） |
| ORM | MyBatis-Plus 3.5.x（`mybatis-plus-spring-boot3-starter`） |
| SQL | **只写在 XML**：`erp-ai-assistant/src/main/resources/mapper/{chat,tool,common}/*.xml` |
| Mapper 扫描 | `com.erp.ai.common.mapper` / `chat.mapper` / `tool.mapper` |

包分层：`chat` / `tool` / `rag` / `common` 域内各自带 `controller`，避免扁平 `controller` 大杂烩。

## 表一览

| 表 | 作用 |
|---|---|
| `learning_item` | 存货主数据（只读工具 queryItem） |
| `learning_inventory` | 现存量假数据（queryInventory） |
| `learning_period` | 期间状态（queryPeriodStatus） |
| `chat_session_message` | Chat 多轮历史 |
| `ai_call_audit` | LLM 调用审计 |
| `tool_call_audit` | 工具调用审计 |

Schema / 种子：`erp-ai-assistant/src/main/resources/db/schema.sql`、`data.sql`（启动时 `spring.sql.init.mode=always`）。  
业务 SQL：见 `mapper/**/*.xml`（例如 `ChatSessionMessageMapper.xml`、`LearningDataMapper.xml`、`AiCallAuditMapper.xml`）。

## 种子数据（Day16）

- `ITEM-A001` / `A001` @ `原料仓` → **120**
- 同上 @ `成品仓` → **0**
- `主公司` / `2026-08` → **OPEN**

## 健康检查

启动后：

```bash
curl -s http://localhost:8080/actuator/health | jq
```

应见 `db` 组件 UP。

## 与教材关系

| 月份 | 说明 |
|---|---|
| MONTH1 | Chat 会话、Tool 假主数据、审计已落 MySQL |
| MONTH2 | VectorStore 仍可先 memory；需要持久化时扩表，不必换库 |
| MONTH3 | 讲义中的 pgvector 为**选修对照**；本学习仓默认 MySQL，不强制 Postgres |
| MONTH5+ | 假账本可继续用内存或扩 MySQL 表；**仍不接公司生产库** |

## 纪律

1. 禁止把公司 VPN / 生产账套 JDBC 配进本仓库。  
2. 学习库可丢可重建；重要实验自行备份。  
3. LLM API Key 仍走环境变量，勿与本库口令混用概念。  
4. 单测默认 H2（`src/test/resources/application.properties`），不依赖外网 MySQL。
