package com.example.service;

import com.example.entity.SchedulerJob;
import com.example.repository.SchedulerJobRepository;
import com.example.scheduler.DynamicJob;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * SchedulerService
 * Quartz 스케줄러를 관리하는 서비스 클래스
 * 주요 기능:
 * - 스케줄러 작업(Job) 등록, 조회, 수정, 삭제
 * - 스케줄러 작업의 실행, 중단, 재개 제어
 * - 데이터베이스와 Quartz 스케줄러 간의 동기화
 * - 모든 작업에 대한 로깅 처리
 * 사용된 기술:
 * - Quartz: 자바 기반 오픈소스 작업 스케줄링 라이브러리
 * - Spring Data JPA: 데이터베이스 접근을 위한 Repository 패턴
 * - SLF4J: 로깅을 위한 추상화 레이어
 */
@Service // Spring 서비스 계층 컴포넌트로 등록하여 비즈니스 로직 처리를 담당
@RequiredArgsConstructor  // Lombok 어노테이션으로 final 필드들에 대한 생성자를 자동 생성하여 의존성 주입
public class SchedulerService
{
    // Quartz 스케줄러 객체 - Spring Boot 자동 구성된 Scheduler 빈을 주입받음
    // 실제 작업을 스케줄링하고 실행하는 핵심 컴포넌트
    private final Scheduler scheduler;

    // 스케줄러 작업 정보를 데이터베이스에서 관리하기 위한 Repository
    // JPA 통해 SchedulerJob 엔티티의 CRUD 작업을 처리
    private final SchedulerJobRepository jobRepository;

    // 로그 기록을 위한 Logger 객체
    // SLF4J를 사용하여 클래스별로 구분된 로그를 생성
    private static final Logger log = LoggerFactory.getLogger(SchedulerService.class);

    // -----------------------------
    // 1️⃣ 스케줄러 등록 메서드
    // 새로운 작업을 Quartz 스케줄러와 데이터베이스에 등록
    // -----------------------------
    public SchedulerJob createJob(SchedulerJob job) throws SchedulerException
    {
        // 중복 작업 검사: 동일한 작업명과 그룹을 가진 작업이 이미 존재하는지 확인
        // 작업의 고유성을 보장하기 위해 jobName jobGroup 조합으로 중복을 체크
        SchedulerJob exists = jobRepository.findByJobNameAndJobGroup(job.getJobName(), job.getJobGroup());
        if (exists != null)
        {
            // 중복된 작업이 발견되면 IllegalArgumentException 던져서 등록을 중단
            // 클라이언트에게 명확한 오류 메시지 제공
            throw new IllegalArgumentException("동일한 Job 이미 존재합니다: " + job.getJobName());
        }

        // JobDetail 생성: Quartz 실제 실행될 작업의 상세 정보를 정의
        JobDetail jobDetail = JobBuilder.newJob(DynamicJob.class) // DynamicJob 클래스를 실제 실행할 작업으로 지정
            .withIdentity(job.getJobName(), job.getJobGroup()) // JobKey 설정: 작업명과 그룹으로 고유 식별자 생성
            .withDescription(job.getDescription())            // 작업에 대한 설명 정보 추가
            .storeDurably()                                   // Trigger 없어도 Job 스케줄러에 유지 (내구성 보장)
            .build(); // JobDetail 객체 생성 완료

        // Trigger 생성: 작업이 언제 실행될지를 정의하는 트리거 설정
        Trigger trigger = TriggerBuilder.newTrigger()
            .forJob(jobDetail)                                // 위에서 생성한 JobDetail 연결
            .withIdentity(job.getJobName() + "Trigger", job.getJobGroup()) // 트리거 이름: "작업명Trigger" + 그룹
            .withSchedule(CronScheduleBuilder.cronSchedule(job.getCronExpression()) // Cron 표현식으로 실행 스케줄 설정
                .withMisfireHandlingInstructionDoNothing() // Misfire 처리: 놓친 실행은 무시하고 다음 스케줄까지 대기
            )
            .build(); // Trigger 객체 생성 완료

        // Quartz Scheduler Job Trigger 함께 등록
        // 이 시점부터 설정된 Cron 표현식에 따라 작업이 자동 실행됨
        scheduler.scheduleJob(jobDetail, trigger);

        // 데이터베이스 저장을 위한 기본 상태 설정
        // 상태가 지정되지 않은 경우 기본값으로 "RUNNING"을 설정
        if (job.getStatus() == null) job.setStatus("RUNNING");

        // 작업 정보를 데이터베이스에 영구 저장
        // JPA Repository 통해 SchedulerJob 엔티티를 저장하고 생성된 ID 등을 받아옴
        SchedulerJob savedJob = jobRepository.save(job);

        // 작업 등록 완료에 대한 정보 로그 기록
        // 디버깅과 운영 모니터링을 위한 상세 정보 포함
        log.info("Job 등록됨: {} - 그룹: {} - 설명: {} - 상태: {}", job.getJobName(), job.getJobGroup(),
            job.getDescription(), job.getStatus());

        // 데이터베이스에 저장된 작업 객체를 반환 (생성된 ID 포함)
        return savedJob;
    }

