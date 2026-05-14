package com.anime.anime.service;

import com.anime.anime.entity.AccessUserDetail;
import com.anime.anime.entity.dto.LocationStatDTO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 访问用户详情服务接口
 *
 * @author anime-platform
 * @since 2026-05-14
 */
public interface AccessUserDetailService extends IService<AccessUserDetail> {

    /**
     * 记录用户访问（同时保存用户ID和IP）
     *
     * @param userId 用户ID，未登录为null
     * @param ip     客户端IP
     * @param sign   访问标识：app/web
     */
    void recordUserAccess(Long userId, String ip, String sign);

    /**
     * 定时任务：从Redis批量读取访问记录并入库
     */
    void batchFlushAccessData();

    /**
     * 计算留存率
     *
     * @param baseDate 基准日期 YYYYMMDD
     * @param days     留存天数数组（1、7、15、30、180）
     * @return 留存率数据 Map<days, retentionRate>
     */
    Map<Integer, Double> calculateRetentionRate(Integer baseDate, Integer[] days);

    /**
     * 获取地理位置统计数据
     *
     * @param days 统计最近N天的数据
     * @return 地理位置统计数据列表
     */
    List<LocationStatDTO> getLocationStatistics(Integer days);

    /**
     * 批量更新IP地理位置信息
     * 使用ip-api.com API获取地理位置
     */
    void batchUpdateLocationInfo();
}
