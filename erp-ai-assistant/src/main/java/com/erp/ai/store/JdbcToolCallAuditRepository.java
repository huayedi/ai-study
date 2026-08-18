package com.erp.ai.store;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcToolCallAuditRepository {

    private final JdbcTemplate jdbc;

    public JdbcToolCallAuditRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(String toolName, boolean ok, String argsKeys, String error, long latencyMs) {
        String clipped = error == null ? null : (error.length() > 500 ? error.substring(0, 500) : error);
        String keys = argsKeys == null ? null : (argsKeys.length() > 250 ? argsKeys.substring(0, 250) : argsKeys);
        jdbc.update("""
                        INSERT INTO tool_call_audit(tool_name, ok, args_keys, error_message, latency_ms)
                        VALUES (?,?,?,?,?)
                        """,
                toolName, ok ? 1 : 0, keys, clipped, latencyMs);
    }
}
