package com.example.config;

import jakarta.annotation.PreDestroy;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Properties;

/**
 * Quartz Scheduler 설정 클래스
 * Spring Boot Quartz 사용할 때 기본 자동 설정으로는 지원되지 않는 세부적인 설정들을 직접 구성하기 위한 Configuration 클래스
 * 주요 기능:
 * - 커스텀 스레드 풀 크기 설정
 * - 스케줄러 인스턴스 명명
 * - JobStore 타입 지정
 * - 스케줄러 라이프사이클 관리
 */
@Configuration
public class SchedulerConfig
{
    private Scheduler scheduler;

    /**
     * Quartz Scheduler Bean 생성 및 설정
     * Spring Boot 기본 Quartz 자동 설정과 별도로 커스텀 스케줄러를 생성
     * Properties 통해 세밀한 설정을 적용
     * @return 설정된 Scheduler 인스턴스
     * @throws SchedulerException 스케줄러 생성 또는 시작 중 오류 발생시
     */
    @Bean
    @Primary
    public Scheduler scheduler() throws SchedulerException
    {
        // Quartz 설정을 위한 Properties 객체 생성
        Properties quartzProperties = new Properties();

        // 스레드 풀 클래스
        quartzProperties.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        // 스레드 풀 크기 (0 이상이어야 함, 권장 10 정도)
        quartzProperties.setProperty("org.quartz.threadPool.threadCount", "10");
        // 스레드 우선순위
        quartzProperties.setProperty("org.quartz.threadPool.threadPriority", "5");

        try
        {
            // StdSchedulerFactory 통해 스케줄러 생성
            StdSchedulerFactory schedulerFactory = new StdSchedulerFactory();

            // 위에서 설정한 Properties factory 초기화
            schedulerFactory.initialize(quartzProperties);

            // 실제 Scheduler 인스턴스 생성
            this.scheduler = schedulerFactory.getScheduler();

            // 스케줄러 시작
            // start() 호출 전까지는 Job 실행되지 않음
            scheduler.start();

            return scheduler;
        }
        catch (SchedulerException e)
        {
            // 스케줄러 생성/시작 실패시 로그 출력 및 예외 재발생
            System.err.println("스케줄러 초기화 실패: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 애플리케이션 종료시 스케줄러 정리 작업
     * @PreDestroy 어노테이션을 통해 Bean 소멸시 자동 호출
     * 실행 중인 Job 안전하게 종료하고 리소스를 정리
     */
    @PreDestroy
    public void destroyScheduler()
    {
        try
        {
            if (scheduler != null && !scheduler.isShutdown())
            {
                // true: 현재 실행 중인 Job 완료될 때까지 대기
                // false: 즉시 강제 종료
                scheduler.shutdown(true);
                System.out.println("스케줄러가 정상적으로 종료");
            }
        }
        catch (SchedulerException e)
        {
            System.err.println("스케줄러 종료 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * 스케줄러 상태 확인을 위한 헬퍼 메소드
     * @return 스케줄러가 시작되어 있는지 여부
     */
    public boolean isSchedulerStarted()
    {
        try
        {
            return scheduler != null && scheduler.isStarted();
        }
        catch (SchedulerException e)
        {
            return false;
        }
    }
}