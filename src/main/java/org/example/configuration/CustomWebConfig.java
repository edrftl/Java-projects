package org.example.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CustomWebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")  // Вказуємо шляхи, для яких дозволяємо CORS
                .allowedOrigins("http://localhost:5173")  // Дозволяємо доступ із цього домену
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // Дозволяємо ці HTTP-методи
                .allowedHeaders("*")  // Дозволяємо всі заголовки
                .allowCredentials(true);  // Дозволяємо передавати кукі та аутентифікаційні дані
    }
}
