package com.eniac.weavroa.llm.model.chat;

import java.util.ArrayList;
import java.util.List;

import com.eniac.weavroa.llm.model.chat.tool.ToolCall;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 消息
 * 表示对话中的一条消息
 */
@Getter
@Setter
@NoArgsConstructor
public class Message {

    /**
     * 消息角色
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 是否开启前缀续写
     */
    private Boolean partial;

    /**
     * 工具调用列表
     */
    private List<ToolCall> toolCalls;

    /**
     * 工具调用 ID（tool 角色消息需要）
     */
    private String toolCallId;


    private Message(String role, String content) {
        this.role = role;
        this.content = content;
    }

    private Message(String role, String content, boolean partial) {
        this(role, content);
        this.partial = partial;
    }

    private Message(String role, String content, String toolCallId) {
        this(role, content);
        this.toolCallId = toolCallId;
    }

    protected void setToolCalls(List<ToolCall> toolCalls) {
        this.toolCalls = toolCalls;
    }

    /**
     * 创建消息列表构建器
     */
    public static MessagesBuilder builder() {
        return new MessagesBuilder();
    }

    /**
     * 消息列表构建器
     */
    public static class MessagesBuilder {

        private final List<Message> messages = new ArrayList<>();

        public MessagesBuilder history(List<Message> messages) {
            this.messages.addAll(messages);
            return this;
        }

        /**
         * 添加用户消息
         */
        public MessagesBuilder user(String content) {
            this.messages.add(new Message("user", content));
            return this;
        }

        /**
         * 添加助手消息
         */
        public MessagesBuilder assistant(String content) {
            this.messages.add(new Message("assistant", content));
            return this;
        }

        /**
         * 添加助手消息（带前缀续写标记）
         */
        public MessagesBuilder assistant(String content, boolean partial) {
            this.messages.add(new Message("assistant", content, partial));
            return this;
        }

        /**
         * 添加带工具调用的助手消息
         * @return AssistantToolBuilder 用于添加工具调用
         */
        public AssistantToolBuilder assistantWithTool(String content, boolean partial) {
            return new AssistantToolBuilder(this, content, partial);
        }

        /**
         * 添加工具响应消息
         */
        public MessagesBuilder tool(String content, String toolCallId) {
            this.messages.add(new Message("tool", content, toolCallId));
            return this;
        }

        /**
         * 添加系统消息
         */
        public MessagesBuilder system(String content) {
            this.messages.add(new Message("system", content));
            return this;
        }

        protected void addMessage(Message message) {
            this.messages.add(message);
        }

        /**
         * 构建消息列表
         */
        public List<Message> build() {
            return this.messages;
        }
    }

    /**
     * 带工具调用的助手消息构建器
     */
    public static class AssistantToolBuilder {

        private final MessagesBuilder parent;
        private final Message message;
        private final List<ToolCall> toolCalls = new ArrayList<>();

        public AssistantToolBuilder(MessagesBuilder parent, String content, boolean partial) {
            this.parent = parent;
            this.message = new Message("assistant", content, partial);
        }

        /**
         * 添加工具调用
         */
        public AssistantToolBuilder tool(String id, String name, String arguments) {
            Integer index = this.toolCalls.size();
            this.toolCalls.add(new ToolCall(id, index, name, arguments));
            return this;
        }

        /**
         * 完成助手消息构建，返回消息列表构建器
         */
        public MessagesBuilder done() {
            this.message.setToolCalls(this.toolCalls);
            this.parent.addMessage(this.message);
            return this.parent;
        }
    }
}
