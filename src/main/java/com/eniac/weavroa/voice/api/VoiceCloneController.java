package com.eniac.weavroa.voice.api;

import com.eniac.weavroa.voice.application.VoiceCloneAppService;
import com.eniac.weavroa.voice.application.port.VoiceEnrollmentPort;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST API - 声音复刻
 */
@RestController
@RequestMapping("/api/v1/voices/clone")
@RequiredArgsConstructor
public class VoiceCloneController {

    private final VoiceCloneAppService voiceCloneAppService;

    /**
     * 创建复刻音色
     */
    @PostMapping
    public ResponseEntity<ApiResponse<String>> createVoice(
            @RequestParam("file") MultipartFile file,
            @RequestParam("targetModel") String targetModel,
            @RequestParam("preferredName") String preferredName,
            @RequestParam(value = "text", required = false) String text,
            @RequestParam(value = "language", required = false) String language) throws Exception {

        String voice = voiceCloneAppService.createVoice(
                file.getInputStream(),
                file.getOriginalFilename(),
                targetModel,
                preferredName,
                text,
                language
        );

        return ResponseEntity.ok(ApiResponse.success(voice));
    }

    /**
     * 查询复刻音色列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<VoiceEnrollmentPort.VoiceInfo>>> listVoices(
            @RequestParam(value = "pageIndex", defaultValue = "0") Integer pageIndex,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {

        List<VoiceEnrollmentPort.VoiceInfo> voices = voiceCloneAppService.listVoices(pageIndex, pageSize);
        return ResponseEntity.ok(ApiResponse.success(voices));
    }

    /**
     * 创建复刻音色（JSON + Base64 方式）
     */
    @PostMapping("/base64")
    public ResponseEntity<ApiResponse<String>> createVoiceWithBase64(
            @RequestBody CreateVoiceRequest request) throws Exception {

        // 将 Base64 转换为 InputStream
        byte[] audioBytes = java.util.Base64.getDecoder().decode(request.getAudioBase64());
        java.io.InputStream audioStream = new java.io.ByteArrayInputStream(audioBytes);

        String voice = voiceCloneAppService.createVoice(
                audioStream,
                request.getFileName() != null ? request.getFileName() : "audio.wav",
                request.getTargetModel(),
                request.getPreferredName(),
                request.getText(),
                request.getLanguage()
        );

        return ResponseEntity.ok(ApiResponse.success(voice));
    }

    /**
     * 删除复刻音色
     */
    @DeleteMapping("/{voice}")
    public ResponseEntity<ApiResponse<Boolean>> deleteVoice(@PathVariable String voice) {
        boolean result = voiceCloneAppService.deleteVoice(voice);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * JSON 请求体
     */
    @Data
    public static class CreateVoiceRequest {
        private String audioBase64;  // Base64 编码的音频数据
        private String fileName;     // 文件名（可选）
        private String targetModel;
        private String preferredName;
        private String text;
        private String language;
    }

    @Data
    public static class ApiResponse<T> {
        private Integer code;
        private String message;
        private T data;

        public static <T> ApiResponse<T> success(T data) {
            ApiResponse<T> response = new ApiResponse<>();
            response.setCode(200);
            response.setMessage("成功");
            response.setData(data);
            return response;
        }

        public static <T> ApiResponse<T> error(String message) {
            ApiResponse<T> response = new ApiResponse<>();
            response.setCode(500);
            response.setMessage(message);
            return response;
        }
    }
}