    // -----------------------------
    // 2️⃣ 모든 스케줄러 조회 메서드
    // 데이터베이스에 저장된 모든 작업 정보를 조회하여 반환
    // -----------------------------
    public List<SchedulerJob> getAllJobs()
    {
        // JPA Repository 통해 데이터베이스의 모든 SchedulerJob 레코드 조회
        // findAll() 메서드는 테이블의 모든 행을 List<SchedulerJob>로 반환
        List<SchedulerJob> jobs = jobRepository.findAll();

        // 조회된 작업 개수를 포함한 정보 로그 기록
        // 시스템 상태 모니터링과 디버깅에 유용한 정보 제공
        log.info("모든 Job 조회: 총 {}개", jobs.size());

        // 조회된 작업 목록을 호출자에게 반환
        return jobs;
    }

    // -----------------------------
    // 3️⃣ 수동 실행 메서드 (Run Now)
    // 지정된 작업을 즉시 실행 (스케줄과 무관하게 강제 실행)
    // -----------------------------
    public void runJobNow(String jobName, String jobGroup) throws SchedulerException
    {
        // 데이터베이스에서 해당 작업 정보 조회
        // 작업명과 그룹으로 식별하여 실제 존재하는 작업인지 확인
        SchedulerJob job = jobRepository.findByJobNameAndJobGroup(jobName, jobGroup);
        if (job == null)
        {
            // 작업이 존재하지 않는 경우 경고 로그 기록 후 메서드 종료
            // 예외를 던지지 않고 조용히 실패 처리 (운영상 안정성 고려)
            log.warn("수동 실행 실패: Job 없음 - {} / {}", jobName, jobGroup);
            return;
        }

        // 작업 상태 확인: PAUSED(중단) 상태인 작업은 수동 실행하지 않음
        // 중단된 작업의 의도하지 않은 실행을 방지하는 안전장치
        if ("PAUSED".equals(job.getStatus()))
        {
            // 중단된 작업의 실행 시도에 대한 경고 로그 기록
            log.warn("수동 실행 실패: Job 중단 상태 - {} / {}", jobName, jobGroup);
            return;
        }

        // Quartz Scheduler 즉시 실행 명령 전송
        // triggerJob() 메서드는 스케줄과 무관하게 작업을 즉시 실행
        // JobKey 작업을 식별하여 실행 요청
        scheduler.triggerJob(JobKey.jobKey(jobName, jobGroup));

        // 수동 실행 완료에 대한 정보 로그 기록
        log.info("Job 수동 실행됨: {} - 그룹: {}", jobName, jobGroup);
    }

