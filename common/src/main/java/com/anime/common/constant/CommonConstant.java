package com.anime.common.constant;

/**
 * 通用业务常量类
 * 遵循阿里巴巴开发规范，所有魔法值集中管理
 *
 * @author anime-platform
 * @date 2026-05-12
 */
public class CommonConstant {

    /**
     * 私有构造函数，防止实例化
     */
    private CommonConstant() {
        throw new IllegalStateException("Utility class");
    }

    // ==================== 用户相关常量 ====================

    /**
     * 用户名最小长度
     */
    public static final int USERNAME_MIN_LENGTH = 4;

    /**
     * 用户名最大长度
     */
    public static final int USERNAME_MAX_LENGTH = 16;

    /**
     * 用户名正则表达式（只允许字母、数字和下划线）
     */
    public static final String USERNAME_PATTERN = "^[a-zA-Z0-9_]+$";

    /**
     * 密码最小长度
     */
    public static final int PASSWORD_MIN_LENGTH = 6;

    /**
     * 密码最大长度
     */
    public static final int PASSWORD_MAX_LENGTH = 64;

    /**
     * 同一IP每日最大注册数
     */
    public static final int MAX_REGISTER_PER_IP_PER_DAY = 3;

    // ==================== 分页相关常量 ====================

    /**
     * 默认页码
     */
    public static final int DEFAULT_PAGE_NUM = 1;

    /**
     * 默认每页大小
     */
    public static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * 搜索默认每页大小
     */
    public static final int SEARCH_DEFAULT_PAGE_SIZE = 24;

    /**
     * 推荐列表默认大小
     */
    public static final int RECOMMEND_DEFAULT_SIZE = 12;

    // ==================== 时间相关常量 ====================

    /**
     * 一天的小时数
     */
    public static final int HOURS_PER_DAY = 24;

    /**
     * Redis IP注册计数过期时间（小时）
     */
    public static final long REDIS_IP_REGISTER_EXPIRE_HOURS = 24L;

    // ==================== 动漫分类常量 ====================

    /**
     * 日本动漫类型ID
     */
    public static final int ANIME_TYPE_JAPAN = 66;

    /**
     * 欧美动漫类型ID
     */
    public static final int ANIME_TYPE_US = 67;

    /**
     * 中国动漫类型ID
     */
    public static final int ANIME_TYPE_CHINA = 68;

    // ==================== 状态常量 ====================

    /**
     * 状态：禁用/关闭/否（0）
     */
    public static final int ZERO = 0;

    /**
     * 状态：启用/开启/是（1）
     */
    public static final int ONE = 1;

    /**
     * 番剧状态：已发布/正常
     */
    public static final int ANIME_STATUS_PUBLISHED = 1;

    /**
     * 番剧状态：已下线
     */
    public static final int ANIME_STATUS_OFFLINE = 0;

    // ==================== 排序常量 ====================

    /**
     * 默认排序：最新
     */
    public static final String SORT_BY_LATEST = "latest";

    /**
     * 按热度排序
     */
    public static final String SORT_BY_HOT = "hot";

    // ==================== 搜索相关常量 ====================

    /**
     * 搜索关键词最小长度
     */
    public static final int SEARCH_KEYWORD_MIN_LENGTH = 2;

    /**
     * 默认热门搜索数量
     */
    public static final int DEFAULT_HOT_KEYWORD_LIMIT = 20;

    /**
     * 默认搜索趋势天数
     */
    public static final int DEFAULT_SEARCH_TREND_DAYS = 7;

    /**
     * 默认最近搜索记录数量
     */
    public static final int DEFAULT_RECENT_SEARCH_LIMIT = 20;

    // ==================== 统计相关常量 ====================

    /**
     * 默认访问统计天数
     */
    public static final int DEFAULT_ACCESS_STATS_DAYS = 7;

    /**
     * 默认设备统计天数
     */
    public static final int DEFAULT_DEVICE_STATS_DAYS = 7;

    // ==================== HTTP状态码常量 ====================

    /**
     * HTTP成功状态码
     */
    public static final int HTTP_STATUS_SUCCESS = 200;

    /**
     * HTTP参数错误状态码
     */
    public static final int HTTP_STATUS_PARAM_ERROR = 400;

    /**
     * HTTP未授权状态码
     */
    public static final int HTTP_STATUS_UNAUTHORIZED = 401;

    /**
     * HTTP禁止访问状态码
     */
    public static final int HTTP_STATUS_FORBIDDEN = 403;

    /**
     * HTTP资源未找到状态码
     */
    public static final int HTTP_STATUS_NOT_FOUND = 404;

    /**
     * HTTP请求过于频繁状态码
     */
    public static final int HTTP_STATUS_TOO_MANY_REQUESTS = 429;

    /**
     * HTTP服务器内部错误状态码
     */
    public static final int HTTP_STATUS_SERVER_ERROR = 500;

    // ==================== 用户状态常量 ====================

    /**
     * 用户角色：普通用户
     */
    public static final int USER_ROLE_NORMAL = 0;

