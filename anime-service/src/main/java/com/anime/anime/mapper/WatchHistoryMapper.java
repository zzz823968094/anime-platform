package com.anime.anime.mapper;

import com.anime.anime.entity.WatchHistory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 观看历史 Mapper
 */
@Mapper
public interface WatchHistoryMapper extends BaseMapper<WatchHistory> {

    /**
     * 查询用户的观看历史列表（按最后观看时间倒序）
     */
    @Select("SELECT * FROM watch_history WHERE user_id = #{userId} ORDER BY last_watch_time DESC LIMIT #{limit}")
    List<WatchHistory> selectByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 根据用户ID和番剧ID查询观看历史
     */
    @Select("SELECT * FROM watch_history WHERE user_id = #{userId} AND anime_id = #{animeId} LIMIT 1")
    WatchHistory selectByUserIdAndAnimeId(@Param("userId") Long userId, @Param("animeId") Long animeId);

    /**
     * 删除用户的某条观看历史
     */
    @Select("DELETE FROM watch_history WHERE user_id = #{userId} AND anime_id = #{animeId}")
    int deleteByUserIdAndAnimeId(@Param("userId") Long userId, @Param("animeId") Long animeId);

    /**
     * 清空用户的所有观看历史
     */
    @Select("DELETE FROM watch_history WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 统计用户的观看历史数量
     */
    @Select("SELECT COUNT(*) FROM watch_history WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);
}
