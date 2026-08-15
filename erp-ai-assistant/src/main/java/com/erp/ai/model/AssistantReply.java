package com.erp.ai.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 助手结构化回复（业务侧使用的对象）。
 * <p>
 * 与提示词中要求的 JSON Schema 对应：
 * <pre>
 * {
 *   "answer": "...",
 *   "need_human": true/false,
 *   "suggested_doc_type": "...",
 *   "required_fields": [],
 *   "confidence": 0.0~1.0
 * }
 * </pre>
 * {@code @JsonIgnoreProperties(ignoreUnknown = true)}：忽略模型多返回的未知字段，增强容错。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssistantReply {

    /** 给终端用户看的中文回答 */
    private String answer;

    /** 是否需要人工确认/介入（高风险或信息不足时应为 true） */
    private boolean needHuman;

    /** 建议单据类型，例如“采购订单”；没有则为 null */
    private String suggestedDocType;

    /** 建议必填字段列表；没有则为空数组 */
    private List<String> requiredFields = new ArrayList<>();

    /** 模型自评置信度（0~1），仅供参考，不能当权限判断 */
    private Double confidence;

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public boolean isNeedHuman() {
        return needHuman;
    }

    public void setNeedHuman(boolean needHuman) {
        this.needHuman = needHuman;
    }

    public String getSuggestedDocType() {
        return suggestedDocType;
    }

    public void setSuggestedDocType(String suggestedDocType) {
        this.suggestedDocType = suggestedDocType;
    }

    public List<String> getRequiredFields() {
        return requiredFields;
    }

    public void setRequiredFields(List<String> requiredFields) {
        this.requiredFields = requiredFields == null ? new ArrayList<>() : requiredFields;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
}
