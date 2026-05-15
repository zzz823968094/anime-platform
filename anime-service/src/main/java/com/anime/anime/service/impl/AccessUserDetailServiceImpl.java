package com.anime.anime.service.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.anime.anime.entity.AccessUserDetail;
import com.anime.anime.entity.dto.LocationStatDTO;
import com.anime.anime.mapper.AccessUserDetailMapper;
import com.anime.anime.service.AccessUserDetailService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 访问用户详情服务实现类
 *
 * @author anime-platform
 * @since 2026-05-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccessUserDetailServiceImpl extends ServiceImpl<AccessUserDetailMapper, AccessUserDetail>
        implements AccessUserDetailService {

    private final AccessUserDetailMapper accessUserDetailMapper;

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * IP地理位置查询API地址（批量接口）
     */
    private static final String IP_API_BATCH_URL = "http://ip-api.com/batch";

    /**
     * 批量处理大小（每次最多100个IP）
     */
    private static final int BATCH_SIZE = 100;

    /**
     * API请求间隔时间（毫秒）- 免费版限制每分钟45次请求
     */
    private static final long API_REQUEST_INTERVAL = 1333L; // 1000ms * 60 / 45 ≈ 1333ms

    /**
     * Redis Key前缀 - 访问记录Set
     */
    private static final String REDIS_KEY_PREFIX = "access:user:detail:";

    /**
     * Redis Key过期时间（天）
     */
    private static final long REDIS_KEY_EXPIRE_DAYS = 3L;

    /**
     * 记录用户访问（同时保存用户ID和IP）
     * 采用Redis缓存策略，定时任务批量入库
     *
     * @param userId 用户ID，未登录为null
     * @param ip     客户端IP
     * @param sign   访问标识：app/web
     */
    @Override
    public void recordUserAccess(Long userId, String ip, String sign) {
        // 参数校验
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            return;
        }

        // 过滤内网IP
        if (isInternalIp(ip)) {
            log.debug("过滤内网IP: ip={}, userId={}", ip, userId);
            return;
        }

        // 设置默认值
        sign = sign == null ? "app" : sign;
        String visitDate = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);

        try {
            // 构建Redis Key: access:user:detail:20260514
            String redisKey = REDIS_KEY_PREFIX + visitDate;

            // 构建Redis Value: userId:ip:sign
            String redisValue = (userId != null ? userId : "null") + ":" + ip + ":" + sign;

            // 存入Redis Set（自动去重）
            Long success = stringRedisTemplate.opsForSet().add(redisKey, redisValue);

            // 设置过期时间（7天）
            if (success != null && success > 0) {
                stringRedisTemplate.expire(redisKey, REDIS_KEY_EXPIRE_DAYS, TimeUnit.DAYS);
                log.debug("访问记录已存入Redis: key={}, value={}", redisKey, redisValue);
            }
        } catch (Exception e) {
            log.error("记录用户访问失败: userId={}, ip={}, sign={}", userId, ip, sign, e);
        }
    }

    /**
     * 判断是否为内网IP
     *
     * @param ip IP地址
     * @return true-内网IP, false-公网IP
     */
    private boolean isInternalIp(String ip) {
        // 过滤IPv6本地回环地址
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return true;
        }

        // 过滤IPv4内网地址段
        if (ip.startsWith("127.") || ip.startsWith("10.") || ip.startsWith("192.168.")) {
            return true;
        }

        // 过滤172.16.0.0 - 172.31.255.255
        if (ip.startsWith("172.")) {
            try {
                String[] parts = ip.split("\\.");
                int secondOctet = Integer.parseInt(parts[1]);
                return secondOctet >= 16 && secondOctet <= 31;
            } catch (Exception e) {
                log.warn("解析IP失败: ip={}", ip);
                return false;
            }
        }

        return false;
    }

    /**
     * 计算留存率
     *
     * @param baseDate 基准日期 YYYYMMDD
     * @param days     留存天数数组（1、7、15、30、180）
     * @return 留存率数据 Map<days, retentionRate>
     */
    @Override
    public Map<Integer, Double> calculateRetentionRate(Integer baseDate, Integer[] days) {
        Map<Integer, Double> retentionMap = new LinkedHashMap<>();

        try {
            // 获取基准日期的活跃用户数
            Long activeUserCount = accessUserDetailMapper.getActiveUserCount(baseDate);

            if (activeUserCount == null || activeUserCount == 0) {
                log.warn("基准日期 {} 没有活跃用户", baseDate);
                for (Integer day : days) {
                    retentionMap.put(day, 0.0);
                }
                return retentionMap;
            }

            // 计算每个留存天数的留存率
            for (Integer day : days) {
                Long retentionCount = accessUserDetailMapper.getRetentionCount(baseDate, day);

                if (retentionCount == null) {
                    retentionCount = 0L;
                }

                // 计算留存率（百分比）
                double retentionRate = (double) retentionCount / activeUserCount * 100;
                retentionMap.put(day, Math.round(retentionRate * 100.0) / 100.0);

                log.info("留存率计算: 基准日期={}, 留存天数={}, 活跃用户={}, 留存用户={}, 留存率={}%",
                        baseDate, day, activeUserCount, retentionCount, retentionRate);
            }
        } catch (Exception e) {
            log.error("计算留存率失败: baseDate={}", baseDate, e);
        }

        return retentionMap;
    }

    /**
     * 获取地理位置统计数据
     *
     * @param days 统计最近N天的数据
     * @return 地理位置统计数据列表
     */
    @Override
    public List<LocationStatDTO> getLocationStatistics(Integer days) {
        try {
            // Mapper直接返回强类型DTO列表
            List<LocationStatDTO> result = accessUserDetailMapper.getLocationStats(days);

            if (result == null || result.isEmpty()) {
                log.info("获取地理位置统计数据: days={}, 记录数=0", days);
                return Collections.emptyList();
            }

            // 计算总用户数用于占比计算
            long totalUsers = result.stream()
                    .mapToLong(dto -> dto.getUserCount() != null ? dto.getUserCount() : 0L)
                    .sum();

            // 计算每个地区的用户占比
            result.forEach(dto -> {
                long userCount = dto.getUserCount() != null ? dto.getUserCount() : 0L;
                Double percentage = totalUsers > 0
                        ? Math.round((double) userCount / totalUsers * 10000.0) / 100.0
                        : 0.0;
                dto.setUserPercentage(percentage);
            });

            log.info("获取地理位置统计数据: days={}, 记录数={}, 总用户数={}", days, result.size(), totalUsers);
            return result;
        } catch (Exception e) {
            log.error("获取地理位置统计数据失败: days={}", days, e);
            return Collections.emptyList();
        }
    }

    /**
     * 定时任务：从Redis批量读取访问记录并入库
     * 每天凌晨00:00:01点执行，处理昨天的数据
     */
    @Override
    @Scheduled(cron = "1 0 0 * * ?", zone = "Asia/Shanghai")
    @Transactional(rollbackFor = Exception.class)
    public void batchFlushAccessData() {
        log.info("开始执行访问数据批量入库任务");
        try {
            // 获取昨天的日期
            String yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE);
            String redisKey = REDIS_KEY_PREFIX + yesterday;

            // 从Redis获取所有访问记录
            Set<String> accessRecords = stringRedisTemplate.opsForSet().members(redisKey);

            if (accessRecords == null || accessRecords.isEmpty()) {
                log.info("Redis中无访问记录: key={}", redisKey);
                return;
            }

            log.info("Redis中待入库记录数: {}, date={}", accessRecords.size(), yesterday);

            int successCount = 0;
            int failCount = 0;
            Integer visitDate = Integer.parseInt(yesterday);
            LocalDateTime now = LocalDateTime.now();

            for (String record : accessRecords) {
                try {
                    // 解析记录: userId:ip:sign
                    String[] parts = record.split(":");
                    if (parts.length != 3) {
                        log.warn("记录格式错误: {}", record);
                        failCount++;
                        continue;
                    }

                    Long userId = "null".equals(parts[0]) ? null : Long.parseLong(parts[0]);
                    String ip = parts[1];
                    String sign = parts[2];

                    // 查询是否已存在该用户+IP+日期+标识的记录
                    LambdaQueryWrapper<AccessUserDetail> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(AccessUserDetail::getUserId, userId)
                            .eq(AccessUserDetail::getIp, ip)
                            .eq(AccessUserDetail::getVisitDate, visitDate)
                            .eq(AccessUserDetail::getSign, sign);

                    AccessUserDetail existing = accessUserDetailMapper.selectOne(queryWrapper);

                    if (existing != null) {
                        // 更新现有记录
                        existing.setLastVisitTime(java.sql.Timestamp.valueOf(now));
                        existing.setVisitCount(existing.getVisitCount() + 1);
                        accessUserDetailMapper.updateById(existing);
                    } else {
                        // 插入新记录
                        AccessUserDetail detail = new AccessUserDetail();
                        detail.setUserId(userId);
                        detail.setIp(ip);
                        detail.setSign(sign);
                        detail.setVisitDate(visitDate);
                        detail.setFirstVisitTime(java.sql.Timestamp.valueOf(now));
                        detail.setLastVisitTime(java.sql.Timestamp.valueOf(now));
                        detail.setVisitCount(1);
                        accessUserDetailMapper.insert(detail);
                    }
                    successCount++;
                } catch (Exception e) {
                    log.error("处理访问记录失败: record={}", record, e);
                    failCount++;
                }
            }

            log.info("访问数据批量入库完成: 成功={}, 失败={}, 日期={}", successCount, failCount, yesterday);

            // 入库成功后删除Redis数据
            stringRedisTemplate.delete(redisKey);
            log.info("已删除Redis数据: key={}", redisKey);

        } catch (Exception e) {
            log.error("访问数据批量入库任务执行失败", e);
        }
    }

    /**
     * 批量更新IP地理位置信息
     * 使用ip-api.com批量接口获取地理位置
     * 每次最多查询100个IP，遵循免费版每分钟45次请求限制
     */
    @Override
    @Scheduled(cron = "0 0 2 * * ?", zone = "Asia/Shanghai")
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateLocationInfo() {
        log.info("开始执行IP地理位置批量更新任务");

        try {
            List<AccessUserDetail> needUpdateList = accessUserDetailMapper.selectNeedLocationUpdate(BATCH_SIZE);

            if (needUpdateList == null || needUpdateList.isEmpty()) {
                log.info("没有需要更新地理位置的记录");
                return;
            }

            log.info("本次需要更新的记录数: {}", needUpdateList.size());

            // 提取所有唯一的IP地址
            List<String> uniqueIps = needUpdateList.stream()
                    .map(AccessUserDetail::getIp)
                    .distinct()
                    .collect(java.util.stream.Collectors.toList());

            log.info("唯一IP数量: {}", uniqueIps.size());

            // 构建批量查询请求体（带字段裁剪和语言定制）
            List<Map<String, String>> batchRequests = uniqueIps.stream()
                    .map(ip -> {
                        Map<String, String> request = new java.util.HashMap<>();
                        request.put("query", ip);
                        request.put("fields", "status,message,country,regionName,city,isp,query");
                        request.put("lang", "zh-CN");
                        return request;
                    })
                    .collect(java.util.stream.Collectors.toList());

            String requestBody = JSONUtil.toJsonStr(batchRequests);

            // 调用批量接口
            String response = HttpUtil.createPost(IP_API_BATCH_URL)
                    .header("Content-Type", "application/json")
                    .body(requestBody)
                    .timeout(10000)
                    .execute()
                    .body();

            if (response == null || response.isEmpty()) {
                log.error("批量查询IP地理位置失败：响应为空");
                return;
            }

            // 解析响应结果（JSON数组）
            cn.hutool.json.JSONArray results = JSONUtil.parseArray(response);
            log.info("批量查询成功，返回结果数: {}", results.size());

            // 构建IP到地理位置的映射
            Map<String, JSONObject> ipLocationMap = new java.util.HashMap<>();
            List<String> failedIps = new java.util.ArrayList<>();

            for (int i = 0; i < results.size(); i++) {
                JSONObject result = results.getJSONObject(i);
                String status = result.getStr("status");
                String ip = result.getStr("query");

                if ("success".equals(status) && ip != null) {
                    ipLocationMap.put(ip, result);
                } else {
                    log.warn("IP地理位置查询失败: ip={}, message={}",
                            ip, result.getStr("message"));
                    failedIps.add(ip);
                }
            }

            // 对失败的IP进行重试（最多2次）
            if (!failedIps.isEmpty()) {
                log.info("开始重试失败的IP查询，数量: {}", failedIps.size());
                retryFailedIps(failedIps, ipLocationMap);
            }

            // 批量更新数据库
            int updateCount = 0;
            for (AccessUserDetail detail : needUpdateList) {
                try {
                    JSONObject locationInfo = ipLocationMap.get(detail.getIp());
                    if (locationInfo != null) {
                        detail.setLocationCountry(locationInfo.getStr("country"));
                        detail.setLocationProvince(locationInfo.getStr("regionName"));
                        detail.setLocationCity(locationInfo.getStr("city"));
                        detail.setLocationIsp(locationInfo.getStr("isp"));

                        accessUserDetailMapper.updateById(detail);
                        updateCount++;

                        log.debug("更新IP地理位置成功: ip={}, country={}, province={}, city={}",
                                detail.getIp(), detail.getLocationCountry(),
                                detail.getLocationProvince(), detail.getLocationCity());
                    }
                } catch (Exception e) {
                    log.error("更新IP地理位置失败: ip={}", detail.getIp(), e);
                }
            }

            log.info("IP地理位置批量更新任务执行完成: 更新记录数={}", updateCount);

        } catch (Exception e) {
            log.error("IP地理位置批量更新任务执行失败", e);
        }
    }

    /**
     * 重试失败的IP查询
     *
     * @param failedIps 失败的IP列表
     * @param ipLocationMap 成功的IP地理位置映射表
     */
    private void retryFailedIps(List<String> failedIps, Map<String, JSONObject> ipLocationMap) {
        int maxRetries = 2;
        List<String> remainingFailedIps = new java.util.ArrayList<>(failedIps);

        for (int retry = 1; retry <= maxRetries; retry++) {
            if (remainingFailedIps.isEmpty()) {
                break;
            }

            log.info("第{}次重试，剩余失败IP数: {}", retry, remainingFailedIps.size());

            // 等待间隔时间后再重试
            try {
                Thread.sleep(API_REQUEST_INTERVAL);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("重试等待被中断", e);
                break;
            }

            // 构建重试请求体
            List<Map<String, String>> retryRequests = remainingFailedIps.stream()
                    .map(ip -> {
                        Map<String, String> request = new java.util.HashMap<>();
                        request.put("query", ip);
                        request.put("fields", "status,message,country,regionName,city,isp,query");
                        request.put("lang", "zh-CN");
                        return request;
                    })
                    .collect(java.util.stream.Collectors.toList());

            String requestBody = JSONUtil.toJsonStr(retryRequests);

            try {
                String response = HttpUtil.createPost(IP_API_BATCH_URL)
                        .header("Content-Type", "application/json")
                        .body(requestBody)
                        .timeout(10000)
                        .execute()
                        .body();

                if (response != null && !response.isEmpty()) {
                    cn.hutool.json.JSONArray results = JSONUtil.parseArray(response);
                    List<String> stillFailedIps = new java.util.ArrayList<>();

                    for (int i = 0; i < results.size(); i++) {
                        JSONObject result = results.getJSONObject(i);
                        String status = result.getStr("status");
                        String ip = result.getStr("query");

                        if ("success".equals(status) && ip != null) {
                            ipLocationMap.put(ip, result);
                            log.info("重试成功: ip={}", ip);
                        } else {
                            stillFailedIps.add(ip);
                            log.warn("重试失败: ip={}, message={}", ip, result.getStr("message"));
                        }
                    }

                    remainingFailedIps = stillFailedIps;
                }
            } catch (Exception e) {
                log.error("第{}次重试异常", retry, e);
                break;
            }
        }

        if (!remainingFailedIps.isEmpty()) {
            log.warn("最终仍有{}个IP查询失败: {}", remainingFailedIps.size(), remainingFailedIps);
        }
    }
}
