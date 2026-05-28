package com.anime.anime.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.anime.anime.entity.DeviceStatistics;
import com.anime.anime.mapper.DeviceStatisticsMapper;
import com.anime.anime.service.DeviceStatisticsService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 设备统计服务
 * 从 Gateway 接收设备信息上报，存储到 Redis，定时聚合到数据库
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceStatisticsServiceImpl extends ServiceImpl<DeviceStatisticsMapper, DeviceStatistics> implements DeviceStatisticsService {

    private static final String REDIS_KEY_PREFIX = "device:";
    private static final String REDIS_KEY_SUFFIX = ":devices";
    private final StringRedisTemplate redisTemplate;
    private final DeviceStatisticsMapper deviceStatisticsMapper;

    /**
     * 记录设备信息到Redis
     *
     * @param ip          客户端IP
     * @param deviceModel 设备型号
     * @param os          操作系统
     */
    @Override
    public void recordDevice(String ip, String deviceModel, String os) {
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            return;
        }

        // 设置默认值
        deviceModel = deviceModel == null || deviceModel.isEmpty() ? "Unknown" : deviceModel;
        os = os == null || os.isEmpty() ? "Unknown" : os;

        try {
            // 获取当前日期作为Key
            String dateKey = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            String redisKey = REDIS_KEY_PREFIX + dateKey + REDIS_KEY_SUFFIX;

            // 构建设备信息JSON
            JSONObject deviceInfo = new JSONObject();
            deviceInfo.set("ip", ip);
            deviceInfo.set("deviceModel", deviceModel);
            deviceInfo.set("os", os);

            // 将设备信息添加到Redis Hash中（按设备型号+系统分组）
            String deviceKey = deviceModel + "|" + os;
            redisTemplate.opsForHash().put(redisKey, deviceKey + ":" + ip, JSONUtil.toJsonStr(deviceInfo));

            // 设置过期时间为3天（防止Redis数据堆积）
            redisTemplate.expire(redisKey, java.time.Duration.ofDays(3));

            log.debug("记录设备信息: {} -> {}|{}", dateKey, deviceModel, os);
        } catch (Exception e) {
            log.error("记录设备信息失败: {}|{}|{}", ip, deviceModel, os, e);
        }
    }

    /**
     * 将前一天的设备数据聚合到数据库
     */
    @Override
    public void aggregateDeviceData() {
        log.info("开始执行设备数据聚合任务");
        try {
            // 获取昨天的日期Key
            String yesterday = LocalDate.now().minusDays(1)
                    .format(DateTimeFormatter.BASIC_ISO_DATE);
            String redisKey = REDIS_KEY_PREFIX + yesterday + REDIS_KEY_SUFFIX;

            // 从Redis中获取所有设备信息
            Map<Object, Object> deviceMap = redisTemplate.opsForHash().entries(redisKey);

            if (deviceMap == null || deviceMap.isEmpty()) {
                log.info("昨天没有设备数据需要聚合");
                return;
            }

            // 按设备型号+系统分组统计
            Map<String, List<String>> deviceIpMap = new HashMap<>();

            for (Map.Entry<Object, Object> entry : deviceMap.entrySet()) {
                String value = (String) entry.getValue();
                if (value != null) {
                    try {
                        JSONObject deviceInfo = JSONUtil.parseObj(value);
                        String ip = deviceInfo.getStr("ip");
                        String deviceModel = deviceInfo.getStr("deviceModel");
                        String os = deviceInfo.getStr("os");

                        String deviceKey = deviceModel + "|" + os;
                        deviceIpMap.computeIfAbsent(deviceKey, k -> new ArrayList<>()).add(ip);
                    } catch (Exception e) {
                        log.error("解析设备信息失败: {}", value, e);
                    }
                }
            }

            Integer dateInt = Integer.parseInt(yesterday);

            // 遍历每个设备型号，插入或更新数据库
            for (Map.Entry<String, List<String>> entry : deviceIpMap.entrySet()) {
                String[] parts = entry.getKey().split("\\|");
                String deviceModel = parts.length > 0 ? parts[0] : "Unknown";
                String os = parts.length > 1 ? parts[1] : "Unknown";

                // 去重IP
                Set<String> uniqueIps = new HashSet<>(entry.getValue());
                int userCount = uniqueIps.size();

                // 检查是否已存在该日期和设备型号的记录
                DeviceStatistics existing = deviceStatisticsMapper.selectOne(
                        new LambdaQueryWrapper<DeviceStatistics>()
                                .eq(DeviceStatistics::getDate, dateInt)
                                .eq(DeviceStatistics::getDeviceModel, deviceModel)
                                .eq(DeviceStatistics::getOs, os)
                );

                JSONObject object = new JSONObject();
                object.set("ips", uniqueIps);
                String ips = JSONUtil.toJsonStr(object);

                if (existing != null) {
                    // 更新现有记录
                    existing.setUserCount(userCount);
                    existing.setIp(ips);
                    deviceStatisticsMapper.updateById(existing);
                    log.info("更新设备数据: date={}, deviceModel={}, os={}, userCount={}",
                            yesterday, deviceModel, os, userCount);
                } else {
                    // 插入新记录
                    DeviceStatistics deviceStatistics = new DeviceStatistics();
                    deviceStatistics.setDate(dateInt);
                    deviceStatistics.setDeviceModel(deviceModel);
                    deviceStatistics.setOs(os);
                    deviceStatistics.setUserCount(userCount);
                    deviceStatistics.setIp(ips);
                    deviceStatisticsMapper.insert(deviceStatistics);
                    log.info("新增设备数据: date={}, deviceModel={}, os={}, userCount={}",
                            yesterday, deviceModel, os, userCount);
                }
            }

            log.info("设备数据聚合任务执行完成，共处理 {} 种设备类型", deviceIpMap.size());
        } catch (Exception e) {
            log.error("设备数据聚合任务执行失败", e);
        }
    }

    /**
     * 每天凌晨0点执行，确保前一天的数据完全聚合
     */
    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Shanghai")
    public void aggregateDailyDeviceData() {
        log.info("开始执行每日设备数据最终聚合任务");
        aggregateDeviceData();
    }

    /**
     * 获取指定日期的设备统计数据
     */
    @Override
    public List<DeviceStatistics> getDeviceByDate(String date) {
        try {
            Integer dateInt = Integer.parseInt(date);
            return deviceStatisticsMapper.getDeviceByDate(dateInt);
        } catch (Exception e) {
            log.error("查询设备数据失败: date={}", date, e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取最近N天的设备统计趋势
     */
    @Override
    public List<DeviceStatistics> getDeviceTrend(int days) {
        List<DeviceStatistics> deviceTrend = deviceStatisticsMapper.getDeviceTrend(days);
        return deviceTrend == null ? new ArrayList<>() : deviceTrend;
    }

    /**
     * 获取总访问人数
     */
    @Override
    public Long getTotalUserCount() {
        Long totalUserCount = deviceStatisticsMapper.getTotalUserCount();
        return totalUserCount == null ? 0 : totalUserCount;
    }
}
