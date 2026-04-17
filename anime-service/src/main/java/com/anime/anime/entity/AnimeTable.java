package com.anime.anime.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class AnimeTable {
    @Id
    private Long id;                     // 自增主键ID

    @JsonSerialize(using = ToStringSerializer.class)
    public Long getId() {
        return id;
    }

    private Integer vodId;                  // 视频ID

    private Integer typeId;
    @TableField("type_id_1")// 主分类ID
    private Integer typeId1;                // 一级分类ID

    private Integer groupId;                // 分组ID，0表示未分组

    private String vodName;                 // 视频名称/标题

    private String vodSub;                  // 副标题/别名，包含多语言标题

    private String vodEn;                   // 英文名或拼音名

    private Integer vodStatus;                 // 状态：1-已发布/正常

    private String vodLetter;               // 首字母索引，用于字母检索

    private String vodColor;                // 颜色标记，用于特殊样式

    private String vodTag;                  // 标签关键词，如：动作,动画,奇幻

    private String vodClass;                // 分类标签，如：动作,动画,奇幻,中国动漫

    private String vodPic;                  // 封面图片地址

    private String vodPicThumb;             // 缩略图地址

    private String vodPicSlide;             // 幻灯片/滚动大图地址

    private String vodPicScreenshot;        // 截图地址

    private String vodActor;                // 演员/配音演员列表，多个用逗号分隔

    private String vodDirector;             // 导演

    private String vodWriter;               // 编剧

    private String vodBehind;               // 幕后人员/其他制作人员

    private String vodBlurb;                // 简介/短描述

    private String vodRemarks;              // 备注信息，如：第19集

    private String vodPubdate;              // 上映/发布日期，格式：YYYY-MM-DD(地区)

    private Integer vodTotal;                  // 总集数

    private String vodSerial;               // 连载状态：0-连载中或未定义

    private String vodTv;                   // 电视台/播出平台

    private String vodWeekday;              // 每周更新日

    private String vodArea;                 // 制片国家/地区

    private String vodLang;                 // 语言，如：汉语普通话

    private String vodYear;                 // 上映年份

    private String vodVersion;              // 版本，如：剧场版、OVA

    private String vodState;                // 状态描述，如：更新至XX集

    private String vodAuthor;               // 作者

    private String vodJumpurl;              // 跳转URL

    private String vodTpl;                  // 自定义模板

    private String vodTplPlay;              // 播放页模板

    private String vodTplDown;              // 下载页模板

    private Integer vodIsend;                  // 是否完结：0-未完结，1-已完结

    private Integer vodLock;                   // 是否锁定：0-未锁定，1-锁定

    private Integer vodLevel;                  // 访问所需用户等级，0表示不限等级

    private Integer vodCopyright;              // 版权状态：0-无版权限制

    private Integer vodPoints;              // 观看所需积分，0表示不需要

    private Integer vodPointsPlay;          // 播放所需积分

    private Integer vodPointsDown;          // 下载所需积分

    private Integer vodHits;                // 总点击/播放量

    private Integer vodHitsDay;             // 日点击量

    private Integer vodHitsWeek;            // 周点击量

    private Integer vodHitsMonth;           // 月点击量

    private String vodDuration;             // 单集时长，如：22分钟

    private Integer vodUp;                  // 点赞/顶的数量

    private Integer vodDown;                // 点踩/踩的数量

    private BigDecimal vodScore;            // 评分，如：4.0

    private Integer vodScoreAll;            // 总分/总评分值

    private Integer vodScoreNum;            // 评分人数

    private Date vodTime;                   // 最后更新时间

    private Integer vodTimeAdd;             // 添加时间（Unix时间戳）

    private Integer vodTimeHits;            // 最近点击时间戳，0表示无记录

    private Integer vodTimeMake;            // 制作/创建时间戳，0表示未设置

    private Integer vodTrysee;                // 试看时长（秒），0表示不能试看或完整观看

    private Integer vodDoubanId;            // 豆瓣电影ID

    private BigDecimal vodDoubanScore;      // 豆瓣评分

    private String vodReurl;                // 跳转/重定向URL

    private String vodRelVod;               // 关联的视频ID列表

    private String vodRelArt;               // 关联的文章ID列表

    private String vodPwd;                  // 访问密码

    private String vodPwdUrl;               // 密码访问的链接

    private String vodPwdPlay;              // 播放密码

    private String vodPwdPlayUrl;           // 密码播放链接

    private String vodPwdDown;              // 下载密码

    private String vodPwdDownUrl;           // 密码下载链接

    private String vodContent;              // 详细介绍/剧情简介（可能很长，用TEXT）

    private String vodPlayFrom;             // 播放器来源标识，如：hhm3u8

    private String vodPlayServer;           // 播放服务器地址

    private String vodPlayNote;             // 播放备注，如：提示：请切换线路

    private String vodDownFrom;             // 下载来源标识

    private String vodDownServer;           // 下载服务器地址

    private String vodDownNote;             // 下载备注

    private String vodDownUrl;              // 下载URL数据

    private Integer vodPlot;                  // 剧情/分集数量，0表示未设置

    private String vodPlotName;             // 剧情/分集名称

    private String vodPlotDetail;           // 剧情/分集详情

    private String typeName;                // 分类名称（冗余字段，便于直接展示）
    // 入库时间
    private Date createAt;
    //修改时间
    private Date updateAt;
}
