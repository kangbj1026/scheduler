package com.example.repository;

import com.example.entity.SchedulerJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * SchedulerJob JPA Repository
 */
@Repository
public interface SchedulerJobRepository extends JpaRepository<SchedulerJob, Long>
{
    // 나중에 이름 + 그룹으로 조회 가능
    SchedulerJob findByJobNameAndJobGroup(String jobName, String jobGroup);
}
