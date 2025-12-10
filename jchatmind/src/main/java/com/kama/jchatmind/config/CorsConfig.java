package com.kama.jchatmind.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS 跨域配置
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    /**
     * 跨域配置
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 允许跨域的域名
                // 注意：如果 allowCredentials 为 true，不能使用 "*"，需要指定具体域名
                // 开发环境可以使用 "http://localhost:5173", "http://localhost:3000" 等
                .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
                // 允许任何方法（post、get等）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                // 允许任何请求头
                .allowedHeaders("*")
                // 允许携带凭证（如果需要的话）
                .allowCredentials(true)
                // 预检请求的有效期，单位为秒
                .maxAge(3600);
    }

    /**
     * 使用 CorsFilter 的方式（可选，如果上面的方式不够用可以使用这个）
     * 注意：当 allowCredentials 为 true 时，不能使用 "*" 作为 origin
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许本地开发环境的跨域调用
        // 如果需要允许所有域名，可以注释掉 allowCredentials(true) 并使用 "*"
        config.addAllowedOriginPattern("http://localhost:*");
        config.addAllowedOriginPattern("http://127.0.0.1:*");
        // 允许所有请求头
        config.addAllowedHeader("*");
        // 允许所有请求方法
        config.addAllowedMethod("*");
        // 允许携带凭证
        config.setAllowCredentials(true);
        // 预检请求的有效期，单位为秒
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
