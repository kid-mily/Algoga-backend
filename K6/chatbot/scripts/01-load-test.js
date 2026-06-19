import http from 'k6/http';
import { sleep, check } from 'k6';

const USER_COUNT = __ENV.USER_COUNT || 100;
const BASE_URL = __ENV.BASE_URL || 'http://localhost:15000';

// 🌟 200(정상)과 429(Rate Limit 방어) 모두 정상 응답으로 간주하여 에러율을 왜곡하지 않음
http.setResponseCallback(http.expectedStatuses({ min: 200, max: 200 }, 429));

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
        http_req_failed: ['rate<0.05'],
    }
};

// =====================================================================
// 🌟 1. 초기화 단계 (Setup) - 100명 로그인
// =====================================================================
// =====================================================================
// 🌟 1. 초기화 단계 (Setup) - 100명 로그인
// =====================================================================
export function setup() {
    console.log(`🚀 [초기화] ${USER_COUNT}명의 더미 유저 자동 로그인을 시작합니다...`);

    const users = [];

    for (let i = 1; i <= USER_COUNT; i++) {
        const paddedIndex = i.toString().padStart(2, '0');
        const loginId = `test${paddedIndex}`;

        const payload = JSON.stringify({
            username: loginId,
            password: "password123" // (실제 더미 유저 비밀번호에 맞게 확인)
        });

        const params = { headers: { 'Content-Type': 'application/json' } };
        const res = http.post(`${BASE_URL}/api/v1/auth/login`, payload, params);

        check(res, { [`User ${loginId} 로그인 성공`]: (r) => r.status === 200 });

        if (res.status === 200) {
            // 🚨 수정된 부분: JSON 바디가 아닌 응답의 Cookies 객체에서 토큰을 꺼냅니다.
            let token = null;
            if (res.cookies && res.cookies['accessToken'] && res.cookies['accessToken'].length > 0) {
                token = res.cookies['accessToken'][0].value;
            }

            users.push({ username: loginId, token: token });
        }
    }

    console.log(`✅ [초기화 완료] 총 ${users.length}개의 액세스 토큰 수집 완료`);
    return users;
}


// =====================================================================
// 🌟 2. 메인 실행 단계 (가상 유저 반복 로직)
// =====================================================================
export default function (users) {
    if (!users || users.length === 0) {
        console.error("로그인된 유저가 없어 테스트를 중단합니다.");
        return;
    }

    const userIndex = (__VU - 1) % users.length;
    const myToken = users[userIndex].token;

    // 🌟 핵심 변경: K6의 cookies 옵션 대신, Header에 직접 Cookie 문자열 주입 (100% 인식 보장)
    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Cookie': `accessToken=${myToken}`
        }
    };

    const payload = JSON.stringify({
        question: "결제 취소 및 환불 규정에 대해 자세히 알려주세요."
    });

    const res = http.post(`${BASE_URL}/api/v1/chatbot/ask`, payload, params);

    // 🌟 디버깅 추적 코드: 터미널에서 서버 응답을 실시간으로 확인
    // (콘솔 도배를 막기 위해 5% 확률로만 샘플링 출력)
    if (Math.random() < 0.05) {
        console.log(`[VU: ${__VU}] 챗봇 서버 응답 코드: ${res.status} | 메시지: ${res.body.substring(0, 100)}...`);
    }

    // 만약 200도 아니고 429(도배차단)도 아닌 403, 500 에러 등이 뜨면 무조건 경고 출력
    if (res.status !== 200 && res.status !== 429) {
        console.warn(`🚨 [에러 발생] 상태 코드: ${res.status} | 본문: ${res.body}`);
    }

    check(res, {
        'API 응답이 200(정상) 이거나 429(방어성공) 인가?': (r) => r.status === 200 || r.status === 429,
    });

    sleep(Math.random() * 2 + 1);
}