    /**
     * 用户角色：管理员
     */
    public static final int USER_ROLE_ADMIN = 1;

    /**
     * 用户状态：正常（Integer类型，用于user表）
     */
    public static final int USER_STATUS_NORMAL_INT = 1;

    /**
     * 用户状态：禁用（Integer类型，用于user表）
     */
    public static final int USER_STATUS_DISABLED_INT = 0;

    /**
     * 用户状态：正常（String类型，用于admin_user表）
     */
    public static final String USER_STATUS_NORMAL = "NORMAL";

    /**
     * 用户状态：禁用（String类型，用于admin_user表）
     */
    public static final String USER_STATUS_DISABLED = "DISABLE";

    // ==================== 广告状态常量 ====================

    /**
     * 广告状态：启用
     */
    public static final int AD_STATUS_ENABLED = 1;

    /**
     * 广告状态：禁用
     */
    public static final int AD_STATUS_DISABLED = 0;

    /**
     * 广告状态字符串：enabled
     */
    public static final String AD_STATUS_STRING_ENABLED = "enabled";

    /**
     * 广告状态字符串：disabled
     */
    public static final String AD_STATUS_STRING_DISABLED = "disabled";

    /**
     * 广告状态字符串：true
     */
    public static final String AD_STATUS_STRING_TRUE = "true";

    /**
     * 广告状态字符串：false
     */
    public static final String AD_STATUS_STRING_FALSE = "false";

    // ==================== 分页默认值常量 ====================

    /**
     * 默认当前页码
     */
    public static final int DEFAULT_CURRENT_PAGE = 1;

    /**
     * 默认每页大小 - 通用
     */
    public static final int DEFAULT_PAGE_SIZE_COMMON = 10;

    /**
     * 默认每页大小 - 用户列表
     */
    public static final int DEFAULT_PAGE_SIZE_USER = 10;

    /**
     * 默认每页大小 - 管理员列表
     */
    public static final int DEFAULT_PAGE_SIZE_ADMIN = 10;

    /**
     * 默认每页大小 - 轮播图列表
     */
    public static final int DEFAULT_PAGE_SIZE_CAROUSEL = 10;

    /**
     * 默认每页大小 - 应用版本列表
     */
    public static final int DEFAULT_PAGE_SIZE_VERSION = 10;

    /**
     * 默认每页大小 - 广告列表
     */
    public static final int DEFAULT_PAGE_SIZE_AD = 10;

    // ==================== 管理员相关常量 ====================

    /**
     * 默认管理员账号
     */
    public static final String ADMIN_DEFAULT_ACCOUNT = "admin";

    /**
     * 默认管理员密码
     */
    public static final String ADMIN_DEFAULT_PASSWORD = "123456";

    /**
     * 管理员角色ID
     */
    public static final int ADMIN_ROLE_ID = 1;

    /**
     * 管理员角色名称
     */
    public static final String ADMIN_ROLE_NAME = "超级管理员";

    /**
     * 管理员默认用户ID
     */
    public static final long ADMIN_DEFAULT_USER_ID = 202605011642L;

    // ==================== Redis键名常量 ====================

    /**
     * Redis键名前缀：IP注册计数
     */
    public static final String REDIS_KEY_PREFIX_IP_REGISTER = "register:ip:";

    /**
     * Redis键名：系统更新状态
     */
    public static final String REDIS_KEY_SYSTEM_UPDATE_STATUS = "system:update:status";

    /**
     * Redis键名：系统更新消息
     */
    public static final String REDIS_KEY_SYSTEM_UPDATE_MESSAGE = "system:update:message";

    // ==================== 时间单位常量 ====================

    /**
     * 时间单位：小时
     */
    public static final String TIME_UNIT_HOURS = "HOURS";

    // ==================== 系统维护相关常量 ====================

    /**
     * 系统维护默认消息
     */
    public static final String SYSTEM_MAINTENANCE_DEFAULT_MESSAGE = "系统正在维护升级中，请稍后再试...";
    /**
     * 系统维护默认维护市场
     */
    public static final Integer SYSTEM_MAINTENANCE_DEFAULT_EXPIRE_HOURS = 24;

    // ==================== 爬虫相关常量 ====================

    /**
     * 爬虫批量插入大小
     */
    public static final int CRAWLER_BATCH_SIZE = 100;

    /**
     * 爬虫线程池大小
     */
    public static final int CRAWLER_THREAD_POOL_SIZE = 5;

    // ==================== 其他业务常量 ====================

    /**
     * 默认密码
     */
    public static final String DEFAULT_PASSWORD = "123456";

    /**
     * IP地址分割符
     */
    public static final String IP_ADDRESS_SEPARATOR = ",";

    /**
     * X-Forwarded-For头名称
     */
    public static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";

    /**
     * X-Real-IP头名称
     */
    public static final String HEADER_X_REAL_IP = "X-Real-IP";

    // ==================== 网关相关常量 ====================

    /**
     * 网关维护模式业务状态码
     */
    public static final int GATEWAY_MAINTENANCE_CODE = 999;
}
