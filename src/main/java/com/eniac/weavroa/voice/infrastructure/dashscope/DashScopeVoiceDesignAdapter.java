package com.eniac.weavroa.voice.infrastructure.dashscope;

import com.eniac.weavroa.voice.application.port.VoiceDesignPort;
import com.eniac.weavroa.voice.config.VoiceProperties;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DashScopeVoiceDesignAdapter implements VoiceDesignPort {

    private final VoiceProperties voiceProperties;
    private final DashScopeRestClient restClient;
    private static final Gson gson = new Gson();

    @Override
    public DesignResult createVoice(String voicePrompt, String previewText, String targetModel,
                                    String preferredName, String language, Integer sampleRate, String responseFormat) {
        String url = voiceProperties.getBaseUrl() + "/services/audio/tts/customization";

        String cleanName = sanitizeVoiceName(preferredName);
        log.info("声音设计请求: targetModel={}, preferredName={}, cleanName={}", targetModel, preferredName, cleanName);

        JsonObject input = new JsonObject();
        input.addProperty("action", "create");
        input.addProperty("target_model", targetModel);
        input.addProperty("voice_prompt", voicePrompt);
        input.addProperty("preview_text", previewText);
        input.addProperty("preferred_name", cleanName);
        input.addProperty("language", language != null ? language : "zh");

        JsonObject parameters = new JsonObject();
        parameters.addProperty("sample_rate", sampleRate != null ? sampleRate : 24000);
        parameters.addProperty("response_format", responseFormat != null ? responseFormat : "wav");

        JsonObject payload = new JsonObject();
        payload.addProperty("model", "qwen-voice-design");
        payload.add("input", input);
        payload.add("parameters", parameters);

        String jsonPayload = gson.toJson(payload);
        log.debug("JSON Payload: {}", jsonPayload);

        String response = restClient.post(url, jsonPayload);
        return parseCreateResponse(response);
    }

    @Override
    public List<VoiceInfo> listVoices(Integer pageIndex, Integer pageSize) {
        String url = voiceProperties.getBaseUrl() + "/services/audio/tts/customization";

        String jsonPayload = "{"
                + "\"model\": \"qwen-voice-design\","
                + "\"input\": {"
                +   "\"action\": \"list\","
                +   "\"page_index\": " + (pageIndex != null ? pageIndex : 0) + ","
                +   "\"page_size\": " + (pageSize != null ? pageSize : 10)
                + "}"
                + "}";

        String response = restClient.post(url, jsonPayload);
        return parseListResponse(response);
    }

    @Override
    public VoiceInfo queryVoice(String voice) {
        String url = voiceProperties.getBaseUrl() + "/services/audio/tts/customization";

        String jsonPayload = "{"
                + "\"model\": \"qwen-voice-design\","
                + "\"input\": {"
                +   "\"action\": \"query\","
                +   "\"voice\": \"" + voice + "\""
                + "}"
                + "}";

        String response = restClient.post(url, jsonPayload);
        return parseQueryResponse(response);
    }

    @Override
    public boolean deleteVoice(String voice) {
        String url = voiceProperties.getBaseUrl() + "/services/audio/tts/customization";

        String jsonPayload = "{"
                + "\"model\": \"qwen-voice-design\","
                + "\"input\": {"
                +   "\"action\": \"delete\","
                +   "\"voice\": \"" + voice + "\""
                + "}"
                + "}";

        restClient.post(url, jsonPayload);
        return true;
    }

    private DesignResult parseCreateResponse(String response) {
        try {
            JsonObject jsonObject = gson.fromJson(response, JsonObject.class);

            if (jsonObject.has("output")) {
                JsonObject output = jsonObject.getAsJsonObject("output");

                if (output.has("fallback_mode") && output.get("fallback_mode").getAsBoolean()) {
                    String fallbackReason = output.has("fallback_reason") ? 
                            output.get("fallback_reason").getAsString() : "unknown";
                    log.warn("声音设计降级，原因: {}", fallbackReason);
                }
                
                String voice = output.has("voice") ? output.get("voice").getAsString() : null;
                String targetModel = output.has("target_model") ? output.get("target_model").getAsString() : null;

                String previewAudio = null;
                if (output.has("preview_audio")) {
                    if (output.get("preview_audio").isJsonPrimitive()) {
                        previewAudio = output.get("preview_audio").getAsString();
                    } else if (output.get("preview_audio").isJsonObject()) {
                        JsonObject audioObj = output.getAsJsonObject("preview_audio");
                        previewAudio = audioObj.has("data") ? audioObj.get("data").getAsString() : null;
                    }
                }
                
                log.info("声音设计成功: voice={}, targetModel={}", voice, targetModel);
                return new DesignResult(voice, previewAudio, targetModel);
            } else {
                log.error("响应中缺少 output: {}", response);
                return new DesignResult(null, null, null);
            }
        } catch (Exception e) {
            log.error("解析声音设计响应失败: {}", response, e);
            return new DesignResult(null, null, null);
        }
    }

    private List<VoiceInfo> parseListResponse(String response) {
        List<VoiceInfo> result = new ArrayList<>();
        return result;
    }

    private VoiceInfo parseQueryResponse(String response) {
        String voice = extractJsonField(response, "\"voice\"");
        String gmtCreate = extractJsonField(response, "\"gmt_create\"");
        String targetModel = extractJsonField(response, "\"target_model\"");
        return new VoiceInfo(voice, gmtCreate, targetModel);
    }

    private String extractJsonField(String json, String field) {
        int start = json.indexOf(field + "\": \"") + field.length() + 4;
        if (start < field.length() + 4) return null;
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    private String sanitizeVoiceName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "voice_" + System.currentTimeMillis() % 1000000;
        }

        String pinyinName = convertChineseToPinyin(name);

        String cleaned = pinyinName.replaceAll("[^a-zA-Z0-9_]", "");

        if (cleaned.isEmpty()) {
            cleaned = "voice_" + System.currentTimeMillis() % 1000000;
        }

        if (cleaned.length() > 32) {
            cleaned = cleaned.substring(0, 32);
        }

        return cleaned.toLowerCase();
    }

    private String convertChineseToPinyin(String chinese) {
        if (chinese == null || chinese.isEmpty()) {
            return "";
        }

        StringBuilder pinyin = new StringBuilder();
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

        for (char c : chinese.toCharArray()) {
            if (Character.toString(c).matches("[\u4e00-\u9fa5]")) {
                try {
                    String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, format);
                    if (pinyinArray != null && pinyinArray.length > 0) {
                        pinyin.append(pinyinArray[0]);
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    log.warn("拼音转换失败: {}", c, e);
                }
            } else if (Character.isLetterOrDigit(c) || c == '_') {
                pinyin.append(c);
            }
        }

        return pinyin.toString();
    }
}
