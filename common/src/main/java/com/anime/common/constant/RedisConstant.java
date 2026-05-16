package com.anime.common.constant;

/**
 * Redis键名常量类
 * 遵循阿里巴巴开发规范，Redis键名统一管理
 *
 * @author anime-platform
 * @date 2026-05-16
 */
public class RedisConstant {

    /**
     * 私有构造函数，防止实例化
     */
    private RedisConstant() {
        throw new IllegalStateException("Utility class");
    }

    // ==================== 键名前缀常量 ====================

    /**
     * 用户相关键名前缀
     */
    public static final String PREFIX_USER = "user:";

    /**
     * 管理员相关键名前缀
     */
    public static final String PREFIX_ADMIN = "admin:";

    /**
     * 动漫相关键名前缀
     */
    public static final String PREFIX_ANIME = "anime:";

    /**
     * 视频相关键名前缀
     */
    public static final String PREFIX_VIDEO = "video:";

    /**
     * 弹幕相关键名前缀
     */
    public static final String PREFIX_DANMAKU = "danmaku:";

    /**
     * 广告相关键名前缀
     */
    public static final String PREFIX_AD = "ad:";

    /**
     * 轮播图相关键名前缀
     */
    public static final String PREFIX_CAROUSEL = "carousel:";

    /**
     * 搜索相关键名前缀
     */
    public static final String PREFIX_SEARCH = "search:";

    /**
     * 统计相关键名前缀
     */
    public static final String PREFIX_STATS = "stats:";

    /**
     * 系统相关键名前缀
     */
    public static final String PREFIX_SYSTEM = "system:";

    /**
     * 限流相关键名前缀
     */
    public static final String PREFIX_RATE_LIMIT = "rate_limit:";

    /**
     * 锁相关键名前缀
     */
    public static final String PREFIX_LOCK = "lock:";

    /**
     * 缓存相关键名前缀
     */
    public static final String PREFIX_CACHE = "cache:";

    /**
     * 会话相关键名前缀
     */
    public static final String PREFIX_SESSION = "session:";

    /**
     * Token相关键名前缀
     */
    public static final String PREFIX_TOKEN = "token:";

    // ==================== 用户相关键名 ====================

    /**
     * 用户信息缓存键名模板
     * 用法：String.format(USER_INFO_KEY, userId)
     */
    public static final String USER_INFO_KEY = PREFIX_USER + "info:%s";

    /**
     * 用户登录状态键名模板
     * 用法：String.format(USER_LOGIN_STATUS_KEY, userId)
     */
    public static final String USER_LOGIN_STATUS_KEY = PREFIX_USER + "login:status:%s";

    /**
     * 用户Token键名模板
     * 用法：String.format(USER_TOKEN_KEY, userId)
     */
    public static final String USER_TOKEN_KEY = PREFIX_TOKEN + "user:%s";

    /**
     * IP注册计数键名前缀
     * 用法：String.format(IP_REGISTER_COUNT_KEY, ip)
     */
    public static final String IP_REGISTER_COUNT_KEY = PREFIX_USER + "register:ip:%s";

    /**
     * 用户验证码键名模板
     * 用法：String.format(USER_VERIFICATION_CODE_KEY, phone)
     */
    public static final String USER_VERIFICATION_CODE_KEY = PREFIX_USER + "verification:code:%s";

    // ==================== 管理员相关键名 ====================

    /**
     * 管理员信息缓存键名模板
     * 用法：String.format(ADMIN_INFO_KEY, adminId)
     */
    public static final String ADMIN_INFO_KEY = PREFIX_ADMIN + "info:%s";

    /**
     * 管理员登录状态键名模板
     * 用法：String.format(ADMIN_LOGIN_STATUS_KEY, adminId)
     */
    public static final String ADMIN_LOGIN_STATUS_KEY = PREFIX_ADMIN + "login:status:%s";

    /**
     * 管理员Token键名模板
     * 用法：String.format(ADMIN_TOKEN_KEY, adminId)
     */
    public static final String ADMIN_TOKEN_KEY = PREFIX_TOKEN + "admin:%s";

    // ==================== 动漫相关键名 ====================

    /**
     * 动漫详情缓存键名模板
     * 用法：String.format(ANIME_DETAIL_KEY, animeId)
     */
    public static final String ANIME_DETAIL_KEY = PREFIX_ANIME + "detail:%s";

    /**
     * 动漫列表缓存键名
     */
    public static final String ANIME_LIST_KEY = PREFIX_ANIME + "list";

    /**
     * 热门动漫缓存键名
     */
    public static final String ANIME_HOT_KEY = PREFIX_ANIME + "hot";

    /**
     * 推荐动漫缓存键名
     */
    public static final String ANIME_RECOMMEND_KEY = PREFIX_ANIME + "recommend";

