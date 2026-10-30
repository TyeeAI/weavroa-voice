package com.eniac.weavroa.voice.infrastructure.dashscope;

import com.eniac.weavroa.voice.application.port.VoiceEnrollmentPort;
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DashScopeVoiceEnrollmentAdapter implements VoiceEnrollmentPort {

    private final VoiceProperties voiceProperties;
    private final DashScopeRestClient restClient;
    private static final Gson gson = new Gson();

    @Override
    public String createVoice(InputStream audioStream, String fileName, String targetModel,
                              String preferredName, String text, String language) {
        String url = voiceProperties.getBaseUrl() + "/services/audio/tts/customization";

        String cleanName = sanitizeVoiceName(preferredName);
        log.info("原始音色名: {}, 清洗后: {}", preferredName, cleanName);

        JsonObject input = new JsonObject();
        input.addProperty("action", "create");
        input.addProperty("target_model", targetModel);
        input.addProperty("preferred_name", cleanName);

        JsonObject audio = new JsonObject();
        audio.addProperty("data", "__FILE_DATA_URI__");
        input.add("audio", audio);
        
        if (text != null && !text.isEmpty()) {
            input.addProperty("text", text);
        }
        if (language != null && !language.isEmpty()) {
            input.addProperty("language", language);
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("model", "qwen-voice-enrollment");
        payload.add("input", input);

        String jsonPayload = gson.toJson(payload);

        String response = restClient.postWithFile(url, jsonPayload, audioStream, fileName);
        return parseVoice(response);
    }

    @Override
    public List<VoiceInfo> listVoices(Integer pageIndex, Integer pageSize) {
        String url = voiceProperties.getBaseUrl() + "/services/audio/tts/customization";

        String jsonPayload = "{"
                + "\"model\": \"qwen-voice-enrollment\","
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
    public boolean deleteVoice(String voice) {
        String url = voiceProperties.getBaseUrl() + "/services/audio/tts/customization";

        String jsonPayload = "{"
                + "\"model\": \"qwen-voice-enrollment\","
                + "\"input\": {"
                +   "\"action\": \"delete\","
                +   "\"voice\": \"" + voice + "\""
                + "}"
                + "}";

        restClient.post(url, jsonPayload);
        return true;
    }

    private String parseVoice(String response) {
        try {
            JsonObject jsonObject = gson.fromJson(response, JsonObject.class);

            JsonObject output = jsonObject.getAsJsonObject("output");
            if (output == null || !output.has("voice")) {
                throw new RuntimeException("响应中缺少 output.voice: " + response);
            }
            
            return output.get("voice").getAsString();
        } catch (Exception e) {
            log.error("解析 voice 失败: {}", response, e);
            throw new RuntimeException("解析 voice 失败: " + response, e);
        }
    }

    private List<VoiceInfo> parseListResponse(String response) {
        List<VoiceInfo> result = new ArrayList<>();
        return result;
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
