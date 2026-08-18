package com.erp.ai.security.dto;

import java.util.List;

/**
 * Day23：安全抽测结果（学习接口，不接公司防火墙）。
 */
public class SecurityProbeResponse {

    private String traceId;
    private boolean blocked;
    private String category;
    private String reason;
    private List<String> matched = List.of();
    private String defenseHint;

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List<String> getMatched() {
        return matched;
    }

    public void setMatched(List<String> matched) {
        this.matched = matched == null ? List.of() : List.copyOf(matched);
    }

    public String getDefenseHint() {
        return defenseHint;
    }

    public void setDefenseHint(String defenseHint) {
        this.defenseHint = defenseHint;
    }
}
