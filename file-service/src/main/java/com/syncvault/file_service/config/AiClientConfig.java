package com.syncvault.file_service.config;

import com.syncvault.aisdk.LLMClient;
import com.syncvault.aisdk.LLMClientBuilder;
import com.syncvault.aisdk.Provider;
import com.syncvault.aisdk.client.ResilientLLMClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiClientConfig {

    @Value("${syncvault.ai.openai.api-key}")
    private String apiKey;

    @Value("${syncvault.ai.openai.model}")
    private String model;

    @Bean
    public ResilientLLMClient resilientLLMClient() {
        LLMClient delegate = new LLMClientBuilder()
                .provider(Provider.OPENAI)
                .apiKey(apiKey)
                .model(model)
                .build();
        return ResilientLLMClient.withDefaults(delegate);
    }
}