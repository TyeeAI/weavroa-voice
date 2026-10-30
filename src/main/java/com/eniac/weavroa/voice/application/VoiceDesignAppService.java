package com.eniac.weavroa.voice.application;

import com.eniac.weavroa.llm.factory.ModelFactory;
import com.eniac.weavroa.llm.model.chat.ChatModel;
import com.eniac.weavroa.llm.model.chat.Message;
import com.eniac.weavroa.llm.model.chat.response.ChatResponse;
import com.eniac.weavroa.voice.application.port.VoiceDesignPort;
import com.eniac.weavroa.voice.domain.TargetModelPolicy;
import com.eniac.weavroa.voice.domain.VoicePipeline;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceDesignAppService {

    private final VoiceDesignPort voiceDesignPort;
    private final ModelFactory modelFactory;

    public VoiceDesignPort.DesignResult createVoice(
            String voicePrompt,
            String previewText,
            String targetModel,
            String preferredName,
            String language) {

        TargetModelPolicy.validateTargetModel(VoicePipeline.VOICE_DESIGN, targetModel);

        String refinedPrompt = refineVoicePrompt(voicePrompt);
        log.info("原始提示词: {}, 润色后: {}", voicePrompt, refinedPrompt);

        return voiceDesignPort.createVoice(
                refinedPrompt,
                previewText,
                targetModel,
                preferredName,
                language,
                24000,
                "wav"
        );
    }

    private String refineVoicePrompt(String originalPrompt) {
        if (originalPrompt == null || originalPrompt.trim().isEmpty()) {
            return "沉稳有力的男声，语速中等，适合新闻播报";
        }
    
        try {
            ChatModel chatModel = modelFactory.chatModel();
                
            String systemPrompt = "你是一位专业的声音设计师。请将用户的声音描述优化为符合阿里云 DashScope 声音设计规范的提示词。\n" +
                    "\n" +
                    "核心原则：\n" +
                    "1. 具体而非模糊：使用'低沉'、'清脆'等具体词汇，避免'好听'等主观词汇\n" +
                    "2. 多维而非单一：结合性别、年龄、音调、语速、情感等多个维度\n" +
                    "3. 客观而非主观：描述声音特征，而非个人喜好\n" +
                    "4. 简洁而非冗余：每个词都有意义，避免重复\n" +
                    "\n" +
                    "描述维度参考：\n" +
                    "- 性别：男性、女性、中性\n" +
                    "- 年龄：儿童、青少年、青年、中年、老年\n" +
                    "- 音调：高音、中音、低音\n" +
                    "- 语速：快速、中速、缓慢\n" +
                    "- 情感：开朗、沉稳、温柔、严肃、活泼\n" +
                    "- 特点：有磁性、清脆、沙哑、圆润、甜美、浑厚\n" +
                    "- 用途：新闻播报、广告配音、有声书、动画角色\n" +
                    "\n" +
                    "示例：\n" +
                    "输入：'硬朗的机械风'\n" +
                    "输出：'沉稳的中年男性，音色低沉有力，带有机械感和电子音效，语速中等偏慢，适合科技产品广告配音'\n" +
                    "\n" +
                    "请只返回优化后的提示词文本，不要添加任何解释。";
    
            String userMessage = "请优化以下声音描述：" + originalPrompt;

            List<Message> messages = Message.builder()
                .system(systemPrompt)
                .user(userMessage)
                .build();

            ChatResponse response = CompletableFuture.supplyAsync(() ->
                chatModel.generate(messages).block()
            ).join();
                
            if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
                log.warn("LLM 返回为空，使用原始提示词");
                return originalPrompt;
            }

            String content = response.getChoices().getFirst().getMessage().getContent();
            if (content == null) {
                log.warn("LLM 返回内容为空，使用原始提示词");
                return originalPrompt;
            }
                
            String refined = content.trim()
                    .replaceAll("^['\"]|['\"]$", "")
                    .replaceAll("^(优化后[:：]?|提示词[:：]?)\\s*", "");

            return refined.isEmpty() ? originalPrompt : refined;
        } catch (Exception e) {
            log.warn("LLM 润色失败，使用原始提示词: {}", e.getMessage());
            return originalPrompt;
        }
    }

    public List<VoiceDesignPort.VoiceInfo> listVoices(Integer pageIndex, Integer pageSize) {
        return voiceDesignPort.listVoices(pageIndex, pageSize);
    }

    public VoiceDesignPort.VoiceInfo queryVoice(String voice) {
        return voiceDesignPort.queryVoice(voice);
    }

    public boolean deleteVoice(String voice) {
        return voiceDesignPort.deleteVoice(voice);
    }
}
