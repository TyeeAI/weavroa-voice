package com.eniac.weavroa.llm.model.chat.request.option;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchOption {

    /**
     * 可选）默认值为false
     * 是否强制开启联网搜索，仅当enable_search为true时生效。
     * 可选值：
     * true：强制开启；
     * false：不强制开启，由模型判断是否联网搜索。
     * */
    private boolean forcedSearch;

    /**
     * （可选）默认值为turbo
     * 搜索量级策略，仅当enable_search为true时生效。
     * 可选值：
     * turbo （默认）: 兼顾响应速度与搜索效果，适用于大多数场景。
     * max: 采用更全面的搜索策略，可调用多源搜索引擎，以获取更详尽的搜索结果，但响应时间可能更长。
     * agent：可多次调用联网搜索工具与大模型，实现多轮信息检索与内容整合。
     * */
    private String searchStrategy = "turbo";

    /**
     * （可选）默认值为false
     * 是否开启垂域搜索，仅当enable_search为true时生效。
     * 可选值：
     * true：开启。
     * false：不开启。
     * 该参数非OpenAI标准参数。
     * */
    private boolean enableSearchExtension;
}
