package com.anime.anime.mapper;

import com.anime.anime.entity.AccessData;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 访问数据 Mapper
 */
@Mapper
public interface AccessDataMapper extends BaseMapper<AccessData> {

    /**
     * 查询最近N天的访问趋势
     * @param days 天数
     * @return 日期和访问人数列表
     */
    @Select("SELECT date, user_count FROM access_data " +
            "WHERE date >= DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL #{days} DAY), '%Y%m%d') " +
            "ORDER BY date ASC")
    @Results({
        @Result(property = "date", column = "date"),
        @Result(property = "userCount", column = "user_count")
    })
    List<AccessData> getAccessTrend(int days);

    /**
     * 查询总访问人数（所有日期的累加）
     * @return 总访问记录数
     */
    @Select("SELECT SUM(user_count) FROM access_data")
    Long getTotalUserCount();
}
