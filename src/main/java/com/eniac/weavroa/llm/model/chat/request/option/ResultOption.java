package com.eniac.weavroa.llm.model.chat.request.option;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.Set;

@Getter
@AllArgsConstructor
public class ResultOption {

    public static ResultOption DEFAULT = ResultOption.builder().build();

    /**
     * （可选）默认值为["text"]
     * 输出数据的模态，仅适用于 全模态 模型
     * 可选值如下：
     * ["text","audio"]：输出文本与音频；
     * ["text"]：仅输出文本。
     */
    private Set<String> modalities;

    /**
     * 输出音频的音色与格式，仅适用于 全模态 模型，且modalities参数需为["text","audio"]。
     * voice string （必选）输出音频的音色。
     * format string （必选）输出音频的格式，仅支持设定为wav。
     */
    private AudioOption audio;

    /**
     * （可选）
     * 返回内容的格式。可选值：
     * {"type": "text"}：输出文字回复；
     * {"type": "json_object"}：输出标准格式的JSON字符串。
     * {"type": "json_schema","json_schema": {...} }：输出指定格式的JSON字符串。
     * 若指定为{"type": "json_object"}，需在提示词中明确指示模型输出JSON，如：“请按照json格式输出”，否则会报错。
     */
    @JsonProperty("response_format")
    private ResponseFormatOption responseFormat;

    public static ResultOptionBuilder builder() {
        return new ResultOptionBuilder();
    }

    public static class ResultOptionBuilder {
        private Set<String> modalities;
        private AudioOption audio;
        private ResponseFormatOption responseFormat;

        public ResultOption build() {
            return new ResultOption(this.modalities, this.audio, this.responseFormat);
        }

        public ResultOptionBuilder modalities(Set<String> modalities) {
            this.modalities = modalities;
            return this;
        }

        public ResultOptionBuilder json() {
            this.responseFormat = new ResponseFormatOption("json_object", null);
            return this;
        }

        public ResultOptionBuilder json(String name, String description, Map<String, Object> schema) {
            JSONSchemaOption jsonSchemaOption = new JSONSchemaOption(name, description, schema, true);
            this.responseFormat = new ResponseFormatOption("json_schema", jsonSchemaOption);
            return this;
        }

        public ResultOptionBuilder audio(String voice) {
            this.modalities = Set.of("audio");
            this.audio = new AudioOption(voice);
            return this;
        }
    }
}
