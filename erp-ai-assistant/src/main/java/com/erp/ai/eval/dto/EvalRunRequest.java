package com.erp.ai.eval.dto;

import java.util.ArrayList;
import java.util.List;

public class EvalRunRequest {

    /** classpath 相对路径，默认 evals/month1-smoke.jsonl */
    private String suite = "evals/month1-smoke.jsonl";

    public String getSuite() {
        return suite;
    }

    public void setSuite(String suite) {
        this.suite = suite;
    }
}
