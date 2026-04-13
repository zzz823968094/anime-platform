package com.anime.common.enums;

public enum UserStatusEnum {
    NORMAL("正常"),
    DISABLE("禁用");
    private String desc;
    UserStatusEnum(String desc) {
        this.desc = desc;
    }
    public String getDesc() {
        return desc;
    }
}
