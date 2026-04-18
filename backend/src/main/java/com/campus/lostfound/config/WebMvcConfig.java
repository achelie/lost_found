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
        // 确保路径以 / 结尾
        String resourcePath = uploadPath;
        if (!resourcePath.endsWith("/") && !resourcePath.endsWith("\\")) {
            resourcePath += "/";
        }
        // 如果是Windows路径，转换为file URL格式
        if (!resourcePath.startsWith("file:")) {
            resourcePath = "file:" + resourcePath.replace("\\", "/");
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourcePath);
    }
}
