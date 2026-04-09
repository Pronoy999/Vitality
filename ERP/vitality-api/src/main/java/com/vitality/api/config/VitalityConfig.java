package com.vitality.api.config;

import com.vitality.common.utils.SecurityUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class VitalityConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // apply to all routes
                .allowedOrigins("http://localhost:4200") // allow Angular frontend
                .allowedOrigins("http://122.166.244.91:4200") // allow Angular frontend
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Bean
    public SecurityUtils securityUtils() {
        return new SecurityUtils();
    }
}
