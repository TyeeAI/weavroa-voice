package com.eniac.weavroa.voice.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class VoiceAsset {

    private String voiceId;

    private VoicePipeline pipeline;

    private String targetModel;

    private String preferredName;

    private String businessUserId;

    private LocalDateTime createdAt;

    private LocalDateTime lastSynthesisAt;

    private String requestId;
}
