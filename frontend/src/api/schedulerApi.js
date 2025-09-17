// HTTP 요청을 보내기 위해 사용
import axios from "axios";

// 스케줄러 관련 API 기본 URL 정의
const API_BASE = "http://localhost:8081/api/schedulers";

// ------------------------------
// 1️⃣ 모든 스케줄러 잡(Job) 조회
// ------------------------------
export const getAllJobs = () =>
    axios.get(API_BASE,
        {
        // 쿠키 기반 인증 등을 위해 요청 시 credentials 포함
        withCredentials: true
    });

// ------------------------------
// 2️⃣ 새로운 잡(Job) 생성
// ------------------------------
export const createJob = (job) =>
    // job 객체를 POST 방식으로 API 전송하여 새로운 잡을 생성
    axios.post(API_BASE, job);

// ------------------------------
// 3️⃣ 특정 잡(Job) 즉시 실행
// ------------------------------
export const runJobNow = (jobName, jobGroup) =>
    axios.post(
        `${API_BASE}/run`, // 실행 API 경로
        null, // POST 요청 본문이 필요 없으므로 null
        {
            params: { // 쿼리 파라미터로 잡 이름과 그룹 전달
                jobName,
                jobGroup
            }
        }
    );

// ------------------------------
// 4️⃣ 특정 잡(Job) 삭제
// ------------------------------
export const deleteJob = (jobName, jobGroup) =>
    axios.delete(API_BASE,
        {
        params: { // 삭제할 잡의 이름과 그룹을 쿼리 파라미터로 전달
            jobName,
            jobGroup
        }
    });

// ------------------------------
// 5️⃣ 기존 잡(Job) 수정
// ------------------------------
export const updateJob = (job) =>
    // job 객체를 PUT 방식으로 전송하여 기존 잡 정보 업데이트
    axios.put(`${API_BASE}/update`, job);

// ------------------------------
// 6️⃣ 특정 잡(Job) 일시정지
// ------------------------------
export const pauseJob = (jobName, jobGroup) =>
    axios.post(
        `${API_BASE}/pause`, // 일시정지 API 경로
        null, // POST 본문 없음
        {
            params: { // 일시정지할 잡의 이름과 그룹
                jobName,
                jobGroup
            }
        }
    );

// ------------------------------
// 7️⃣ 특정 잡(Job) 재개
// ------------------------------
export const resumeJob = (jobName, jobGroup) =>
    axios.post(
        `${API_BASE}/resume`, // 재개 API 경로
        null, // POST 본문 없음
        {
            params: { // 재개할 잡의 이름과 그룹
                jobName,
                jobGroup
            }
        }
    );
