package com.anime.video.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("anime")
public class Anime {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer viewCount;
}
