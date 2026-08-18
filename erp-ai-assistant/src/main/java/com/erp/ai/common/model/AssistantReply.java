package com.erp.ai.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 助手结构化回复（业务侧使用的对象）。
 *
 * <h2>职责</h2>
 * 承载提示词约定的 JSON Schema 字段，供 API 响应与后续业务逻辑使用。
 *
 * <h2>与提示词 Schema 的对应</h2>
 * <pre>
 * {
 *   "answer": "...",
 *   "need_human": true/false,
 *   "suggested_doc_type": "...",
 *   "required_fields": [],
 *   "confidence": 0.0~1.0
 * }
 * </pre>
 * 注意：JSON 侧多用 snake_case；本类字段为 camelCase，由 {@code ReplyParser} 做兼容映射。
 *
 * <h2>为何 {@code @JsonIgnoreProperties(ignoreUnknown = true)}</h2>
 * 忽略模型多返回的未知字段，增强容错，避免反序列化直接失败。
 *
 * <h2>学习要点</h2>
 * {@code confidence} 仅供参考，不能当权限或是否执行写操作的依据；
 * 高风险操作看 {@code needHuman} 与业务规则。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssistantReply {

    /** 给终端用户看的中文回答（必填，由解析器校验） */
    private String answer;

    /** 是否需要人工确认/介入（高风险或信息不足时应为 true） */
    private boolean needHuman;

    /** 建议单据类型，例如「采购订单」；没有则为 null */
    private String suggestedDocType;

    /** 建议必填字段列表；没有则为空数组（永不建议长期保持 null） */
    private List<String> requiredFields = new ArrayList<>();

    /** 模型自评置信度（0~1），仅供参考，不能当权限判断；可为 null */
    private Double confidence;

    /** @return 用户可见回答 */
    public String getAnswer() {
        return answer;
    }

    /** @param answer 中文回答 */
    public void setAnswer(String answer) {
        this.answer = answer;
    }

    /** @return 是否需要人工 */
    public boolean isNeedHuman() {
        return needHuman;
    }

    /** @param needHuman 是否需要人工 */
    public void setNeedHuman(boolean needHuman) {
        this.needHuman = needHuman;
    }

    /** @return 建议单据类型，可能为 null */
    public String getSuggestedDocType() {
        return suggestedDocType;
    }

    /** @param suggestedDocType 单据类型 */
    public void setSuggestedDocType(String suggestedDocType) {
        this.suggestedDocType = suggestedDocType;
    }

    /** @return 必填字段列表 */
    public List<String> getRequiredFields() {
        return requiredFields;
    }

    /**
     * 设置必填字段；{@code null} 时规范为空列表，避免 NPE。
     *
     * @param requiredFields 字段名列表
     */
    public void setRequiredFields(List<String> requiredFields) {
        this.requiredFields = requiredFields == null ? new ArrayList<>() : requiredFields;
    }

    /** @return 置信度，可能为 null */
    public Double getConfidence() {
        return confidence;
    }

    /** @param confidence 0~1 或 null */
    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
}
