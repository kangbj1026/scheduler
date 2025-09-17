// React 필요한 훅 import
import React, { useEffect, useState } from "react";

// SchedulerForm 컴포넌트 import (잡 등록/수정 폼)
import SchedulerForm from "../components/SchedulerForm";

// API 함수 import (Axios 백엔드 스케줄러 API 호출)
import { getAllJobs, createJob, runJobNow, deleteJob, pauseJob, resumeJob, updateJob } from "../api/schedulerApi";

// 스케줄러 관리 페이지 컴포넌트
const SchedulerPage = () =>
{
    // 상태: 모든 잡(Job) 목록
    const [jobs, setJobs] = useState([]);

    // API 호출로 잡 목록 가져오기
    const fetchJobs = async () =>
    {
        const res = await getAllJobs();   // GET /api/schedulers
        setJobs(res.data);                // 가져온 데이터 상태에 저장
    };

    // 컴포넌트 마운트 시 잡 목록 초기 로딩
    useEffect(() =>
    {
        fetchJobs().then(r => console.log("r = " , r));
    }, []); // 빈 배열: 한 번만 실행

    // ------------------------------
    // 잡 생성 핸들러
    // ------------------------------
    const handleCreate = async (job) =>
    {
        try {
            const res = await createJob(job);  // POST /api/schedulers
            if (res.status === 200) {
                await fetchJobs();            // 목록 갱신
            }
        } catch (err) {
            // 에러 처리: 백엔드에서 400 등의 에러 메시지 전달 가능
            alert(err.response?.data || "Job 생성 실패");
        }
    };

    // ------------------------------
    // 편집 중인 잡 상태
    // ------------------------------
    const [editingJob, setEditingJob] = useState(null);

    // ------------------------------
    // 잡 액션 핸들러: 실행/삭제/수정/중단/재개
    // ------------------------------
    const handleAction = async (action, job) =>
    {
        switch(action) {
            case "run": await runJobNow(job.jobName, job.jobGroup); break;   // 즉시 실행
            case "delete": await deleteJob(job.jobName, job.jobGroup); break; // 삭제
            case "update": await updateJob(job); break;                       // 수정
            case "pause": await pauseJob(job.jobName, job.jobGroup); break;   // 중단
            case "resume": await resumeJob(job.jobName, job.jobGroup); break; // 재개
            default: break;
        }
        await fetchJobs(); // 액션 후 최신 목록 갱신
    };

    // ------------------------------
    // 잡 수정 핸들러
    // ------------------------------
    const handleUpdate = async (job) =>
    {
        await updateJob(job);      // API 호출로 잡 업데이트
        setEditingJob(null);       // 수정 모드 종료
        await fetchJobs();         // 최신 목록 갱신
    };

    // ------------------------------
    // 렌더링
    // ------------------------------
    return (
        <div>
            <h1>스케줄러 관리</h1>

            {/* 잡 등록/수정 폼 */}
            <SchedulerForm
                onSubmit={editingJob ? handleUpdate : handleCreate} // 편집 중이면 update, 아니면 create
                initialValues={editingJob}                           // 편집 대상 초기값 전달
            />

            {/* 잡 목록 테이블 */}
            <table border="1">
                <thead>
                <tr>
                    <th>Job Name</th>
                    <th>Job Group</th>
                    <th>Description</th>
                    <th>Cron</th>
                    <th>Status</th>
                    <th>Action</th>
                </tr>
                </thead>
                <tbody>
                {jobs.map(job => (
                    <tr key={job.id}>
                        <td>{job.jobName}</td>
                        <td>{job.jobGroup}</td>
                        <td>{job.description}</td>
                        <td>{job.cronExpression}</td>
                        <td>{job.status}</td>
                        <td>
                            {/* 실행 버튼 */}
                            <button onClick={() => handleAction("run", job)}>실행</button>

                            {/* 중단/재개 버튼: 현재 상태에 따라 조건부 렌더링 */}
                            {job.status === "RUNNING" ?
                                <button onClick={() => handleAction("pause", job)}>중단</button> :
                                <button onClick={() => handleAction("resume", job)}>재개</button>
                            }

                            {/* 수정 버튼: 클릭 시 편집 모드로 전환 */}
                            <button onClick={() => setEditingJob(job)}>수정</button>

                            {/* 삭제 버튼 */}
                            <button onClick={() => handleAction("delete", job)}>삭제</button>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
};

// 외부에서 사용할 수 있도록 export
export default SchedulerPage;
