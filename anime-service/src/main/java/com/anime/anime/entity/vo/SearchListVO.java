package com.anime.anime.entity.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class SearchListVO implements Serializable {
    private String keyword;
    private Integer cnt;
}
