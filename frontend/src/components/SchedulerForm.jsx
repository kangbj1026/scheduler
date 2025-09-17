// React 라이브러리에서 필요한 훅(useState, useEffect) 불러오기
import React, { useState, useEffect } from "react";

// SchedulerForm 컴포넌트 정의
// props:
// - onSubmit: 폼 제출 시 호출되는 함수
// - initialValues: 기존 잡 정보를 편집할 때 초기값으로 사용
const SchedulerForm = ({ onSubmit, initialValues }) =>
{
    // 상태 선언
    const [id, setId] = useState(null);  // 잡의 고유 ID (편집 시 필요)
    const [jobName, setJobName] = useState(""); // 잡 이름
    const [jobGroup, setJobGroup] = useState(""); // 잡 그룹
    const [cronExpression, setCronExpression] = useState(""); // 크론 표현식
    const [description, setDescription] = useState(""); // 잡 설명

    // 초기값이 바뀔 때마다 상태를 업데이트
    useEffect(() =>
    {
        setId(initialValues?.id || null); // initialValues.id가 있으면 설정, 없으면 null
        setJobName(initialValues?.jobName || ""); // 초기 잡 이름
        setJobGroup(initialValues?.jobGroup || "DEFAULT"); // 초기 잡 그룹, 기본값 DEFAULT
        setCronExpression(initialValues?.cronExpression || "0/1 * * * * ?"); // 초기 크론, 기본 1초마다 실행
        setDescription(initialValues?.description || ""); // 초기 설명
    }, [initialValues]); // initialValues 변경될 때마다 실행

    // 폼 제출 핸들러
    const handleSubmit = (e) =>
    {
        e.preventDefault(); // 페이지 리로드 방지
        // 부모 컴포넌트로 현재 상태 전달
        onSubmit({ id, jobName, jobGroup, description, cronExpression });
    };

    // 렌더링되는 폼 JSX
    return (
        <form onSubmit={handleSubmit} style={{ marginBottom: "1rem" }}>
            {/* 잡 이름 입력 */}
            <input
                placeholder="Job Name"
                value={jobName}
                onChange={e => setJobName(e.target.value)}
            />
            {/* 잡 그룹 입력 */}
            <input
                placeholder="Job Group"
                value={jobGroup}
                onChange={e => setJobGroup(e.target.value)}
            />
            {/* 설명 입력 */}
            <input
                placeholder="Description"
                value={description}
                onChange={e => setDescription(e.target.value)}
            />
            {/* 크론 표현식 입력 */}
            <input
                placeholder="Cron"
                value={cronExpression}
                onChange={e => setCronExpression(e.target.value)}
            />
            {/* 제출 버튼: initialValues 있으면 "수정", 없으면 "등록" */}
            <button type="submit">
                {initialValues ? "수정" : "등록"}
            </button>
        </form>
    );
};

// 컴포넌트 외부에서 사용할 수 있도록 export
export default SchedulerForm;
