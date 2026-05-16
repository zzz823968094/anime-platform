package com.anime.common.query;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 分页查询基类
 *
 * @author anime-platform
 * @date 2026-05-16
 */
@Data
public class PageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 排序字段
     */
    private String orderBy;

    /**
     * 排序方式 (asc/desc)
     */
    private String orderDirection = "desc";
}
