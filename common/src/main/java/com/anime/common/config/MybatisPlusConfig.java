package com.anime.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus配置类
 * 遵循阿里巴巴开发规范，统一配置分页、乐观锁、防全表操作等插件
 *
 * @author anime-platform
 * @date 2026-05-16
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 配置MyBatis-Plus拦截器
     * 包含：分页插件、乐观锁插件、防全表更新删除插件
     *
     * @return MybatisPlusInterceptor
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 分页插件
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        // 设置最大单页限制数量，默认500条，-1不受限制
        paginationInnerInterceptor.setMaxLimit(500L);
        // 溢出总页数后是否进行处理（默认不处理）
        paginationInnerInterceptor.setOverflow(false);
        interceptor.addInnerInterceptor(paginationInnerInterceptor);

        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // 防全表更新与删除插件
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        return interceptor;
    }

    /**
     * 自动填充处理器
     * 实现createTime、updateTime、createBy、updateBy的自动填充
     *
     * @return MetaObjectHandler
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                // 创建时间
                this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
                // 更新时间
                this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
                // 创建人（需要从上下文获取，这里暂时设为null，实际使用时通过ThreadLocal获取）
                this.strictInsertFill(metaObject, "createBy", Long.class, getCurrentUserId());
                // 更新人
                this.strictInsertFill(metaObject, "updateBy", Long.class, getCurrentUserId());
                // 逻辑删除标识默认值
                this.strictInsertFill(metaObject, "isDeleted", Integer.class, 0);
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                // 更新时间
                this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
                // 更新人
                this.strictUpdateFill(metaObject, "updateBy", Long.class, getCurrentUserId());
            }

            /**
             * 获取当前用户ID
             * 实际项目中应从SecurityContext或ThreadLocal中获取
             *
             * @return 当前用户ID
             */
            private Long getCurrentUserId() {
                // TODO: 从SecurityContext或ThreadLocal中获取当前用户ID
                // 示例：return SecurityContextHolder.getContext().getAuthentication().getPrincipal().getId();
                return null;
            }
        };
    }
}
