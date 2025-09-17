package com.example.config;

import com.example.entity.SchedulerJob;
import com.example.repository.SchedulerJobRepository;
import com.example.scheduler.DynamicJob;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * SchedulerInitializer
 * --------------------
 * 서버가 시작될 때 데이터베이스에 저장된 Quartz Job 조회
 * Quartz Scheduler 등록되어 있는지 확인 후 없는 Job 새로 등록하는 역할을 수행
 * 또한, Job 상태(RUNNING, PAUSED)에 따라 즉시 일시정지 또는 실행 대기 상태로 설정
 */
@Slf4j
@Component // Spring Bean 등록, 애플리케이션 컨텍스트에 포함
@RequiredArgsConstructor // final 필드 자동 생성자 주입
public class SchedulerInitializer
{
    // Quartz Scheduler 인스턴스 주입
    private final Scheduler scheduler;

    // DB SchedulerJob 정보를 조회하기 위한 Repository 주입
    private final SchedulerJobRepository jobRepository;

    /**
     * 서버 시작 시 실행되는 초기화 메소드
     */
    @PostConstruct // Bean 생성 후 자동으로 호출됨
    public void initJobs() throws SchedulerException
    {
        // DB 모든 스케줄러 Job 조회
        List<SchedulerJob> jobs = jobRepository.findAll();
        log.info("서버 시작 시 DB에 저장된 스케줄러 조회: 총 {}개", jobs.size());

        // 조회된 Job 하나씩 처리
        for (SchedulerJob job : jobs)
        {
            // JobKey 생성: Quartz Job 식별하는 고유 키 (이름 + 그룹)
            JobKey jobKey = JobKey.jobKey(job.getJobName(), job.getJobGroup());

            // Scheduler 이미 Job 존재하는지 확인
            if (scheduler.checkExists(jobKey))
            {
                // 존재하면 로그만 출력
                log.info("Job 이미 존재: {} / {}", job.getJobName(), job.getJobGroup());
            }
            else
            {
                // Job 존재하지 않으면 새로 등록
                log.info("Job 없음 → 새로 등록: {} / {}", job.getJobName(), job.getJobGroup());

                // JobDetail 생성
                // - DynamicJob.class: 실제 실행될 Job 클래스
                // - withIdentity(jobKey): 이름과 그룹 설정
                // - withDescription: Job 설명 추가
                // - storeDurably(): Trigger 없어도 Job 유지
                JobDetail jobDetail = JobBuilder.newJob(DynamicJob.class)
                    .withIdentity(jobKey)
                    .withDescription(job.getDescription())
                    .storeDurably()
                    .build();

                // CronTrigger 생성
                // - 이름 + 그룹으로 TriggerKey 설정
                // - forJob(jobDetail): 어떤 Job 연결될 Trigger 지정
                // - withSchedule: Cron 표현식에 따른 실행 스케줄
                // - withMisfireHandlingInstructionFireAndProceed: 놓친 실행(Misfire) 처리 정책
                CronTrigger newTrigger = TriggerBuilder.newTrigger()
                    .withIdentity(job.getJobName() + "Trigger", job.getJobGroup())
                    .forJob(jobDetail)
                    .withSchedule(CronScheduleBuilder
                        .cronSchedule(job.getCronExpression())
                        .withMisfireHandlingInstructionFireAndProceed())
                    .build();

                // Quartz Scheduler JobDetail + Trigger 등록
                scheduler.scheduleJob(jobDetail, newTrigger);

                // 상태 체크 후 PAUSED 즉시 Job 일시정지
                if ("PAUSED".equalsIgnoreCase(job.getStatus()))
                {
                    scheduler.pauseJob(jobKey);
                    log.info("Job 상태 PAUSED → Job 일시정지됨: {} / {}", job.getJobName(), job.getJobGroup());
                }
                else
                {
                    // RUNNING 상태면 로그 출력만, Scheduler Trigger 따라 실행
                    log.info("Job 상태 RUNNING → Job 실행 대기중: {} / {}", job.getJobName(), job.getJobGroup());
                }
            }
        }

        // 모든 초기화 작업 완료 후 로그
        log.info("서버 초기화 완료: Quartz Scheduler DB Job 등록 상태 확인 완료");
    }
}
