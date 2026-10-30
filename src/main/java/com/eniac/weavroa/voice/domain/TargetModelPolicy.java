package com.eniac.weavroa.voice.domain;

import java.util.List;
import java.util.Map;

public class TargetModelPolicy {

    private static final List<String> CLONE_ALLOW_LIST = List.of(
            "qwen3-tts-vc-2026-01-22"
    );

    private static final List<String> DESIGN_ALLOW_LIST = List.of(
            "qwen3-tts-vd-2026-01-26"
    );

    private static final Map<VoicePipeline, List<String>> WHITE_LIST_MAP = Map.of(
            VoicePipeline.VOICE_CLONE, CLONE_ALLOW_LIST,
            VoicePipeline.VOICE_DESIGN, DESIGN_ALLOW_LIST
    );

    public static final String CLONE_MODEL = "qwen-voice-enrollment";

    public static final String DESIGN_MODEL = "qwen-voice-design";

    public static void validateTargetModel(VoicePipeline pipeline, String targetModel) {
        List<String> allowList = WHITE_LIST_MAP.get(pipeline);
        if (allowList == null || !allowList.contains(targetModel)) {
            throw new IllegalArgumentException(
                    String.format("管线 [%s] 不支持的 target_model [%s]，允许值：%s", pipeline, targetModel, allowList));
        }
    }

    public static void validateModelConsistency(String targetModel, String model) {
        if (!targetModel.equals(model)) {
            throw new IllegalArgumentException(
                    String.format("合成 model [%s] 必须与创建时的 target_model [%s] 完全一致", model, targetModel));
        }
    }

    public static String getCreateModel(VoicePipeline pipeline) {
        return switch (pipeline) {
            case VOICE_CLONE -> CLONE_MODEL;
            case VOICE_DESIGN -> DESIGN_MODEL;
        };
    }

    public static String getDefaultTargetModel(VoicePipeline pipeline) {
        return switch (pipeline) {
            case VOICE_CLONE -> "qwen3-tts-vc-2026-01-22";
            case VOICE_DESIGN -> "qwen3-tts-vd-2026-01-26";
        };
    }
}
