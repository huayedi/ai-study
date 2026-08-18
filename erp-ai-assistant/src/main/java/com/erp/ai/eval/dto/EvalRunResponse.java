package com.erp.ai.eval.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Day25：轻量评测运行结果。
 */
public class EvalRunResponse {

    private String traceId;
    private String suite;
    private int total;
    private int passed;
    private int failed;
    private List<CaseResult> results = new ArrayList<>();

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getSuite() {
        return suite;
    }

    public void setSuite(String suite) {
        this.suite = suite;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPassed() {
        return passed;
    }

    public void setPassed(int passed) {
        this.passed = passed;
    }

    public int getFailed() {
        return failed;
    }

    public void setFailed(int failed) {
        this.failed = failed;
    }

    public List<CaseResult> getResults() {
        return results;
    }

    public void setResults(List<CaseResult> results) {
        this.results = results == null ? new ArrayList<>() : results;
    }

    public static class CaseResult {
        private String id;
        private String type;
        private boolean passed;
        private String detail;

        public CaseResult() {
        }

        public CaseResult(String id, String type, boolean passed, String detail) {
            this.id = id;
            this.type = type;
            this.passed = passed;
            this.detail = detail;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isPassed() {
            return passed;
        }

        public void setPassed(boolean passed) {
            this.passed = passed;
        }

        public String getDetail() {
            return detail;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }
    }
}
