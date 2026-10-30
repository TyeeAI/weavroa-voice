package com.eniac.weavroa.voice.infrastructure.dashscope;

import com.eniac.weavroa.voice.application.port.SpeechSynthesisPort;
import com.eniac.weavroa.voice.config.VoiceProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class DashScopeSpeechSynthesisAdapter implements SpeechSynthesisPort {

    private final VoiceProperties voiceProperties;
    private final DashScopeRestClient restClient;

    @Override
    public SynthesisResult synthesize(String model, String voice, String text) {
        String url = voiceProperties.getBaseUrl() + "/services/aigc/multimodal-generation/generation";

        String jsonPayload = "{"
                + "\"model\": \"" + model + "\","
                + "\"input\": {"
                + "\"text\": \"" + escapeJson(text) + "\","
                + "\"voice\": \"" + voice + "\""
                + "}"
                + "}";

        log.debug("语音合成请求: model={}, voice={}, text长度={}", model, voice, text.length());
        
        String response = restClient.post(url, jsonPayload);
        log.info("DashScope 响应: {}", response);

        String audioUrl = extractAudioUrl(response);
        String requestId = extractRequestId(response);
        
        log.info("解析结果: audioUrl={}, requestId={}", audioUrl, requestId);

        return new SynthesisResult(audioUrl, requestId);
    }

    @Override
    public Flux<AudioChunk> synthesizeStream(String model, String voice, String text) {
        return Flux.<AudioChunk>create(sink -> {
            try {
                log.info("开始流式合成: model={}, voice={}", model, voice);
                
                String url = voiceProperties.getBaseUrl() + "/services/aigc/multimodal-generation/generation";

                String jsonPayload = "{"
                        + "\"model\": \"" + model + "\","
                        + "\"input\": {"
                        + "\"text\": \"" + escapeJson(text) + "\","
                        + "\"voice\": \"" + voice + "\""
                        + "}"
                        + "}";

                sink.next(new AudioChunk(
                    "{\"type\":\"session_started\",\"text\":\"" + escapeJson(text) + "\"}",
                    Instant.now().toEpochMilli(),
                    false
                ));

                callDashScopeStream(url, jsonPayload, sink);

                sink.next(new AudioChunk(
                    "{\"type\":\"session_completed\"}",
                    Instant.now().toEpochMilli(),
                    true
                ));
                sink.complete();
                
            } catch (Exception e) {
                log.error("流式合成失败", e);
                sink.error(e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    private void callDashScopeStream(String url, String jsonPayload, reactor.core.publisher.FluxSink<AudioChunk> sink) {
        try {
            java.net.URL apiUrl = new java.net.URL(url);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) apiUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + voiceProperties.getApiKey());
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("X-DashScope-SSE", "enable");
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(60000);

            try (java.io.OutputStream os = conn.getOutputStream()) {
                os.write(jsonPayload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }
            
            int status = conn.getResponseCode();
            if (status != 200) {
                log.error("DashScope 流式请求失败: status={}", status);
                return;
            }

            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
                                
                String line;
                int chunkCount = 0;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data:")) {
                        String jsonData = line.substring(5).trim();
                        if (!jsonData.isEmpty()) {
                            AudioChunk chunk = parseStreamResponse(jsonData);
                            if (chunk != null) {
                                chunkCount++;
                                log.debug("发送第 {} 个音频块", chunkCount);
                                sink.next(chunk);
                            }
                        }
                    }
                }
                log.info("流式合成完成，共发送 {} 个音频块", chunkCount);
            }
            
            conn.disconnect();
            
        } catch (Exception e) {
            log.error("调用 DashScope 流式 API 失败", e);
            throw new RuntimeException("流式 API 调用失败", e);
        }
    }
    
    private AudioChunk parseStreamResponse(String jsonData) {
        try {
            log.debug("收到流式数据: {}", jsonData);

            int dataStart = jsonData.indexOf("\"data\"");
            if (dataStart == -1) {
                log.debug("元数据: {}", jsonData);
                return new AudioChunk(jsonData, Instant.now().toEpochMilli(), false);
            }
            
            int colonPos = jsonData.indexOf(":", dataStart);
            int valueStart = jsonData.indexOf("\"", colonPos) + 1;
            int valueEnd = jsonData.indexOf("\"", valueStart);
            
            if (valueStart > 0 && valueEnd > valueStart) {
                String base64Audio = jsonData.substring(valueStart, valueEnd);
                log.debug("音频数据块: 长度={}", base64Audio.length());
                return new AudioChunk(base64Audio, Instant.now().toEpochMilli(), false);
            }
            
            return null;
        } catch (Exception e) {
            log.warn("解析流式响应失败: {}", jsonData, e);
            return null;
        }
    }

    private String extractAudioUrl(String response) {
        try {
            int urlStart = response.indexOf("\"url\"");
            if (urlStart == -1) {
                log.warn("响应中未找到 url 字段");
                return null;
            }

            int colonPos = response.indexOf(":", urlStart);
            int valueStart = response.indexOf("\"", colonPos) + 1;
            int valueEnd = response.indexOf("\"", valueStart);
            
            if (valueStart > 0 && valueEnd > valueStart) {
                return response.substring(valueStart, valueEnd);
            }
            
            return null;
        } catch (Exception e) {
            log.error("解析 audioUrl 失败", e);
            return null;
        }
    }

    private String extractRequestId(String response) {
        try {
            int requestIdStart = response.indexOf("\"request_id\"");
            if (requestIdStart == -1) {
                log.warn("响应中未找到 request_id 字段");
                return null;
            }
            
            int colonPos = response.indexOf(":", requestIdStart);
            int valueStart = response.indexOf("\"", colonPos) + 1;
            int valueEnd = response.indexOf("\"", valueStart);
            
            if (valueStart > 0 && valueEnd > valueStart) {
                return response.substring(valueStart, valueEnd);
            }
            
            return null;
        } catch (Exception e) {
            log.error("解析 requestId 失败", e);
            return null;
        }
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
