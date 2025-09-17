package com.example.controller;

import com.example.entity.SchedulerJob;
import com.example.service.SchedulerService;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 스케줄러 REST API
 * - CRUD + 실행, 중단, 재개
 */
@RestController
@RequestMapping("/api/schedulers")
@RequiredArgsConstructor
public class SchedulerController
{
    private final SchedulerService schedulerService;

    // -----------------------------
    // 1️⃣ 스케줄러 등록
    // -----------------------------
    @PostMapping
    public ResponseEntity<?> createJob(@RequestBody SchedulerJob job) throws SchedulerException
    {
        try
        {
            SchedulerJob savedJob = schedulerService.createJob(job);
            return ResponseEntity.ok(savedJob);
        }
        catch (IllegalArgumentException e)
        {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // -----------------------------
    // 2️⃣ 모든 스케줄러 조회
    // -----------------------------
    @GetMapping
    public ResponseEntity<List<SchedulerJob>> getAllJobs()
    {
        return ResponseEntity.ok(schedulerService.getAllJobs());
    }

    // -----------------------------
    // 3️⃣ 수동 실행 (Run Now)
    // -----------------------------
    @PostMapping("/run")
    public ResponseEntity<String> runJobNow(@RequestParam String jobName, @RequestParam String jobGroup) throws SchedulerException
    {
        schedulerService.runJobNow(jobName, jobGroup);
        return ResponseEntity.ok("Job 실행됨: " + jobName);
    }

    // -----------------------------
    // 4️⃣ 삭제
    // -----------------------------
    @DeleteMapping
    public ResponseEntity<String> deleteJob(@RequestParam String jobName, @RequestParam String jobGroup) throws SchedulerException
    {
        schedulerService.deleteJob(jobName, jobGroup);
        return ResponseEntity.ok("Job 삭제됨: " + jobName);
    }

    // -----------------------------
    // 5️⃣ 중단(Pause)
    // -----------------------------
    @PostMapping("/pause")
    public ResponseEntity<String> pauseJob(@RequestParam String jobName, @RequestParam String jobGroup) throws SchedulerException
    {
        schedulerService.pauseJob(jobName, jobGroup);
        return ResponseEntity.ok("Job 중단됨: " + jobName);
    }

    // -----------------------------
    // 6️⃣ 재개(Resume)
    // -----------------------------
    @PostMapping("/resume")
    public ResponseEntity<String> resumeJob(@RequestParam String jobName, @RequestParam String jobGroup) throws SchedulerException
    {
        schedulerService.resumeJob(jobName, jobGroup);
        return ResponseEntity.ok("Job 재개됨: " + jobName);
    }

    // -----------------------------
    // 7️⃣ 수정(Update)
    // -----------------------------
    @PutMapping("update")
    public ResponseEntity<SchedulerJob> updateJob(@RequestBody SchedulerJob job) throws SchedulerException
    {
        SchedulerJob updatedJob = schedulerService.updateJob(job);
        return ResponseEntity.ok(updatedJob);
    }
}
