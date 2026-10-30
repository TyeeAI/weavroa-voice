package com.eniac.weavroa.llm.model.chat.request.option;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AudioOption {

    /**
     * （必选）输出音频的音色。
     * Cherry: 芊悦,阳光积极、亲切自然小姐姐
     * Serena: 苏瑶,温柔小姐姐
     * Ethan: 晨煦,标准普通话，带部分北方口音。阳光、温暖、活力、朝气
     * Chelsie: 千雪,二次元虚拟女友
     *
     *
     *
     *
     *
     * */
    private String voice;

    /**
     * 输出音频的格式，仅支持设定为wav。
     * */
    private String format = "wav";

    public AudioOption(String voice) {
        this.voice = voice;
    }
}
