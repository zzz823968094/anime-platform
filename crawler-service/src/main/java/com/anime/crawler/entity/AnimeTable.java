package com.anime.crawler.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Table(name = "anime_table")
public class AnimeTable implements Serializable {
    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonProperty("id")
    private Long id;                     // 自增主键ID

    @JsonProperty("vod_id")
    private Integer vodId;                  // 视频ID

    @JsonProperty("type_id")
    private Integer typeId;

    @TableField("type_id_1")// 主分类ID
    @JsonProperty("type_id_1")
    private Integer typeId1;                // 一级分类ID

    @JsonProperty("group_id")
    private Integer groupId;                // 分组ID，0表示未分组

    @JsonProperty("vod_name")
    private String vodName;                 // 视频名称/标题

    @JsonProperty("vod_sub")
    private String vodSub;                  // 副标题/别名，包含多语言标题

    @JsonProperty("vod_en")
    private String vodEn;                   // 英文名或拼音名

    @JsonProperty("vod_status")
    private Byte vodStatus;                 // 状态：1-已发布/正常

    @JsonProperty("vod_letter")
    private String vodLetter;               // 首字母索引，用于字母检索

    @JsonProperty("vod_color")
    private String vodColor;                // 颜色标记，用于特殊样式

    @JsonProperty("vod_tag")
    private String vodTag;                  // 标签关键词，如：动作,动画,奇幻

    @JsonProperty("vod_class")
    private String vodClass;                // 分类标签，如：动作,动画,奇幻,中国动漫

    @JsonProperty("vod_pic")
    private String vodPic;                  // 封面图片地址

    @JsonProperty("vod_pic_thumb")
    private String vodPicThumb;             // 缩略图地址

    @JsonProperty("vod_pic_slide")
    private String vodPicSlide;             // 幻灯片/滚动大图地址

    @JsonProperty("vod_pic_screenshot")
    private String vodPicScreenshot;        // 截图地址

    @JsonProperty("vod_actor")
    private String vodActor;                // 演员/配音演员列表，多个用逗号分隔

    @JsonProperty("vod_director")
    private String vodDirector;             // 导演

    @JsonProperty("vod_writer")
    private String vodWriter;               // 编剧

    @JsonProperty("vod_behind")
    private String vodBehind;               // 幕后人员/其他制作人员

    @JsonProperty("vod_blurb")
    private String vodBlurb;                // 简介/短描述

    @JsonProperty("vod_remarks")
    private String vodRemarks;              // 备注信息，如：第19集

    @JsonProperty("vod_pubdate")
    private String vodPubdate;              // 上映/发布日期，格式：YYYY-MM-DD(地区)

    @JsonProperty("vod_total")
    private Integer vodTotal;                  // 总集数

    @JsonProperty("vod_serial")
    private String vodSerial;               // 连载状态：0-连载中或未定义

    @JsonProperty("vod_tv")
    private String vodTv;                   // 电视台/播出平台

    @JsonProperty("vod_weekday")
    private String vodWeekday;              // 每周更新日

    @JsonProperty("vod_area")
    private String vodArea;                 // 制片国家/地区

    @JsonProperty("vod_lang")
    private String vodLang;                 // 语言，如：汉语普通话

    @JsonProperty("vod_year")
    private String vodYear;                 // 上映年份

    @JsonProperty("vod_version")
    private String vodVersion;              // 版本，如：剧场版、OVA

    @JsonProperty("vod_state")
    private String vodState;                // 状态描述，如：更新至XX集

    @JsonProperty("vod_author")
    private String vodAuthor;               // 作者

    @JsonProperty("vod_jumpurl")
    private String vodJumpurl;              // 跳转URL

    @JsonProperty("vod_tpl")
    private String vodTpl;                  // 自定义模板

    @JsonProperty("vod_tpl_play")
    private String vodTplPlay;              // 播放页模板

    @JsonProperty("vod_tpl_down")
    private String vodTplDown;              // 下载页模板

    @JsonProperty("vod_isend")
    private Integer vodIsend;                  // 是否完结：0-未完结，1-已完结

    @JsonProperty("vod_lock")
    private Integer vodLock;                   // 是否锁定：0-未锁定，1-锁定

    @JsonProperty("vod_level")
    private Integer vodLevel;                  // 访问所需用户等级，0表示不限等级

    @JsonProperty("vod_copyright")
    private Integer vodCopyright;              // 版权状态：0-无版权限制

