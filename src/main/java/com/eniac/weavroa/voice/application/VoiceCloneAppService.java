package com.eniac.weavroa.voice.application;

import com.eniac.weavroa.voice.application.port.VoiceEnrollmentPort;
import com.eniac.weavroa.voice.domain.TargetModelPolicy;
import com.eniac.weavroa.voice.domain.VoicePipeline;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VoiceCloneAppService {

    private final VoiceEnrollmentPort voiceEnrollmentPort;

    public String createVoice(
            InputStream audioStream,
            String fileName,
            String targetModel,
            String preferredName,
            String text,
            String language) {

        TargetModelPolicy.validateTargetModel(VoicePipeline.VOICE_CLONE, targetModel);

        return voiceEnrollmentPort.createVoice(audioStream, fileName, targetModel, preferredName, text, language);
    }

    public List<VoiceEnrollmentPort.VoiceInfo> listVoices(Integer pageIndex, Integer pageSize) {
        return voiceEnrollmentPort.listVoices(pageIndex, pageSize);
    }

    public boolean deleteVoice(String voice) {
        return voiceEnrollmentPort.deleteVoice(voice);
    }
}
