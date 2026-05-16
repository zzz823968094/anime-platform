package com.anime.anime.entity.query;

import com.anime.common.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 动漫列表查询参数
 *
 * @author anime-platform
 * @date 2026-05-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AnimeListQuery extends PageQuery {

    /**
     * 类型
     */
    private String type;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 年份
     */
    private Integer year;

    /**
     * 风格/流派
     */
    private String genre;

    /**
     * 排序方式
     */
    private String sort;

    /**
     * 关键词
     */
    private String keyword;
}
