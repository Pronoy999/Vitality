package com.vitality.api.config;

import com.vitality.common.utils.PDFGenerator;
import com.vitality.common.utils.SecurityUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.TemplateEngine;

@Configuration
public class VitalityConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // apply to all routes
                .allowedOrigins(
                        "http://localhost:4200",
                        "http://122.166.244.91:4200",
                        "http://localhost:5173",
                        "http://49.205.205.114:5173",
                        "https://vitaliti.fly.dev/"
                ) // allow React frontend
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Bean
    public SecurityUtils securityUtils() {
        return new SecurityUtils();
    }

    @Bean
    public PDFGenerator pdfGenerator(TemplateEngine templateEngine) {
        return new PDFGenerator(templateEngine);
    }
}
