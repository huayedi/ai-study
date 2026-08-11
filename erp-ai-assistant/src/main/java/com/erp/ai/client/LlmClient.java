package com.erp.ai.client;

import com.erp.ai.model.ChatMessage;

import java.util.List;

public interface LlmClient {

    String providerName();

    LlmResult chat(List<ChatMessage> messages);
}