    // -----------------------------
    // 4️⃣ 작업 삭제 메서드
    // Quartz 스케줄러와 데이터베이스에서 작업을 완전히 제거
    // -----------------------------
    public void deleteJob(String jobName, String jobGroup) throws SchedulerException
    {
        // Quartz Scheduler 작업 삭제
        // deleteJob() 메서드는 해당 작업과 연결된 모든 트리거도 함께 삭제
        // JobKey 삭제할 작업을 식별
        scheduler.deleteJob(JobKey.jobKey(jobName, jobGroup));

        // 데이터베이스에서도 해당 작업 정보 삭제
        // 먼저 작업을 조회한 후 존재하는 경우에만 삭제 처리
        SchedulerJob job = jobRepository.findByJobNameAndJobGroup(jobName, jobGroup);
        if (job != null) {
            // JPA Repository 통해 데이터베이스에서 작업 레코드 삭제
            jobRepository.delete(job);
        }

        // 작업 삭제 완료에 대한 정보 로그 기록
        log.info("Job 삭제됨: {} - 그룹: {}", jobName, jobGroup);
    }

    // -----------------------------
    // 5️⃣ 작업 중단 메서드 (Pause)
    // 작업의 실행을 일시적으로 중단 (삭제하지 않고 실행만 멈춤)
    // -----------------------------
    public void pauseJob(String jobName, String jobGroup) throws SchedulerException
    {
        // 중단할 작업의 고유 식별자 생성
        // JobKey 작업명과 그룹의 조합으로 작업을 고유하게 식별
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);

        // Quartz Scheduler 해당 작업을 중단 상태로 변경
        // pauseJob() 메서드는 작업을 삭제하지 않고 실행만 일시 정지
        scheduler.pauseJob(jobKey);

        // 데이터베이스의 작업 상태도 동기화하여 업데이트
        SchedulerJob job = jobRepository.findByJobNameAndJobGroup(jobName, jobGroup);
        if (job != null)
        {
            // 작업 상태를 "PAUSED"로 변경하여 중단 상태임을 표시
            job.setStatus("PAUSED");

            // 변경된 상태 정보를 데이터베이스에 저장
            jobRepository.save(job);
        }

