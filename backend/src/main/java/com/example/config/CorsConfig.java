package com.example.config;

import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS 설정
 * - React 프론트(포트 3000)에서 API 호출 허용
 * - 모든 엔드포인트, 모든 HTTP 메서드 허용
 */
@Configuration  // 스프링 컨테이너가 이 클래스를 Bean 인식하도록 설정
public class CorsConfig
{
    @Bean  // Spring Bean 등록되어 WebMvcConfigurer 인터페이스 구현을 통해 MVC 설정에 반영
    public WebMvcConfigurer corsConfigurer()
    {
        return new WebMvcConfigurer()
        {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry)
            {
                // 모든 API 엔드포인트에 대해 CORS 설정 적용
                registry.addMapping("/**")
                    // 허용할 출처(origin) 지정: React 프론트 로컬 서버
                    .allowedOrigins("http://localhost:3000")
                    // 허용할 HTTP 메서드: GET, POST, PUT, DELETE 등 모든 요청 허용
                    .allowedMethods("*")
                    // 허용할 헤더: 모든 요청 헤더 허용
                    .allowedHeaders("*")
                    // 쿠키, 인증 정보 전송 허용
                    .allowCredentials(true);
            }
        };
    }
}
