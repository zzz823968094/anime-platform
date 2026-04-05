package com.anime.anime.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("search_log")
public class SearchLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String keyword;

    private Long userId;

    private String ip;

    private LocalDateTime createdAt;
}