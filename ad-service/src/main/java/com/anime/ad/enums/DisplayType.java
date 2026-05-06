package com.anime.ad.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DisplayType {
    IMAGE("IMAGE", "图片"),
    VIDEO("VIDEO", "视频"),
    HTML("HTML", "富媒体");

    private final String code;
    private final String desc;

    public static DisplayType getByCode(String code) {
        for (DisplayType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
