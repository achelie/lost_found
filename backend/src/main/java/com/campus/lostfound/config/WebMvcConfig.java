package com.campus.lostfound.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Value("${upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 把本地文件目录转换成 Spring 可识别的资源地址
        String resourcePath = uploadPath;
        if (!resourcePath.endsWith("/") && !resourcePath.endsWith("\\")) {
            resourcePath += "/";
        }
        // Windows 本地路径要转成 file: URL，浏览器才能通过 /uploads/** 访问
        if (!resourcePath.startsWith("file:")) {
            resourcePath = "file:" + resourcePath.replace("\\", "/");
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourcePath);
    }
}
