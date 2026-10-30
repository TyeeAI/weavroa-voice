package com.eniac.weavroa.voice.api;

import com.eniac.weavroa.voice.application.TtsAppService;
import com.eniac.weavroa.voice.application.port.SpeechSynthesisPort;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * REST API - 语音合成（支持非流式和流式）
 */
@RestController
@RequestMapping("/api/v1/tts")
@RequiredArgsConstructor
public class TtsController {

    private final TtsAppService ttsAppService;

    /**
     * 非流式语音合成（支持系统音色和自定义音色）
     */
    @PostMapping("/synthesize")
    public ResponseEntity<ApiResponse<SpeechSynthesisPort.SynthesisResult>> synthesize(
            @RequestBody SynthesisRequest request) {

        SpeechSynthesisPort.SynthesisResult result = ttsAppService.synthesize(
                request.getModel(),
                request.getVoice(),
                request.getText()
        );


        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 查询支持的音色列表
     */
    @GetMapping("/voices")
    public ResponseEntity<ApiResponse<VoiceListResponse>> getSupportedVoices() {
        VoiceListResponse response = new VoiceListResponse();
        response.setVoices(SUPPORTED_VOICES);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 流式语音合成（SSE）
     * 前端使用 EventSource 或 fetch + ReadableStream 接收数据
     */
    @GetMapping(value = "/synthesize/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<SpeechSynthesisPort.AudioChunk> synthesizeStream(
            @RequestParam String model,
            @RequestParam String voice,
            @RequestParam String text) {

        return ttsAppService.synthesizeStream(model, voice, text);
    }

    /**
     * 流式语音合成（POST 方式，适合长文本）
     */
    @PostMapping(value = "/synthesize/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<SpeechSynthesisPort.AudioChunk> synthesizeStreamPost(
            @RequestBody SynthesisRequest request) {

        return ttsAppService.synthesizeStream(
                request.getModel(),
                request.getVoice(),
                request.getText()
        );
    }

    @Data
    public static class SynthesisRequest {
        private String model;
        private String voice;
        private String text;
        private String languageType; // 可选，建议与文本语种一致
    }

    @Data
    public static class VoiceInfo {
        private String voice;      // 音色参数值
        private String name;       // 音色名称
        private String description; // 音色描述
        private String gender;     // 性别：male/female
        private List<String> supportedModels; // 支持的模型列表
    }

    @Data
    public static class VoiceListResponse {
        private List<VoiceInfo> voices;
    }

    // 系统预设音色列表（根据阿里云文档）
    private static final List<VoiceInfo> SUPPORTED_VOICES = List.of(
        new VoiceInfo() {{
            setVoice("Cherry");
            setName("芊悦");
            setDescription("阳光积极、亲切自然小姐姐");
            setGender("female");
            setSupportedModels(List.of("qwen3-tts-instruct-flash", "qwen3-tts-flash", "qwen-tts"));
        }},
        new VoiceInfo() {{
            setVoice("Serena");
            setName("苏瑶");
            setDescription("温柔小姐姐");
            setGender("female");
            setSupportedModels(List.of("qwen3-tts-instruct-flash", "qwen3-tts-flash", "qwen-tts"));
        }},
        new VoiceInfo() {{
            setVoice("Ethan");
            setName("晨煦");
            setDescription("标准普通话，带部分北方口音。阳光、温暖、活力、朝气");
            setGender("male");
            setSupportedModels(List.of("qwen3-tts-instruct-flash", "qwen3-tts-flash", "qwen-tts"));
        }},
        new VoiceInfo() {{
            setVoice("Chelsie");
            setName("千雪");
            setDescription("二次元虚拟女友");
            setGender("female");
            setSupportedModels(List.of("qwen3-tts-instruct-flash", "qwen3-tts-flash", "qwen-tts"));
        }},
        new VoiceInfo() {{
            setVoice("Momo");
            setName("茉兔");
            setDescription("撒娇搞怪，逗你开心");
            setGender("female");
            setSupportedModels(List.of("qwen3-tts-instruct-flash", "qwen3-tts-flash"));
        }},
        new VoiceInfo() {{
            setVoice("Vivian");
            setName("十三");
            setDescription("拽拽的、可爱的小暴躁");
            setGender("female");
            setSupportedModels(List.of("qwen3-tts-instruct-flash", "qwen3-tts-flash"));
        }},
        new VoiceInfo() {{
            setVoice("Moon");
            setName("月白");
            setDescription("率性帅气");
            setGender("male");
            setSupportedModels(List.of("qwen3-tts-instruct-flash", "qwen3-tts-flash"));
        }},
        new VoiceInfo() {{
            setVoice("Maia");
            setName("四月");
            setDescription("知性与温柔的碰撞");
            setGender("female");
            setSupportedModels(List.of("qwen3-tts-instruct-flash", "qwen3-tts-flash"));
        }},
        new VoiceInfo() {{
            setVoice("Kai");
            setName("凯");
            setDescription("耳朵的一场SPA");
            setGender("male");
            setSupportedModels(List.of("qwen3-tts-instruct-flash", "qwen3-tts-flash"));
        }},
        new VoiceInfo() {{
            setVoice("Nofish");
            setName("不吃鱼");
            setDescription("不会翘舌音的设计师");
            setGender("male");
            setSupportedModels(List.of("qwen3-tts-instruct-flash", "qwen3-tts-flash"));
        }}
        // 更多音色可以根据需要添加...
    );

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
