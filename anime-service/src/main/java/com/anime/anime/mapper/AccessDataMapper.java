package com.anime.anime.mapper;

import com.anime.anime.entity.AccessData;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 访问数据 Mapper
 */
@Mapper
public interface AccessDataMapper extends BaseMapper<AccessData> {

    /**
     * 查询最近N天的访问趋势
     *
     * @param days 天数
     * @return 日期和访问人数列表
     */
    List<AccessData> getAccessTrend(int days);

    /**
     * 查询总访问人数（所有日期的累加）
     *
     * @return 总访问记录数
     */
    Long getTotalUserCount();
}
