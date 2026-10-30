package com.eniac.weavroa.llm.config;

import com.eniac.weavroa.configuration.http.WebHttpClient;
import com.eniac.weavroa.llm.factory.ModelFactory;
import com.eniac.weavroa.llm.factory.registry.DefaultModelRegistry;
import com.eniac.weavroa.llm.factory.registry.ModelRegistry;
import com.eniac.weavroa.llm.service.ModelService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LLM 模块自动装配
 */
@Configuration
@EnableConfigurationProperties(ModelConfigure.class)
public class LLMAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ModelFactory modelFactory(WebHttpClient httpClient, ModelConfigure configuration) {
        ModelRegistry modelRegistry = new DefaultModelRegistry(httpClient);
        ModelFactory factory = new ModelFactory(modelRegistry, configuration);
        factory.init();
        return factory;
    }

    @Bean
    @ConditionalOnMissingBean
    public ModelService modelService(ModelFactory modelFactory) {
        return new ModelService(modelFactory);
    }
}
