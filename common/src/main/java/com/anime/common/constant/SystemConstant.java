package com.anime.common.constant;

/**
 * 系统常量类
 * 遵循阿里巴巴开发规范，系统级常量统一管理
 *
 * @author anime-platform
 * @date 2026-05-16
 */
public class SystemConstant {

    /**
     * 私有构造函数，防止实例化
     */
    private SystemConstant() {
        throw new IllegalStateException("Utility class");
    }

    // ==================== 字符集常量 ====================

    /**
     * UTF-8字符集
     */
    public static final String CHARSET_UTF_8 = "UTF-8";

    /**
     * GBK字符集
     */
    public static final String CHARSET_GBK = "GBK";

    // ==================== 分隔符常量 ====================

    /**
     * 逗号分隔符
     */
    public static final String COMMA_SEPARATOR = ",";

    /**
     * 分号分隔符
     */
    public static final String SEMICOLON_SEPARATOR = ";";

    /**
     * 竖线分隔符
     */
    public static final String PIPE_SEPARATOR = "|";

    /**
     * 下划线分隔符
     */
    public static final String UNDERSCORE_SEPARATOR = "_";

    /**
     * 横线分隔符
     */
    public static final String HYPHEN_SEPARATOR = "-";

    /**
     * 冒号分隔符
     */
    public static final String COLON_SEPARATOR = ":";

    /**
     * 空格
     */
    public static final String SPACE = " ";

    /**
     * 空字符串
     */
    public static final String EMPTY_STRING = "";

    // ==================== 布尔值常量 ====================

    /**
     * 是/真
     */
    public static final String YES = "Y";

    /**
     * 否/假
     */
    public static final String NO = "N";

    /**
     * 成功标识
     */
    public static final String SUCCESS_FLAG = "success";

    /**
     * 失败标识
     */
    public static final String FAIL_FLAG = "fail";

    // ==================== 数字常量 ====================

    /**
     * 零
     */
    public static final int ZERO = 0;

    /**
     * 一
     */
    public static final int ONE = 1;

    /**
     * 二
     */
    public static final int TWO = 2;

    /**
     * 三
     */
    public static final int THREE = 3;

    /**
     * 四
     */
    public static final int FOUR = 4;

    /**
     * 五
     */
    public static final int FIVE = 5;

    /**
     * 十
     */
    public static final int TEN = 10;

    /**
     * 一百
     */
    public static final int ONE_HUNDRED = 100;

    /**
     * 一千
     */
    public static final int ONE_THOUSAND = 1000;

    // ==================== 时间单位常量（毫秒） ====================

    /**
     * 一秒（毫秒）
     */
    public static final long ONE_SECOND_MILLIS = 1000L;

    /**
     * 一分钟（毫秒）
     */
    public static final long ONE_MINUTE_MILLIS = 60 * ONE_SECOND_MILLIS;

    /**
     * 一小时（毫秒）
     */
    public static final long ONE_HOUR_MILLIS = 60 * ONE_MINUTE_MILLIS;

    /**
     * 一天（毫秒）
     */
    public static final long ONE_DAY_MILLIS = 24 * ONE_HOUR_MILLIS;

    /**
     * 一周（毫秒）
     */
    public static final long ONE_WEEK_MILLIS = 7 * ONE_DAY_MILLIS;

    /**
     * 一个月（毫秒，按30天计算）
     */
    public static final long ONE_MONTH_MILLIS = 30 * ONE_DAY_MILLIS;

    // ==================== 文件相关常量 ====================

    /**
     * 默认文件大小限制（10MB）
     */
    public static final long DEFAULT_FILE_SIZE_LIMIT = 10 * 1024 * 1024;

    /**
     * 图片文件大小限制（5MB）
     */
    public static final long IMAGE_FILE_SIZE_LIMIT = 5 * 1024 * 1024;

    /**
     * 视频文件大小限制（100MB）
     */
    public static final long VIDEO_FILE_SIZE_LIMIT = 100 * 1024 * 1024;

    // ==================== 正则表达式常量 ====================

    /**
     * 手机号正则表达式
     */
    public static final String PHONE_PATTERN = "^1[3-9]\\d{9}$";

    /**
     * 邮箱正则表达式
     */
    public static final String EMAIL_PATTERN = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";

    /**
     * IP地址正则表达式
     */
    public static final String IP_PATTERN = "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$";

    /**
     * 身份证号正则表达式
     */
    public static final String ID_CARD_PATTERN = "(^\\d{15}$)|(^\\d{18}$)|(^\\d{17}(\\d|X|x)$)";

    /**
     * URL正则表达式
     */
    public static final String URL_PATTERN = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$";

    // ==================== JSON相关常量 ====================

    /**
     * Content-Type: application/json
     */
    public static final String CONTENT_TYPE_JSON = "application/json";

    /**
     * Content-Type: application/json; charset=utf-8
     */
    public static final String CONTENT_TYPE_JSON_UTF8 = "application/json; charset=utf-8";

    // ==================== HTTP头常量 ====================

    /**
     * Authorization头
     */
    public static final String HEADER_AUTHORIZATION = "Authorization";

    /**
     * Token前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * User-Agent头
     */
    public static final String HEADER_USER_AGENT = "User-Agent";

    /**
     * Referer头
     */
    public static final String HEADER_REFERER = "Referer";

    /**
     * Origin头
     */
    public static final String HEADER_ORIGIN = "Origin";

    /**
     * X-Request-ID头
     */
    public static final String HEADER_X_REQUEST_ID = "X-Request-ID";
}
