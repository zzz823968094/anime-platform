package com.anime.anime.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.anime.anime.entity.AccessData;
import com.anime.anime.mapper.AccessDataMapper;
import com.anime.anime.service.AccessDataService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 访问统计服务（轻量级方案）
 * 直接从 anime-service 接收访问上报，存储到 Redis，定时聚合到数据库
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccessDataServiceImpl extends ServiceImpl<AccessDataMapper, AccessData> implements AccessDataService {

    private final StringRedisTemplate redisTemplate;
    private final AccessDataMapper accessDataMapper;

    private static final String REDIS_KEY_PREFIX = "access:";
    private static final String REDIS_KEY_SUFFIX = ":ips";
    private static final String APP_REDIS_KEY_SUFFIX = ":app:ips";
    private static final String WEB_REDIS_KEY_SUFFIX = ":web:ips";

    /**
     * 记录访问IP到Redis
     *
     * @param ip   客户端IP
     * @param sign 标识
     */
    @Override
    public void recordAccess(String ip, String sign) {
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            return;
        }
        sign = sign == null ? "app" : sign;
        try {
            // 获取当前日期作为Key
            String dateKey = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            String redisKey = REDIS_KEY_PREFIX + dateKey + ":" + sign + REDIS_KEY_SUFFIX;
            // 异步将IP添加到Redis Set中（自动去重）
            redisTemplate.opsForSet().add(redisKey, ip);
            // 设置过期时间为3天（防止Redis数据堆积）
            redisTemplate.expire(redisKey, java.time.Duration.ofDays(3));
            log.debug("记录访问IP: {} -> {}", dateKey, ip);
        } catch (Exception e) {
            log.error("记录访问IP失败: {}", ip, e);
        }
    }

    /**
     * 将前一天的访问数据聚合到数据库
     */
    public void aggregateHourlyAccessData() {
        log.info("开始执行访问数据聚合任务");
        try {
            // 获取昨天的日期Key
            String yesterday = LocalDate.now().minusDays(1)
                    .format(DateTimeFormatter.BASIC_ISO_DATE);
            String webRedisKey = REDIS_KEY_PREFIX + yesterday + WEB_REDIS_KEY_SUFFIX;
            String appRedisKey = REDIS_KEY_PREFIX + yesterday + APP_REDIS_KEY_SUFFIX;
            // 从Redis中获取所有IP
            Set<String> appIps = redisTemplate.opsForSet().members(appRedisKey);
            Set<String> webIps = redisTemplate.opsForSet().members(webRedisKey);
            // 计算访问人数（去重IP数）
            int appUserCount = (appIps == null || appIps.isEmpty()) ? 0 : appIps.size();
            int webUserCount = (webIps == null || webIps.isEmpty()) ? 0 : webIps.size();
            // 检查是否已存在该日期的记录
            Integer dateInt = Integer.parseInt(yesterday);
            AccessData existing = accessDataMapper.selectOne(
                    new LambdaQueryWrapper<AccessData>()
                            .eq(AccessData::getDate, dateInt)
            );
            JSONObject object = new JSONObject();
            object.set("appIp", appIps);
            object.set("webIp", webIps);
            String ips = JSONUtil.toJsonStr(object);
            if (existing != null) {
                // 更新现有记录
                existing.setAppUserCount(appUserCount);
                existing.setWebUserCount(webUserCount);
                existing.setIp(ips);
                accessDataMapper.updateById(existing);
                log.info("更新访问数据: date={}, appUserCount={},webUserCount={}", yesterday, appUserCount, webUserCount);
            } else {
                // 插入新记录
                AccessData accessData = new AccessData();
                accessData.setDate(dateInt);
                accessData.setAppUserCount(appUserCount);
                accessData.setWebUserCount(webUserCount);
                accessData.setIp(ips);
                accessDataMapper.insert(accessData);
                log.info("新增访问数据: date={}, appUserCount={},webUserCount={}", yesterday, appUserCount, webUserCount);
            }
            log.info("访问数据聚合任务执行完成");
        } catch (Exception e) {
            log.error("访问数据聚合任务执行失败", e);
        }
    }

    /**
     * 每天凌晨1点执行，确保前一天的数据完全聚合
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void aggregateDailyAccessData() {
        log.info("开始执行每日访问数据最终聚合任务");
        aggregateHourlyAccessData();
    }

    /**
     * 获取今日实时访问人数（从Redis读取）
     */
    @Override
    public Integer getTodayAppRealTimeUserCount() {
        return getUserCountBySign(APP_REDIS_KEY_SUFFIX);
    }

    /**
     * 获取今日实时访问人数（从Redis读取）
     */
    @Override
    public Integer getTodayWebRealTimeUserCount() {
        return getUserCountBySign(WEB_REDIS_KEY_SUFFIX);
    }

    @NonNull
    private Integer getUserCountBySign(String webRedisKeySuffix) {
        try {
            String today = LocalDate.now()
                    .format(DateTimeFormatter.BASIC_ISO_DATE);
            String redisKey = REDIS_KEY_PREFIX + today + webRedisKeySuffix;
            Long count = redisTemplate.opsForSet().size(redisKey);
            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
            log.error("获取今日实时访问人数失败", e);
            return 0;
        }
    }

    /**
     * 获取指定日期的访问数据
     */
    @Override
    public AccessData getAccessDataByDate(String date) {
        try {
            Integer dateInt = Integer.parseInt(date);
            return accessDataMapper.selectOne(
                    new LambdaQueryWrapper<AccessData>()
                            .eq(AccessData::getDate, dateInt)
            );
        } catch (Exception e) {
            log.error("查询访问数据失败: date={}", date, e);
            return null;
        }
    }

    /**
     * 获取最近N天的访问趋势
     */
    @Override
    public List<AccessData> getAccessTrend(int days) {
        List<AccessData> accessTrend = accessDataMapper.getAccessTrend(days);
        AccessData accessData = new AccessData();
        Integer todayWebRealTimeUserCount = getTodayWebRealTimeUserCount();
        Integer todayAppRealTimeUserCount = getTodayAppRealTimeUserCount();
        accessData.setWebUserCount(todayWebRealTimeUserCount);
        accessData.setAppUserCount(todayAppRealTimeUserCount);
        String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        accessData.setDate(Integer.parseInt(today));
        if(accessTrend == null || accessTrend.isEmpty()){
            accessTrend = new ArrayList<>();
        }
        accessTrend.add(accessData);
        return accessTrend;
    }

    /**
     * 获取总访问人数
     */
    @Override
    public Long getTotalUserCount() {
        Long totalUserCount = accessDataMapper.getTotalUserCount();
        return totalUserCount == null ? 0 : totalUserCount;
    }
}
