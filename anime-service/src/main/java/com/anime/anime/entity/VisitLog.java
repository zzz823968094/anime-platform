package com.anime.anime.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("visit_log")
public class VisitLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String page;

    private String ip;

    private Long userId;

    private LocalDate visitDate;

    private LocalDateTime createdAt;
}