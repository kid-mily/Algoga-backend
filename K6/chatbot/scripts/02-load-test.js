import http from 'k6/http';
import { sleep, check } from 'k6';

const USER_COUNT = __ENV.USER_COUNT || 100;
const BASE_URL = __ENV.BASE_URL || 'http://localhost:15000';

export const options = {
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
    scenarios: {
        chatbot_load_test: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '20s', target: USER_COUNT },
                { duration: '1m', target: USER_COUNT },
                { duration: '20s', target: 0 },
            ],
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.05'], // 실패율 5% 미만 허용
    }
};

// =====================================================================
// 1. 초기화 단계 (Setup) - 100명 유저 로그인 및 토큰 수집
// =====================================================================
export function setup() {
    console.log(`🚀 [초기화] ${USER_COUNT}명의 더미 유저 로그인을 시작합니다...`);
    const users = [];

    for (let i = 1; i <= USER_COUNT; i++) {
        const paddedIndex = i.toString().padStart(2, '0');
        const loginId = `test${paddedIndex}`;

        const payload = JSON.stringify({
            username: loginId,
            password: "password123"
        });

        const params = { headers: { 'Content-Type': 'application/json' } };
        const res = http.post(`${BASE_URL}/api/v1/auth/login`, payload, params);

        check(res, { [`User ${loginId} 로그인 성공`]: (r) => r.status === 200 });

        if (res.status === 200) {
            // HttpOnly 쿠키(Set-Cookie)에서 accessToken 추출
            let token = null;
            if (res.cookies && res.cookies['accessToken'] && res.cookies['accessToken'].length > 0) {
                token = res.cookies['accessToken'][0].value;
            } else {
                token = res.json('data.accessToken'); // 예비용 폴백
            }
            users.push({ username: loginId, token: token });
        }
    }

    console.log(`✅ [초기화 완료] 총 ${users.length}개의 액세스 토큰 수집 완료`);
    return users;
}

// =====================================================================
// 2. 가상 유저(VU) 실행 단계 - 챗봇 질문 요청
// =====================================================================
export default function (users) {
    if (!users || users.length === 0) {
        console.error("❌ 사용할 수 있는 유저 토큰이 없습니다.");
        return;
    }

    const userIndex = (__VU - 1) % users.length;
    const myToken = users[userIndex].token;

    const params = {
        headers: {
            'Content-Type': 'application/json',
            // 추출한 토큰을 헤더의 Cookie로 삽입
            'Cookie': `accessToken=${myToken}`
        }
    };

    // 🌟 변경된 질문 페이로드 적용
    const payload = JSON.stringify({
        question: "결제가 중복으로 됐어요"
    });

    const res = http.post(`${BASE_URL}/api/v1/chatbot/ask`, payload, params);

    // 약 5% 확률로 응답 결과를 콘솔에 출력 (디버깅용)
    if (Math.random() < 0.05) {
        console.log(`[VU: ${__VU}] 상태: ${res.status} | 응답: ${res.body.substring(0, 100)}...`);
    }

    if (res.status !== 200 && res.status !== 429) {
        console.warn(`⚠️ 예외 상태 발생: ${res.status} | 응답: ${res.body}`);
    }

    check(res, {
        'API 응답 200(정상) 또는 429(Rate Limit 방어) 인가?': (r) => r.status === 200 || r.status === 429,
    });

    // 1~3초 간격으로 랜덤하게 대기 후 다시 요청
    sleep(Math.random() * 2 + 1);
}