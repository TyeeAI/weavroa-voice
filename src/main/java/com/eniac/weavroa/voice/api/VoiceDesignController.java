package com.eniac.weavroa.voice.api;

import com.eniac.weavroa.voice.application.VoiceDesignAppService;
import com.eniac.weavroa.voice.application.port.VoiceDesignPort;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API - 声音设计
 */
@RestController
@RequestMapping("/api/v1/voices/design")
@RequiredArgsConstructor
public class VoiceDesignController {

    private final VoiceDesignAppService voiceDesignAppService;

    /**
     * 创建设计音色
     */
    @PostMapping
    public ResponseEntity<ApiResponse<VoiceDesignPort.DesignResult>> createVoice(
            @RequestBody CreateVoiceRequest request) {

        VoiceDesignPort.DesignResult result = voiceDesignAppService.createVoice(
                request.getVoicePrompt(),
                request.getPreviewText(),
                request.getTargetModel(),
                request.getPreferredName(),
                request.getLanguage()
        );

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 查询设计音色列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<VoiceDesignPort.VoiceInfo>>> listVoices(
            @RequestParam(value = "pageIndex", defaultValue = "0") Integer pageIndex,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {

        List<VoiceDesignPort.VoiceInfo> voices = voiceDesignAppService.listVoices(pageIndex, pageSize);
        return ResponseEntity.ok(ApiResponse.success(voices));
    }

    /**
     * 查询单个设计音色
     */
    @GetMapping("/{voice}")
    public ResponseEntity<ApiResponse<VoiceDesignPort.VoiceInfo>> queryVoice(@PathVariable String voice) {
        VoiceDesignPort.VoiceInfo info = voiceDesignAppService.queryVoice(voice);
        return ResponseEntity.ok(ApiResponse.success(info));
    }

    /**
     * 删除设计音色
     */
    @DeleteMapping("/{voice}")
    public ResponseEntity<ApiResponse<Boolean>> deleteVoice(@PathVariable String voice) {
        boolean result = voiceDesignAppService.deleteVoice(voice);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Data
    public static class CreateVoiceRequest {
        private String voicePrompt;
        private String previewText;
        private String targetModel;
        private String preferredName;
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
    }
}
