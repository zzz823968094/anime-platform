package com.anime.common.base;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体类（包含通用字段）
 * 遵循阿里巴巴开发规范，所有实体类继承此类
 * 注意：不同表的字段命名可能不同，子类可通过@TableField覆盖
 *
 * @author anime-platform
 * @date 2026-05-16
 */
@Data
public class BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 创建时间
     * 默认映射到 create_time，子类可根据实际表结构覆盖
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     * 默认映射到 update_time，子类可根据实际表结构覆盖
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     * 默认映射到 create_by，子类可根据实际表结构覆盖
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 更新人ID
     * 默认映射到 update_by，子类可根据实际表结构覆盖
     */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

}
