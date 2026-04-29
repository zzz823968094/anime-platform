package com.anime.anime.service.impl;

import cn.hutool.json.JSONUtil;
import com.anime.anime.entity.AccessData;
import com.anime.anime.mapper.AccessDataMapper;
import com.anime.anime.service.AccessDataService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    /**
     * 记录访问IP到Redis
     *
     * @param ip 客户端IP
     */
    @Override
    public void recordAccess(String ip) {
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            return;
        }

        try {
            // 获取当前日期作为Key
            String dateKey = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            String redisKey = REDIS_KEY_PREFIX + dateKey + REDIS_KEY_SUFFIX;
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
            String redisKey = REDIS_KEY_PREFIX + yesterday + REDIS_KEY_SUFFIX;
            // 从Redis中获取所有IP
            Set<String> ips = redisTemplate.opsForSet().members(redisKey);
            // 计算访问人数（去重IP数）
            int userCount;
            if (ips == null || ips.isEmpty()) {
                userCount = 0;
            } else {
                userCount = ips.size();
            }
            // 将IP集合转换为JSON字符串
            String ipJson = JSONUtil.toJsonStr(ips);
            // 检查是否已存在该日期的记录
            Integer dateInt = Integer.parseInt(yesterday);
            AccessData existing = accessDataMapper.selectOne(
                    new LambdaQueryWrapper<AccessData>()
                            .eq(AccessData::getDate, dateInt)
            );
            if (existing != null) {
                // 更新现有记录
                existing.setUserCount(userCount);
                existing.setIp(ipJson);
                accessDataMapper.updateById(existing);
                log.info("更新访问数据: date={}, userCount={}", yesterday, userCount);
            } else {
                // 插入新记录
                AccessData accessData = new AccessData();
                accessData.setDate(dateInt);
                accessData.setUserCount(userCount);
                accessData.setIp(ipJson);
                accessDataMapper.insert(accessData);
                log.info("新增访问数据: date={}, userCount={}", yesterday, userCount);
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
    public Integer getTodayRealTimeUserCount() {
        try {
            String today = LocalDate.now()
                    .format(DateTimeFormatter.BASIC_ISO_DATE);
            String redisKey = REDIS_KEY_PREFIX + today + REDIS_KEY_SUFFIX;

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
        return accessDataMapper.getAccessTrend(days);
    }

    /**
     * 获取总访问人数
     */
    @Override
    public Long getTotalUserCount() {
        return accessDataMapper.getTotalUserCount();
    }
}
