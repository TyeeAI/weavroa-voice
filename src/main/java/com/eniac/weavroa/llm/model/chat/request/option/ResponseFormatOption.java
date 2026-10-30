package com.eniac.weavroa.llm.model.chat.request.option;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class ResponseFormatOption {
    /**
     * 返回内容的格式。可选值：
     * text：输出文字回复；
     * json_object：输出标准格式的JSON字符串；
     * json_schema：输出指定格式的JSON字符串。
     */
    private String type;

    /**
     * 当 type 为 json_schema 时，该字段为必选，用于定义结构化输出的配置。
     */
    @JsonProperty("json_schema")
    private JSONSchemaOption jsonSchema;

    public static ResponseFormatOptionBuilder builder() {
        return new ResponseFormatOptionBuilder();
    }

    @Getter
    public static class ResponseFormatOptionBuilder {

        private String name;

        private String description;

        private Map<String, Object> schema;

        public ResponseFormatOptionBuilder jsonSchema(Map<String, Object> schema) {
            this.schema = schema;
            return this;
        }

        public ResponseFormatOptionBuilder jsonSchema(String name, Map<String, Object> schema) {
            this.name = name;
            this.schema = schema;
            return this;
        }

        public ResponseFormatOptionBuilder jsonSchema(String name, String description, Map<String, Object> schema) {
            this.name = name;
            this.schema = schema;
            this.description = description;
            return this;
        }

        public ResponseFormatOption build() {
            JSONSchemaOption jsonSchema = new JSONSchemaOption(this.name, this.description, this.schema, true);
            return new ResponseFormatOption("json_schema", jsonSchema);
        }

    }
}
