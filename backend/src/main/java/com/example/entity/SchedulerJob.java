package com.example.entity;

import com.example.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;

/**
 * 스케줄러 Job 정보 엔티티
 * - BaseEntity 상속: createdAt, updatedAt 자동 관리
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class SchedulerJob extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("PK ID")
    private Long id;

    @Comment("스케줄러 Job 이름")
    private String jobName;

    @Comment("스케줄러 Job 그룹")
    private String jobGroup;

    @Comment("스케줄러 설명")
    private String description;

    @Comment("스케줄러 실행 Cron 표현식")
    private String cronExpression;

    @Comment("스케줄러 상태 (RUNNING, PAUSED)")
    private String status;
}