    /**
     * 动漫分类缓存键名模板
     * 用法：String.format(ANIME_CATEGORY_KEY, categoryId)
     */
    public static final String ANIME_CATEGORY_KEY = PREFIX_ANIME + "category:%s";

    // ==================== 视频相关键名 ====================

    /**
     * 视频详情缓存键名模板
     * 用法：String.format(VIDEO_DETAIL_KEY, videoId)
     */
    public static final String VIDEO_DETAIL_KEY = PREFIX_VIDEO + "detail:%s";

    /**
     * 视频播放次数统计键名模板
     * 用法：String.format(VIDEO_PLAY_COUNT_KEY, videoId)
     */
    public static final String VIDEO_PLAY_COUNT_KEY = PREFIX_VIDEO + "play:count:%s";

    // ==================== 搜索相关键名 ====================

    /**
     * 热门搜索缓存键名
     */
    public static final String SEARCH_HOT_KEY = PREFIX_SEARCH + "hot";

    /**
     * 搜索趋势缓存键名
     */
    public static final String SEARCH_TREND_KEY = PREFIX_SEARCH + "trend";

    /**
     * 用户最近搜索记录键名模板
     * 用法：String.format(SEARCH_RECENT_KEY, userId)
     */
    public static final String SEARCH_RECENT_KEY = PREFIX_SEARCH + "recent:%s";

    // ==================== 统计相关键名 ====================

    /**
     * 日活跃用户数统计键名模板
     * 用法：String.format(STATS_DAU_KEY, date)
     */
    public static final String STATS_DAU_KEY = PREFIX_STATS + "dau:%s";

    /**
     * 日新增用户数统计键名模板
     * 用法：String.format(STATS_NEW_USER_KEY, date)
     */
    public static final String STATS_NEW_USER_KEY = PREFIX_STATS + "new_user:%s";

    /**
     * 访问统计键名模板
     * 用法：String.format(STATS_ACCESS_KEY, date)
     */
    public static final String STATS_ACCESS_KEY = PREFIX_STATS + "access:%s";

    // ==================== 系统相关键名 ====================

    /**
     * 系统更新状态键名
     */
    public static final String SYSTEM_UPDATE_STATUS_KEY = PREFIX_SYSTEM + "update:status";

    /**
     * 系统更新消息键名
     */
    public static final String SYSTEM_UPDATE_MESSAGE_KEY = PREFIX_SYSTEM + "update:message";

    /**
     * 系统维护状态键名
     */
    public static final String SYSTEM_MAINTENANCE_KEY = PREFIX_SYSTEM + "maintenance";

    // ==================== 限流相关键名 ====================

    /**
     * API限流键名模板
     * 用法：String.format(RATE_LIMIT_API_KEY, apiPath, ip)
     */
    public static final String RATE_LIMIT_API_KEY = PREFIX_RATE_LIMIT + "api:%s:%s";

    /**
     * 用户操作限流键名模板
     * 用法：String.format(RATE_LIMIT_USER_KEY, userId, action)
     */
    public static final String RATE_LIMIT_USER_KEY = PREFIX_RATE_LIMIT + "user:%s:%s";

    // ==================== 锁相关键名 ====================

    /**
     * 分布式锁键名模板
     * 用法：String.format(LOCK_KEY, businessKey)
     */
    public static final String LOCK_KEY = PREFIX_LOCK + "%s";

    /**
     * 用户注册锁键名模板
     * 用法：String.format(LOCK_USER_REGISTER_KEY, ip)
     */
    public static final String LOCK_USER_REGISTER_KEY = PREFIX_LOCK + "user:register:%s";

    // ==================== 缓存过期时间常量（秒） ====================

    /**
     * 验证码过期时间（5分钟）
     */
    public static final int VERIFICATION_CODE_EXPIRE = 5 * 60;

    /**
     * 用户信息缓存过期时间（30分钟）
     */
    public static final int USER_INFO_EXPIRE = 30 * 60;

    /**
     * 动漫详情缓存过期时间（1小时）
     */
    public static final int ANIME_DETAIL_EXPIRE = 60 * 60;

    /**
     * 动漫列表缓存过期时间（10分钟）
     */
    public static final int ANIME_LIST_EXPIRE = 10 * 60;

    /**
     * 热门搜索缓存过期时间（1小时）
     */
    public static final int SEARCH_HOT_EXPIRE = 60 * 60;

    /**
     * Token过期时间（7天）
     */
    public static final int TOKEN_EXPIRE = 7 * 24 * 60 * 60;

    /**
     * 分布式锁默认过期时间（30秒）
     */
    public static final int LOCK_DEFAULT_EXPIRE = 30;

    /**
     * IP注册计数过期时间（24小时）
     */
    public static final int IP_REGISTER_EXPIRE = 24 * 60 * 60;
}
