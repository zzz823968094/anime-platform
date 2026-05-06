package com.anime.ad.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LinkType {
    URL("URL", "外部链接"),
    ANIME("ANIME", "动漫详情"),
    SEARCH("SEARCH", "搜索结果"),
    NONE("NONE", "无跳转");

    private final String code;
    private final String desc;

    public static LinkType getByCode(String code) {
        for (LinkType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
