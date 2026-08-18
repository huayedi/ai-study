package com.erp.ai.rag.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * RAG 问答请求 DTO。
 * <p>
 * <b>职责</b>：承载用户自然语言问题，作为 {@link com.erp.ai.rag.controller.RagController}
 * 入参进入 retrieve 阶段。
 * <p>
 * <b>RAG 流水线位置</b>：API 输入（load/chunk 已在启动完成；本对象触发 retrieve 起的在线路径）。
 * <p>
 * <b>对应 Day</b>：Day10+ RAG Ask API。
 * <p>
 * <b>与其他类关系</b>：被 Controller 校验后交给 {@link com.erp.ai.rag.service.RagService#ask}；
 * 不直接依赖检索器实现。
 */
public class RagAskRequest {

    /**
     * 用户问题正文；不可为空白。
     * <p>
     * 空问题在 Bean Validation 层即失败；若绕过校验，检索器也会因空白返回空命中。
     */
    @NotBlank(message = "question 不能为空")
    private String question;

    /**
     * @return 用户问题；可能尚未通过校验
     */
    public String getQuestion() {
        return question;
    }

    /**
     * @param question 用户问题字符串
     */
    public void setQuestion(String question) {
        this.question = question;
    }
}
