package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 애플리케이션 진입점
 * - 서버 실행 시 ApplicationContext 생성
 * - Quartz Config 자동 로딩
 */
@SpringBootApplication
public class SchedulerApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(SchedulerApplication.class, args);
    }
}