package com.eniac.weavroa.voice.application.port;

import java.io.InputStream;
import java.util.List;

public interface VoiceEnrollmentPort {

    String createVoice(InputStream audioStream, String fileName, String targetModel,
                       String preferredName, String text, String language);

    List<VoiceInfo> listVoices(Integer pageIndex, Integer pageSize);

    boolean deleteVoice(String voice);

    record VoiceInfo(String voice, String gmtCreate, String targetModel) {
    }
}
