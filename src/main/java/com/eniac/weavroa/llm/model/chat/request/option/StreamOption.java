package com.eniac.weavroa.llm.model.chat.request.option;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StreamOption {

    public static StreamOption INCLUDED = new StreamOption();

    private boolean includeUsage = true;

    private StreamOption() {}
}
