package com.anime.common.utils;

import java.util.Objects;

/**
 * 脱敏工具类
 * 遵循阿里巴巴开发规范，对敏感信息进行脱敏处理
 *
 * @author anime-platform
 * @date 2026-05-16
 */
public class DesensitizeUtil {

    /**
     * 私有构造函数，防止实例化
     */
    private DesensitizeUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 手机号脱敏：保留前3后4位
     * 示例：138****5678
     *
     * @param phone 手机号
     * @return 脱敏后的手机号
     */
    public static String maskPhone(String phone) {
        if (Objects.isNull(phone) || phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 邮箱脱敏：保留前2位和@后面的内容
     * 示例：te***@example.com
     *
     * @param email 邮箱
     * @return 脱敏后的邮箱
     */
    public static String maskEmail(String email) {
        if (Objects.isNull(email) || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        if (parts[0].length() <= 2) {
            return email;
        }
        return parts[0].substring(0, 2) + "***@" + parts[1];
    }

    /**
     * 身份证号脱敏：保留前6后4位
     * 示例：110101********1234
     *
     * @param idCard 身份证号
     * @return 脱敏后的身份证号
     */
    public static String maskIdCard(String idCard) {
        if (Objects.isNull(idCard) || (idCard.length() != 15 && idCard.length() != 18)) {
            return idCard;
        }
        return idCard.substring(0, 6) + "********" + idCard.substring(idCard.length() - 4);
    }

    /**
     * 姓名脱敏：保留姓，名用*代替
     * 示例：张**、欧阳**
     *
     * @param name 姓名
     * @return 脱敏后的姓名
     */
    public static String maskName(String name) {
        if (Objects.isNull(name) || name.isEmpty()) {
            return name;
        }
        if (name.length() == 1) {
            return "*";
        }
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        // 复姓处理
        if (name.length() == 3 && isCompoundSurname(name.substring(0, 2))) {
            return name.substring(0, 2) + "*";
        }
        return name.charAt(0) + "**";
    }

    /**
     * 银行卡号脱敏：保留前6后4位
     * 示例：622202********1234
     *
     * @param bankCard 银行卡号
     * @return 脱敏后的银行卡号
     */
    public static String maskBankCard(String bankCard) {
        if (Objects.isNull(bankCard) || bankCard.length() < 10) {
            return bankCard;
        }
        int length = bankCard.length();
        return bankCard.substring(0, 6) + "********" + bankCard.substring(length - 4);
    }

    /**
     * IP地址脱敏：保留第一段和最后一段
     * 示例：192.***.***.1
     *
     * @param ip IP地址
     * @return 脱敏后的IP地址
     */
    public static String maskIp(String ip) {
        if (Objects.isNull(ip) || !ip.contains(".")) {
            return ip;
        }
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return ip;
        }
        return parts[0] + ".***.***." + parts[3];
    }

    /**
     * 密码脱敏：全部用*代替
     *
     * @param password 密码
     * @return 脱敏后的密码
     */
    public static String maskPassword(String password) {
        if (Objects.isNull(password)) {
            return null;
        }
        return "******";
    }

    /**
     * 地址脱敏：保留省市，详细地址用*代替
     *
     * @param address 地址
     * @return 脱敏后的地址
     */
    public static String maskAddress(String address) {
        if (Objects.isNull(address) || address.isEmpty()) {
            return address;
        }
        // 简单处理：保留前6个字符（通常是省市区）
        if (address.length() <= 6) {
            return address;
        }
        return address.substring(0, 6) + "******";
    }

    /**
     * 判断是否为复姓
     *
     * @param surname 姓氏
     * @return 是否为复姓
     */
    private static boolean isCompoundSurname(String surname) {
        return "欧阳".equals(surname) || "太史".equals(surname) || "端木".equals(surname)
                || "上官".equals(surname) || "司马".equals(surname) || "东方".equals(surname)
                || "独孤".equals(surname) || "南宫".equals(surname) || "万俟".equals(surname)
                || "闻人".equals(surname) || "夏侯".equals(surname) || "诸葛".equals(surname)
                || "尉迟".equals(surname) || "公羊".equals(surname) || "赫连".equals(surname)
                || "澹台".equals(surname) || "公冶".equals(surname) || "宗政".equals(surname)
                || "濮阳".equals(surname) || "淳于".equals(surname) || "单于".equals(surname)
                || "申屠".equals(surname) || "公孙".equals(surname) || "仲孙".equals(surname)
                || "轩辕".equals(surname) || "令狐".equals(surname) || "钟离".equals(surname)
                || "宇文".equals(surname) || "长孙".equals(surname) || "慕容".equals(surname)
                || "鲜于".equals(surname) || "闾丘".equals(surname) || "司徒".equals(surname)
                || "司空".equals(surname);
    }
}
