package com.eniac.weavroa.voice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "weavroa.voice")
public class VoiceProperties {

    private String apiKey;

    private String baseUrl = "https://dashscope.aliyuncs.com/api/v1";

    private String realtimeWsUrl = "wss://dashscope.aliyuncs.com/api-ws/v1/realtime";

    private String cloneModel = "qwen-voice-enrollment";

    private String designModel = "qwen-voice-design";

    private String defaultTtsModel = "qwen-tts";
}
