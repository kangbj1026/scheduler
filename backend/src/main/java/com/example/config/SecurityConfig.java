package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 최소 설정 ( 개발 / 테스트용 )
 * 주요 기능
 * - 모든 요청에 대해 인증/인가 절차를 거치지 않고 허용
 * - CSRF( Cross-Site Request Forgery ) 보안 기능 비활성화
 * -
 * 주의
 * - 운영( Production ) 환경에서 사용하면 보안상 매우 위험하므로, 반드시 개발/테스트/로컬 환경에서만 활성화
 */
@Configuration
public class SecurityConfig
{
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception
    {
        http
            .csrf(CsrfConfigurer::disable)  // CSRF 비활성화 (메서드 참조)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());  // 모든 요청 허용
        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer()
    {
        // 아래 코드는 예제의 원문을 유지한 것이며, 실제로는 매우 위험합니다.
        return (web) -> web.ignoring().anyRequest();

        // 안전한 예시 (정적 리소스만 제외):
//        return (web) -> web.ignoring().requestMatchers("/css/**", "/js/**", "/images/**");
    }
}