        // 작업 중단 완료에 대한 정보 로그 기록
        log.info("Job 중단됨: {} - 그룹: {}", jobName, jobGroup);
    }

    // -----------------------------
    // 6️⃣ 작업 재개 메서드 (Resume)
    // 중단된 작업을 다시 활성화하여 스케줄에 따라 실행되도록 복원
    // -----------------------------
    public void resumeJob(String jobName, String jobGroup) throws SchedulerException
    {
        // 재개할 작업의 고유 식별자 생성
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);

        // Quartz Scheduler 해당 작업의 상세 정보 조회
        // JobDetail 작업의 클래스, 데이터, 설정 등을 포함하는 객체
        JobDetail jobDetail = scheduler.getJobDetail(jobKey);
        if (jobDetail == null)
        {
            // 작업이 스케줄러에 존재하지 않는 경우 경고 로그 기록 후 종료
            log.warn("재개 실패: JobDetail 존재하지 않음 - {} / {}", jobName, jobGroup);
            return;
        }

        // 기존 트리거 정보 조회 (트리거명은 등록 시 "작업명Trigger" 형식으로 생성됨)
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName + "Trigger", jobGroup);
        Trigger oldTrigger = scheduler.getTrigger(triggerKey);

        if (oldTrigger == null)
        {
            // 트리거가 존재하지 않는 경우 경고 로그 기록 후 종료
            log.warn("재개 실패: Trigger 존재하지 않음 - {} / {}", jobName, jobGroup);
            return;
        }

        // 새로운 트리거 생성 (misfire 정책을 변경하여 놓친 실행에 대한 처리 개선)
        CronTrigger newTrigger = TriggerBuilder.newTrigger()
            .withIdentity(triggerKey) // 기존과 동일한 트리거 키 사용
            .forJob(jobKey) // 재개할 작업과 연결
            .withSchedule(CronScheduleBuilder
                .cronSchedule(((CronTrigger) oldTrigger).getCronExpression()) // 기존 Cron 표현식 유지
                .withMisfireHandlingInstructionFireAndProceed()) // 놓친 실행이 있으면 즉시 실행 후 정상 스케줄 진행
            .build();

        // 기존 트리거를 새로운 트리거로 교체
        // rescheduleJob() 메서드는 기존 트리거를 제거하고 새 트리거로 대체
        scheduler.rescheduleJob(triggerKey, newTrigger);

        // 데이터베이스의 작업 상태를 "RUNNING"으로 변경하여 활성 상태로 복원
        SchedulerJob job = jobRepository.findByJobNameAndJobGroup(jobName, jobGroup);
        if (job != null)
        {
            // 작업 상태를 실행 중으로 변경
            job.setStatus("RUNNING");

            // 변경된 상태를 데이터베이스에 저장
            jobRepository.save(job);
        }

        // 작업 재개 완료에 대한 정보 로그 기록
        log.info("Job 재개됨: {} - 그룹: {}", jobName, jobGroup);
    }

    // -----------------------------
    // 7️⃣ 작업 수정 메서드 (Update)
    // 기존 작업의 정보를 변경 (작업명, 그룹, Cron 표현식, 설명 등)
    // -----------------------------
    public SchedulerJob updateJob(SchedulerJob job) throws SchedulerException
    {
        // 수정할 작업 객체의 상세 정보를 로그로 출력 (디버깅 목적)
        log.info(String.valueOf(job));

        // 데이터베이스에서 수정 대상 작업을 ID로 조회
        // Optional 사용하여 null 안전성 보장
        Optional<SchedulerJob> schedulerJobOpt = jobRepository.findById(job.getId());
        if (schedulerJobOpt.isEmpty())
        {
            // 수정 대상 작업이 존재하지 않으면 예외 발생
            throw new SchedulerException("Job 존재하지 않음: " + job.getId());
        }

        // Optional 실제 작업 객체 추출
        SchedulerJob existingJob = schedulerJobOpt.get();

        // 기존 작업의 JobKey 생성 (삭제를 위해 필요)
        JobKey oldJobKey = JobKey.jobKey(existingJob.getJobName(), existingJob.getJobGroup());

        // Quartz Scheduler 기존 작업이 존재하는지 확인 후 삭제
        // 작업 정보가 변경되면 기존 작업을 완전히 제거하고 새로 등록하는 방식 사용
        if (scheduler.checkExists(oldJobKey))
        {
            // 기존 작업과 관련된 모든 트리거도 함께 삭제됨
            scheduler.deleteJob(oldJobKey);
        }

        // 새로운 JobDetail 생성 (수정된 정보로)
        JobDetail newJobDetail = JobBuilder.newJob(DynamicJob.class) // 실제 실행할 작업 클래스
            .withIdentity(job.getJobName(), job.getJobGroup()) // 새로운 작업명과 그룹으로 설정
            .withDescription(job.getDescription()) // 작업 설명 추가
            .usingJobData("description", job.getDescription()) // 작업 데이터에 설명 정보 추가
            .build();

        // 새로운 Trigger 생성 (수정된 Cron 표현식으로)
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());
        CronTrigger newTrigger = TriggerBuilder.newTrigger()
            .withIdentity(job.getJobName() + "Trigger", job.getJobGroup()) // 트리거 식별자도 새 정보로 설정
            .withSchedule(scheduleBuilder) // 새로운 Cron 스케줄 적용
            .build();

        // 새로운 작업과 트리거를 Quartz Scheduler 등록
        scheduler.scheduleJob(newJobDetail, newTrigger);

        // 데이터베이스의 기존 레코드를 새로운 정보로 업데이트
        // 기존 객체의 필드들을 수정된 값으로 변경
        existingJob.setJobName(job.getJobName());             // 작업명 업데이트
        existingJob.setJobGroup(job.getJobGroup());           // 그룹명 업데이트
        existingJob.setCronExpression(job.getCronExpression()); // Cron 표현식 업데이트
        existingJob.setDescription(job.getDescription());     // 설명 업데이트

        // 변경된 정보를 데이터베이스에 저장
        jobRepository.save(existingJob);

        // 업데이트된 작업을 즉시 중단 상태로 변경
        pauseJob(existingJob.getJobName(), existingJob.getJobGroup());

        // 업데이트된 작업 객체를 반환
        return existingJob;
    }
}