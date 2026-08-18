package com.erp.ai.eval.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 评测运行请求体。
 *
 * <h2>职责</h2>
 * <p>指定要加载的 JSONL 评测套件在 classpath 上的相对路径。</p>
 *
 * <h2>在系统中的位置</h2>
 * <p>{@code eval.dto} 入站 DTO；供 {@code POST /api/ai/eval/run} 使用。</p>
 *
 * <h2>对应学习 Day</h2>
 * <p>Day25：轻量评测入参。</p>
 *
 * <h2>调用链</h2>
 * <pre>
 * HTTP JSON（可选）→ EvalRunRequest → EvalRunnerService.run → ClassPathResource(suite)
 * </pre>
 *
 * <h2>设计约束</h2>
 * <ul>
 *   <li>默认 suite 为 {@code evals/month1-smoke.jsonl}</li>
 *   <li>blank/null 时由 Service 回退到同一默认路径</li>
 *   <li>本类无校验注解；错误套件路径在加载阶段以 IllegalArgumentException 暴露</li>
 * </ul>
 */
public class EvalRunRequest {

    /**
     * classpath 相对路径，默认 {@code evals/month1-smoke.jsonl}。
     * <p>指向 resources 下的 JSONL 文件，每行一个用例（或 # 注释 / 空行）。</p>
     */
    private String suite = "evals/month1-smoke.jsonl";

    /**
     * 获取评测套件路径。
     *
     * @return suite 相对路径；可能为 null（若被显式置空）
     */
    public String getSuite() {
        return suite;
    }

    /**
     * 设置评测套件路径。
     *
     * @param suite classpath 相对路径，例如 {@code evals/month1-smoke.jsonl}
     */
    public void setSuite(String suite) {
        this.suite = suite;
    }
}
