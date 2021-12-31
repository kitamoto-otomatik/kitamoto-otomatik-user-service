package com.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

@EnableWebMvc
@Configuration
public class AppConfig implements WebMvcConfigurer {
    @Bean
    public Random random() throws NoSuchAlgorithmException {
        return SecureRandom.getInstanceStrong();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**");
    }
}
