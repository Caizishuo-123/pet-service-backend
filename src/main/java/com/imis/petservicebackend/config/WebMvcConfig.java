package com.imis.petservicebackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload-path}")
    private String uploadPath;

    /**
     * 跨域配置
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 静态资源映射
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        //  用户上传头像（本地磁盘）
        registry.addResourceHandler("/img/head/**")
                .addResourceLocations("file:" + uploadPath + "/img/head/");
        // 【新增】宠物图片映射
        // 映射规则：访问 http://localhost:8081/img/pet/xxx.jpg 
        // 实际指向：D:\\pet-platform\\...\\upload\\img\\pet\\xxx.jpg
        registry.addResourceHandler("/img/pet/**")
                .addResourceLocations("file:" + uploadPath + "/img/pet/");

        //  默认头像（classpath）
        registry.addResourceHandler("/img/head/**")
                .addResourceLocations("classpath:/static/img/head/");
    }
}