    @JsonProperty("vod_points")
    private Integer vodPoints;              // 观看所需积分，0表示不需要

    @JsonProperty("vod_points_play")
    private Integer vodPointsPlay;          // 播放所需积分

    @JsonProperty("vod_points_down")
    private Integer vodPointsDown;          // 下载所需积分

    @JsonProperty("vod_hits")
    private Integer vodHits;                // 总点击/播放量

    @JsonProperty("vod_hits_day")
    private Integer vodHitsDay;             // 日点击量

    @JsonProperty("vod_hits_week")
    private Integer vodHitsWeek;            // 周点击量

    @JsonProperty("vod_hits_month")
    private Integer vodHitsMonth;           // 月点击量

    @JsonProperty("vod_duration")
    private String vodDuration;             // 单集时长，如：22分钟

    @JsonProperty("vod_up")
    private Integer vodUp;                  // 点赞/顶的数量

    @JsonProperty("vod_down")
    private Integer vodDown;                // 点踩/踩的数量

    @JsonProperty("vod_score")
    private BigDecimal vodScore;            // 评分，如：4.0

    @JsonProperty("vod_score_all")
    private Integer vodScoreAll;            // 总分/总评分值

    @JsonProperty("vod_score_num")
    private Integer vodScoreNum;            // 评分人数

    @JsonProperty("vod_time")
    private Date vodTime;                   // 最后更新时间

    @JsonProperty("vod_time_add")
    private Integer vodTimeAdd;             // 添加时间（Unix时间戳）

    @JsonProperty("vod_time_hits")
    private Integer vodTimeHits;            // 最近点击时间戳，0表示无记录

    @JsonProperty("vod_time_make")
    private Integer vodTimeMake;            // 制作/创建时间戳，0表示未设置

    @JsonProperty("vod_trysee")
    private Integer vodTrysee;                // 试看时长（秒），0表示不能试看或完整观看

    @JsonProperty("vod_douban_id")
    private Integer vodDoubanId;            // 豆瓣电影ID

    @JsonProperty("vod_douban_score")
    private BigDecimal vodDoubanScore;      // 豆瓣评分

    @JsonProperty("vod_reurl")
    private String vodReurl;                // 跳转/重定向URL

    @JsonProperty("vod_rel_vod")
    private String vodRelVod;               // 关联的视频ID列表

    @JsonProperty("vod_rel_art")
    private String vodRelArt;               // 关联的文章ID列表

    @JsonProperty("vod_pwd")
    private String vodPwd;                  // 访问密码

    @JsonProperty("vod_pwd_url")
    private String vodPwdUrl;               // 密码访问的链接

    @JsonProperty("vod_pwd_play")
    private String vodPwdPlay;              // 播放密码

    @JsonProperty("vod_pwd_play_url")
    private String vodPwdPlayUrl;           // 密码播放链接

    @JsonProperty("vod_pwd_down")
    private String vodPwdDown;              // 下载密码

    @JsonProperty("vod_pwd_down_url")
    private String vodPwdDownUrl;           // 密码下载链接

    @JsonProperty("vod_content")
    private String vodContent;              // 详细介绍/剧情简介（可能很长，用TEXT）

    @JsonProperty("vod_play_from")
    private String vodPlayFrom;             // 播放器来源标识，如：hhm3u8

    @JsonProperty("vod_play_server")
    private String vodPlayServer;           // 播放服务器地址

    @JsonProperty("vod_play_note")
    private String vodPlayNote;             // 播放备注，如：提示：请切换线路

    @JsonProperty("vod_down_from")
    private String vodDownFrom;             // 下载来源标识

    @JsonProperty("vod_down_server")
    private String vodDownServer;           // 下载服务器地址

    @JsonProperty("vod_down_note")
    private String vodDownNote;             // 下载备注

    @JsonProperty("vod_down_url")
    private String vodDownUrl;              // 下载URL数据

    @JsonProperty("vod_plot")
    private Integer vodPlot;                  // 剧情/分集数量，0表示未设置

    @JsonProperty("vod_plot_name")
    private String vodPlotName;             // 剧情/分集名称

    @JsonProperty("vod_plot_detail")
    private String vodPlotDetail;           // 剧情/分集详情

    @JsonProperty("type_name")
    private String typeName;                // 分类名称（冗余字段，便于直接展示）

    @TableField(exist = false)
    private java.util.Date updatedAt;       // 更新时间（非数据库字段，用于标记记录更新时间）
}
