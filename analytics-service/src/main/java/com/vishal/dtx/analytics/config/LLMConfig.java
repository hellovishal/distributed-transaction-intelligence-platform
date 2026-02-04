package com.vishal.dtx.analytics.config;

import com.vishal.dtx.analytics.llm.LLMService;
import com.vishal.dtx.analytics.llm.OllamaLLMService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class LLMConfig {

    @Bean
    public LLMService llmService(RestTemplate restTemplate) {
        return new OllamaLLMService(restTemplate);
    }
}
