package com.eniac.weavroa.llm.model;

import lombok.Getter;

/**
 * 模型能力类型
 */
@Getter
public enum ModelCapability {

    /**
     * 文本生成/对话
     */
    TEXT("文本生成与对话"),

    /**
     * 图像生成
     */
    IMAGE_GEN("图像生成"),

    /**
     * 图像理解
     */
    IMAGE_REC("图像理解与识别"),

    /**
     * 语音识别 (ASR)
     */
    AUDIO_ASR("语音识别"),

    /**
     * 语音合成 (TTS)
     */
    AUDIO_TTS("语音合成"),

    /**
     * 视频生成
     */
    VIDEO_GEN("视频生成"),

    /**
     * 视频理解
     */
    VIDEO_REC("视频理解与识别"),

    /**
     * 文本向量化
     */
    EMBEDDING("文本向量化"),

    /**
     * 深度推理
     */
    REASONING("深度推理与分析"),

    /**
     * 多模态
     */
    MULTIMODAL("多模态"),

    /**
     * 高精度文本提取
     */
    OCR("高精度文本提取"),

    /**
     * 视频生成（通用）
     */
    VIDEO("视频生成"),


    IMAGE_TRANSLATE("图片翻译"),


    IMAGE_GEN_WAN_RPO("图片生成"),

    VIDEO_GEN_WAN_R2V("视频生成"),

    VIDEO_GEN_TEXT_WAN_R2V("文本生成视频");


    /**
     * 能力描述
     */
    private final String description;

    ModelCapability(String description) {
        this.description = description;
    }
}
