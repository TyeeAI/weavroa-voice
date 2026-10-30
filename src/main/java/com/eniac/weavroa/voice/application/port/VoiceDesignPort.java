package com.eniac.weavroa.voice.application.port;

import java.util.List;

public interface VoiceDesignPort {

    DesignResult createVoice(String voicePrompt, String previewText, String targetModel,
                             String preferredName, String language, Integer sampleRate, String responseFormat);

    List<VoiceInfo> listVoices(Integer pageIndex, Integer pageSize);

    VoiceInfo queryVoice(String voice);

    boolean deleteVoice(String voice);

    record DesignResult(String voice, String previewAudioBase64, String targetModel) {
    }

    record VoiceInfo(String voice, String gmtCreate, String targetModel) {
    }
}
