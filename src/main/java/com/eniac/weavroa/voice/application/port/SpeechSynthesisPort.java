package com.eniac.weavroa.voice.application.port;

import reactor.core.publisher.Flux;

public interface SpeechSynthesisPort {

    SynthesisResult synthesize(String model, String voice, String text);

    Flux<AudioChunk> synthesizeStream(String model, String voice, String text);

    record SynthesisResult(String audioUrl, String requestId) {
    }

    record AudioChunk(String data, Long timestamp, boolean isLast) {
    }
}
