package com.erp.ai.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AssistantReply {

    private String answer;
    private boolean needHuman;
    private String suggestedDocType;
    private List<String> requiredFields = new ArrayList<>();
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
