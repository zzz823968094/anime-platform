package com.anime.ad.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TargetType {
    ALL("ALL", "全部用户"),
    NEW("NEW", "新用户"),
    VIP("VIP", "VIP用户");

    private final String code;
    private final String desc;

    public static TargetType getByCode(String code) {
        for (TargetType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
