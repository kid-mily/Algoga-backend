import http from 'k6/http';
import { check } from 'k6';
import { BASE_URL, USER_COUNT } from '../../common/config.js';

// 토큰 발급 백도어 호출
export function generateTestTokens() {
    const res = http.get(`${BASE_URL}/api/v1/test/generate-tokens?count=${USER_COUNT}`);
    check(res, { 'setup: 토큰 발급 성공': (r) => r.status === 200 });
    return res.json();
}

// 챗봇 질문 API 호출
export function askChatbot(token, question) {
    const payload = JSON.stringify({ question: question });
    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
        },
    };
    const res = http.post(`${BASE_URL}/api/v1/chatbot/ask`, payload, params);

    // 200(정상) 또는 429(Rate Limit 방어) 모두 성공으로 취급
    check(res, { 'ask: status is 200 or 429': (r) => r.status === 200 || r.status === 429 });
    return res;
}