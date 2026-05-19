package com.anime.anime.entity.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 动漫信息 VO
 *
 * @author anime-platform
 * @date 2026-05-16
 */
@Data
public class AnimeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String vodName;
    private String vodPic;
    private String vodContent;
    private Integer vodHits;
    private Integer vodScore;
    private LocalDateTime updateAt;
}
