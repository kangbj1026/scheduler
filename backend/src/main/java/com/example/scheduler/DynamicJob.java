package com.example.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Quartz 스케줄러에서 실행되는 실제 Job 클래스
 * 역할:
 * - Quartz 지정된 시간(Cron)마다 실행할 Job 정의
 * - 실행 시 로그를 기록하여 어떤 Job 언제 실행되었는지 확인
 * 특징:
 * - Spring Service 스케줄러 등록/중단/재개/삭제 기능과 분리
 * - 실제 수행할 작업 로직을 execute() 안에 작성
 */
public class DynamicJob implements Job
{
    private static final Logger log = LoggerFactory.getLogger(DynamicJob.class);

    /**
     * Job 실행 시 호출되는 메소드
     * @param context Quartz 제공하는 실행 컨텍스트
     * @throws JobExecutionException 실행 중 예외 발생 시
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        // Job 이름과 설명 가져오기
        String jobName = context.getJobDetail().getKey().getName();
        String desc = context.getJobDetail().getDescription();

        // 실행 시각 가져오기
        log.info("Job 실행됨: {} - 설명: {} - 실행 시간: {}", jobName, desc, context.getFireTime());

        // TODO: 실제 수행할 작업 로직 작성 가능
        // 예: 이메일 발송, 배치 처리, 외부 API 호출 등
    }
}
