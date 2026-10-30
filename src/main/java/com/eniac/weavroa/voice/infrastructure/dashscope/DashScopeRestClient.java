package com.eniac.weavroa.voice.infrastructure.dashscope;

import com.eniac.weavroa.voice.config.VoiceProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
public class DashScopeRestClient {

    private final VoiceProperties voiceProperties;

    public String post(String url, String jsonPayload) {
        HttpURLConnection conn = null;
        try {
            String apiKey = voiceProperties.getApiKey();
            log.info("DashScope 请求: url={}, payload={}", url, jsonPayload);
            
            URL apiUrl = new URL(url);
            conn = (HttpURLConnection) apiUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(60000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();
            InputStream is = (status >= 200 && status < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }

            if (status != 200) {
                log.error("DashScope API 调用失败: status={}, response={}", status, response);
                throw new RuntimeException("DashScope API 调用失败: " + status + " - " + response);
            }

            log.debug("DashScope API 响应: {}", response);
            return response.toString();

        } catch (Exception e) {
            log.error("DashScope HTTP 请求异常", e);
            throw new RuntimeException("DashScope HTTP 请求失败", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public String postWithFile(String url, String jsonPayload, InputStream fileStream, String fileName) {
        try {
            log.info("文件上传开始: fileName={}, payload长度={}", fileName, jsonPayload.length());
            log.debug("原始 Payload: {}", jsonPayload);
            
            byte[] bytes = fileStream.readAllBytes();
            String base64 = Base64.getEncoder().encodeToString(bytes);
            String mimeType = getMimeType(fileName);
            String dataUri = "data:" + mimeType + ";base64," + base64;

            jsonPayload = jsonPayload.replace("\"__FILE_DATA_URI__\"", "\"" + dataUri + "\"");
            
            log.debug("替换后 Payload: {}", jsonPayload.substring(0, Math.min(200, jsonPayload.length())));

            return post(url, jsonPayload);
        } catch (Exception e) {
            log.error("文件上传失败: {}", fileName, e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    private String getMimeType(String fileName) {
        if (fileName == null) return "audio/mpeg";
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".wav")) return "audio/wav";
        if (lower.endsWith(".m4a")) return "audio/mp4";
        return "audio/mpeg";
    }
}
