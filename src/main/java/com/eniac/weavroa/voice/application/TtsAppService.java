package com.eniac.weavroa.voice.application;

import com.eniac.weavroa.voice.application.port.SpeechSynthesisPort;
import com.eniac.weavroa.voice.domain.TargetModelPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class TtsAppService {

    private final SpeechSynthesisPort speechSynthesisPort;

    public SpeechSynthesisPort.SynthesisResult synthesize(String model, String voice, String text) {
        TargetModelPolicy.validateModelConsistency(model, model);

        return speechSynthesisPort.synthesize(model, voice, text);
    }

    public Flux<SpeechSynthesisPort.AudioChunk> synthesizeStream(String model, String voice, String text) {
        TargetModelPolicy.validateModelConsistency(model, model);

        return speechSynthesisPort.synthesizeStream(model, voice, text);
    }
}
