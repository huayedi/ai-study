package com.erp.ai.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 单条对话消息，对应 Chat Completions 的 messages 元素。
 * <p>
 * role 常见取值：
 * <ul>
 *   <li>{@code system}：系统提示词，约束助手身份与输出格式</li>
 *   <li>{@code user}：用户输入</li>
 *   <li>{@code assistant}：模型历史回复</li>
 * </ul>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatMessage {

    /** 消息角色：system / user / assistant */
    private String role;

    /** 消息文本内容 */
    private String content;

    public ChatMessage() {
    }

    public ChatMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
