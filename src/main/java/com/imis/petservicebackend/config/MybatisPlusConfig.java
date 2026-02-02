package com.imis.petservicebackend.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类，用于配置分页插件等 MyBatis-Plus 相关功能。
 * ###### 分页配置注意点：
 * 这里分页类型 `DbType.MYSQL`
 * 对于分页创建，如**果不添加这个分页创建，那么前端得到后端的结果是数据库里的所有数据，也就是不分页。**
 * 不信的话将这个MybatisConfig配置类注释掉再其启动springboot工程尝试着把接口测试下。
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 配置 MyBatis-Plus 分页插件
     * @return MybatisPlusInterceptor 对象，用于配置 MyBatis-Plus 的拦截器  物理分页
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        // 创建 MybatisPlusInterceptor 对象
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 创建 PaginationInnerInterceptor 分页插件，并添加到 MybatisPlusInterceptor 中 这里分页类型 DbType.MYSQL
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        // 返回配置好的 MybatisPlusInterceptor 对象
        return interceptor;
    }

}
