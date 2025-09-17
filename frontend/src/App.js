// React 라이브러리 import
import React from "react";

// React Router DOM 라우터 관련 컴포넌트 import
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";

// SchedulerPage 컴포넌트 import (스케줄러 관리 페이지)
import SchedulerPage from "./pages/SchedulerPage";

// 앱 전역 스타일 import
import './App.css';

// ------------------------------
// App 컴포넌트 정의
// ------------------------------
function App()
{
    return (
        // BrowserRouter 사용: SPA URL 기반 라우팅 처리
        <Router>
            {/* Routes 컴포넌트 안에 여러 Route 정의 */}
            <Routes>
                {/* "/" 경로로 접속하면 SchedulerPage 컴포넌트를 렌더링 */}
                <Route path="/" element={<SchedulerPage />} />
            </Routes>
        </Router>
    );
}

// App 컴포넌트를 외부에서 사용할 수 있도록 export
export default App;
