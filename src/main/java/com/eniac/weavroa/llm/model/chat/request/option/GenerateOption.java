package com.eniac.weavroa.llm.model.chat.request.option;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * 生成过程中的约定参数
 */
@Getter
@SuperBuilder
public class GenerateOption {

    public static GenerateOption DEFAULT = GenerateOption.builder().build();

    /**
     * 采样温度 (0-2)
     * 采样温度，控制模型生成文本的多样性。
     * temperature越高，生成的文本更多样，反之，生成的文本更确定。
     * 取值范围： [0, 2)
     * temperature与top_p均可以控制生成文本的多样性，建议只设置其中一个值。
     */
    @Builder.Default
    private Double temperature = 1d;

    /**
     * 核采样概率 (0-1)
     * 核采样的概率阈值，控制模型生成文本的多样性。
     * top_p越高，生成的文本更多样。反之，生成的文本更确定。
     * 取值范围：（0,1.0]
     * temperature与top_p均可以控制生成文本的多样性，建议只设置其中一个值。
     */
    @Builder.Default
    @JsonProperty("top_p")
    private Double topP = 0.5d;

    /**
     * Top-K 采样
     * 指定生成过程中用于采样的候选 Token 数量。值越大，输出越随机；值越小，输出越确定。
     * 若设为 null 或大于 100，则禁用 top_k 策略，仅 top_p 策略生效。取值必须为大于或等于 0 的整数。
     * 该参数非OpenAI标准参数。
     */
    @Builder.Default
    @JsonProperty("top_k")
    private Integer topK = 50;

    /**
     * 存在惩罚 (-2.0 到 2.0)
     * 控制模型生成文本时的内容重复度。
     * 取值范围：[-2.0, 2.0]。正值降低重复度，负值增加重复度。
     * 在创意写作或头脑风暴等需要多样性、趣味性或创造力的场景中，建议调高该值；
     * 在技术文档或正式文本等强调一致性与术语准确性的场景中，建议调低该值。
     */
    @Builder.Default
    @JsonProperty("presence_penalty")
    private Double presencePenalty = 0d;

    /**
     * （可选）
     * 最大生成 token 数
     */
    @JsonProperty("max_tokens")
    private Integer maxTokens;

    /**
     * （可选）默认值为false
     * 是否将输入图像的像素上限提升至 16384 Token 对应的像素值。相关文档：处理高分辨率图像。
     * vl_high_resolution_images：true，使用固定分辨率策略，忽略 max_pixels 设置，超过此分辨率时会将图像总像素缩小至此上限内。
     * vl_high_resolution_images为false，实际分辨率由 max_pixels 与默认上限共同决定，取二者计算结果的最大值。
     * 超过此像素上限时会将图像缩小至此上限内。
     * 该参数非OpenAI标准参数。
     */
    @JsonProperty("vl_high_resolution_images")
    private boolean vlHighResolutionImages;

    /**
     * （可选）
     * 生成响应的数量，取值范围是1-4。适用于需生成多个候选响应的场景，例如创意写作或广告文案。
     * 若传入tools参数，请将n设为 1。
     * 增大 n 会增加输出 Token 的消耗，但不增加输入 Token 消耗。
     */
    @Builder.Default
    private Integer n = 1;

    /**
     * （可选）
     * 随机数种子。用于确保在相同输入和参数下生成结果可复现。
     * 若调用时传入相同的 seed 且其他参数不变，模型将尽可能返回相同结果。
     * 取值范围：[0,2^31−1]。
     */
    private Long seed;

    /**
     * （可选）
     * 停止词列表
     * 用于指定停止词。当模型生成的文本中出现stop 指定的字符串或token_id时，生成将立即终止。
     * 可传入敏感词以控制模型的输出。
     * stop为数组时，不可将token_id和字符串同时作为元素输入，比如不可以指定为["你好",104307]。
     */
    private List<String> stop;
}
