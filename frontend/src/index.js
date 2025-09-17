// React 라이브러리 import
import React from "react";

// ReactDOM 라이브러리 import (React 앱을 실제 DOM 렌더링할 때 사용)
import ReactDOM from "react-dom/client";

// 최상위 App 컴포넌트 import
import App from "./App";

// 전역 스타일 import
import './index.css';

// ------------------------------
// React 18 이상에서 root 생성
// ------------------------------
// HTML id="root"인 DOM 요소를 가져와 React root 지정
const root = ReactDOM.createRoot(document.getElementById("root"));

// ------------------------------
// React 컴포넌트를 DOM 렌더링
// ------------------------------
// App 컴포넌트를 root DOM 요소에 렌더링
root.render(<App />);
