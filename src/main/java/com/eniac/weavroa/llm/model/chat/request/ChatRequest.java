package com.eniac.weavroa.llm.model.chat.request;

import com.eniac.weavroa.llm.model.chat.Message;
import com.eniac.weavroa.llm.model.chat.request.option.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 对话请求
 * 使用组合模式，包含 GenerateOption 和 ResultOption
 * 通过 @JsonUnwrapped 在序列化时展开为扁平结构（符合 OpenAI API 格式）
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    /**
     * 模型名称
     */
    private String model;

    /**
     * 对话消息列表
     */
    @Builder.Default
    private List<Message> messages = new ArrayList<>();

    /**
     * 是否流式输出
     */
    @Builder.Default
    private boolean stream = false;

    /**
     * 流式输出的配置项
     */
    @JsonProperty("stream_option")
    private StreamOption streamOption;

    /**
     * 生成过程控制参数
     */
    @JsonUnwrapped
    private GenerateOption generateOption;

    /**
     * 结果格式控制参数
     */
    @JsonUnwrapped
    private ResultOption resultOption;

    /**
     * 思考模式控制参数
     */
    @JsonUnwrapped
    private ThinkingOption thinkingOption;

    /**
     * 是否开启代码解释器功能
     */
    private boolean enableCodeInterpreter;

    /**
     * 是否返回输出 Token 的对数概率
     */
    private boolean logprobs;

    /**
     * 返回模型最大概率的候选 Token 个数 (0-5)
     */
    private Integer topLogprobs;

    /**
     * 工具调用控制参数
     */
    @JsonUnwrapped
    private ToolOption toolOption;

    /**
     * 是否开启联网搜索
     */
    private Boolean enableSearch;

    /**
     * 联网搜索的策略
     */
    private SearchOption searchOptions;

        /**
     * 从分离的参数构建请求
     */
        public static ChatRequest of(List<Message> messages, GenerateOption genOption, ResultOption resultOption) {
            return ChatRequest.builder()
                    .messages(messages)
                    .generateOption(genOption != null ? genOption : GenerateOption.DEFAULT)
                    .resultOption(resultOption != null ? resultOption : ResultOption.DEFAULT)
                    .build();
        }
    
        /**
         * 从消息和生成选项构建请求
         */
        public static ChatRequest of(List<Message> messages, GenerateOption genOption) {
            return of(messages, genOption, null);
        }
    
        /**
         * 从消息和结果选项构建请求
         */
        public static ChatRequest of(List<Message> messages, ResultOption resultOption) {
            return of(messages, null, resultOption);
        }
    
        /**
         * 仅从消息构建请求
         */
        public static ChatRequest of(List<Message> messages) {
            return ChatRequest.builder().messages(messages).build();
        }
        
        /**
         * 从分离的参数构建流式请求
         */
        public static ChatRequest ofStream(List<Message> messages, GenerateOption genOption, ResultOption resultOption) {
            return ChatRequest.builder()
                    .messages(messages)
                    .generateOption(genOption != null ? genOption : GenerateOption.DEFAULT)
                    .resultOption(resultOption != null ? resultOption : ResultOption.DEFAULT)
                    .stream(true)
                    .streamOption(StreamOption.INCLUDED)
                    .build();
        }
    
        /**
         * 从消息和生成选项构建流式请求
         */
        public static ChatRequest ofStream(List<Message> messages, GenerateOption genOption) {
            return ofStream(messages, genOption, null);
        }
    
        /**
         * 仅从消息构建流式请求
         */
        public static ChatRequest ofStream(List<Message> messages) {
            return ChatRequest.builder().messages(messages).stream(true).build();
        }
}
