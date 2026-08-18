package com.erp.ai.eval.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 轻量评测运行结果响应体。
 *
 * <h2>职责</h2>
 * <p>
 * 汇总一次评测套件执行：追踪 ID、套件路径、总数/通过/失败，以及每条用例的 {@link CaseResult}。
 * </p>
 *
 * <h2>在系统中的位置</h2>
 * <p>{@code eval.dto} 出站 DTO；由 {@code EvalRunnerService#run} 填充后经 Controller 返回。</p>
 *
 * <h2>对应学习 Day</h2>
 * <p>Day25：轻量评测运行结果。</p>
 *
 * <h2>调用链</h2>
 * <pre>
 * EvalRunnerService.run → 填充本对象（含 CaseResult 列表）→ EvalController
 * </pre>
 *
 * <h2>设计约束</h2>
 * <ul>
 *   <li>{@code failed} 由 Service 按 {@code total - passed} 写入，与 results 一致</li>
 *   <li>{@code setResults(null)} 时规范为空 {@link ArrayList}，避免 NPE</li>
 *   <li>内嵌 {@link CaseResult} 同时支持无参构造（反序列化）与全参构造（评测代码快速创建）</li>
 * </ul>
 */
public class EvalRunResponse {

    /** 本次评测运行的追踪 ID（无连字符 UUID）。 */
    private String traceId;

    /** 实际加载的套件 classpath 路径（可能是默认值）。 */
    private String suite;

    /** 用例总数（= results.size()）。 */
    private int total;

    /** 通过数。 */
    private int passed;

    /** 失败数（通常 = total - passed）。 */
    private int failed;

    /** 逐条用例结果列表；默认空 ArrayList。 */
    private List<CaseResult> results = new ArrayList<>();

    /**
     * 获取追踪 ID。
     *
     * @return traceId
     */
    public String getTraceId() {
        return traceId;
    }

    /**
     * 设置追踪 ID。
     *
     * @param traceId 无连字符 UUID
     */
    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    /**
     * 获取套件路径。
     *
     * @return classpath 相对路径
     */
    public String getSuite() {
        return suite;
    }

    /**
     * 设置套件路径。
     *
     * @param suite 实际使用的 JSONL 路径
     */
    public void setSuite(String suite) {
        this.suite = suite;
    }

    /**
     * 获取用例总数。
     *
     * @return total
     */
    public int getTotal() {
        return total;
    }

    /**
     * 设置用例总数。
     *
     * @param total 结果条数
     */
    public void setTotal(int total) {
        this.total = total;
    }

    /**
     * 获取通过数。
     *
     * @return passed
     */
    public int getPassed() {
        return passed;
    }

    /**
     * 设置通过数。
     *
     * @param passed 断言成功的用例数
     */
    public void setPassed(int passed) {
        this.passed = passed;
    }

    /**
     * 获取失败数。
     *
     * @return failed
     */
    public int getFailed() {
        return failed;
    }

    /**
     * 设置失败数。
     *
     * @param failed 断言失败或异常的用例数
     */
    public void setFailed(int failed) {
        this.failed = failed;
    }

    /**
     * 获取逐条结果。
     *
     * @return CaseResult 列表
     */
    public List<CaseResult> getResults() {
        return results;
    }

    /**
     * 设置逐条结果。
     * <p>null 入参会被替换为空 ArrayList，保证列表字段非 null。</p>
     *
     * @param results 用例结果；可为 null
     */
    public void setResults(List<CaseResult> results) {
        this.results = results == null ? new ArrayList<>() : results;
    }

    /**
     * 单条评测用例结果。
     * <p>
     * 由 {@code EvalRunnerService} 各 eval* 方法构造；
     * {@code detail} 为人类可读的诊断摘要（通过时也可能含 sources/gate 等观测信息）。
     * </p>
     */
    public static class CaseResult {

        /** JSONL 中的用例 id；缺失时评测侧用 {@code unknown}。 */
        private String id;

        /** 用例类型：rag / security / tool / draft / chat / scenario 等。 */
        private String type;

        /** 该条是否通过断言。 */
        private boolean passed;

        /** 诊断详情：失败原因、观测字段拼接，或 unknown type / exception 信息。 */
        private String detail;

        /**
         * 无参构造：供 Jackson 等框架反序列化使用。
         */
        public CaseResult() {
        }

        /**
         * 全参构造：评测代码快速创建一条结果。
         *
         * @param id     用例 id
         * @param type   用例类型字符串
         * @param passed 是否通过
         * @param detail 详情文案
         */
        public CaseResult(String id, String type, boolean passed, String detail) {
            this.id = id;
            this.type = type;
            this.passed = passed;
            this.detail = detail;
        }

        /**
         * 获取用例 id。
         *
         * @return id
         */
        public String getId() {
            return id;
        }

        /**
         * 设置用例 id。
         *
         * @param id 用例标识
         */
        public void setId(String id) {
            this.id = id;
        }

        /**
         * 获取用例类型。
         *
         * @return type 字符串
         */
        public String getType() {
            return type;
        }

        /**
         * 设置用例类型。
         *
         * @param type 如 rag、chat
         */
        public void setType(String type) {
            this.type = type;
        }

        /**
         * 是否通过。
         *
         * @return true 表示断言成功
         */
        public boolean isPassed() {
            return passed;
        }

        /**
         * 设置是否通过。
         *
         * @param passed 通过标志
         */
        public void setPassed(boolean passed) {
            this.passed = passed;
        }

        /**
         * 获取详情文案。
         *
         * @return detail
         */
        public String getDetail() {
            return detail;
        }

        /**
         * 设置详情文案。
         *
         * @param detail 诊断或观测信息
         */
        public void setDetail(String detail) {
            this.detail = detail;
        }
    }
}
