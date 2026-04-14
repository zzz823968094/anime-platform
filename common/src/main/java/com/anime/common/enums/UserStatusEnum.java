package com.anime.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum UserStatusEnum {
    NORMAL("NORMAL", "正常"),
    DISABLE("DISABLE", "禁用");

    @EnumValue
    private final String code;
    private final String desc;

    UserStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 将数据库字符串转换为枚举
     */
    public static UserStatusEnum fromCode(String code) {
        for (UserStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status code: " + code);
    }
}
