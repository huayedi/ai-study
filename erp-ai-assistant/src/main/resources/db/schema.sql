-- 学习库 schema（MySQL 5.7+ / H2 MySQL 模式）
-- 库名：ai —— 纯学习测试库，不接公司生产账套

CREATE TABLE IF NOT EXISTS learning_item (
  item_code VARCHAR(64) PRIMARY KEY,
  name VARCHAR(128) NOT NULL,
  spec VARCHAR(256) NOT NULL
);

CREATE TABLE IF NOT EXISTS learning_inventory (
  item_code VARCHAR(64) NOT NULL,
  warehouse VARCHAR(64) NOT NULL,
  qty DECIMAL(18,4) NOT NULL,
  PRIMARY KEY (item_code, warehouse)
);

CREATE TABLE IF NOT EXISTS learning_period (
  company VARCHAR(64) NOT NULL,
  period VARCHAR(16) NOT NULL,
  status VARCHAR(16) NOT NULL,
  PRIMARY KEY (company, period)
);

CREATE TABLE IF NOT EXISTS chat_session_message (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  session_id VARCHAR(64) NOT NULL,
  role VARCHAR(16) NOT NULL,
  content TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ai_call_audit (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  trace_id VARCHAR(64) NOT NULL,
  session_id VARCHAR(64) NULL,
  provider VARCHAR(64) NULL,
  model VARCHAR(128) NULL,
  success TINYINT NOT NULL,
  latency_ms BIGINT NULL,
  attempts INT NULL,
  prompt_tokens INT NULL,
  completion_tokens INT NULL,
  estimated_cost_usd DECIMAL(18,8) NULL,
  error_reason VARCHAR(512) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tool_call_audit (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tool_name VARCHAR(64) NOT NULL,
  ok TINYINT NOT NULL,
  args_keys VARCHAR(256) NULL,
  error_message VARCHAR(512) NULL,
  latency_ms BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
