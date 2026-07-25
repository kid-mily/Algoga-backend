// K6/chat/scripts/03-chat-messages-baseline.js
// ------------------------------------------------------------
// 목적:
// - GET /api/v1/chat/rooms/{roomId}/messages (채팅방 상세 조회) 성능 측정
// - 메시지별 unreadCount N+1 제거 전/후 p95 비교용 baseline
//
// 전제:
// - ROOM_ID 방은 1:1(DIRECT) 채팅방이며 메시지 약 100건 보유
// - 로그인 계정이 해당 방의 멤버여야 함 (아니면 CHAT_NOT_MEMBER 에러)
//
// 실행:
//   k6 run --env ROOM_ID=5 --env USER_COUNT=50 K6/chat/scripts/03-chat-messages-baseline.js
// ------------------------------------------------------------

import http from 'k6/http';
import { check, sleep, fail } from 'k6';
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.2/index.js';
import { BASE_URL } from '../../common/config.js';

const ROOM_ID = Number(__ENV.ROOM_ID || 5);
const USER_COUNT = Number(__ENV.USER_COUNT || 50);
const LOGIN_USERNAME = __ENV.LOGIN_USERNAME || 'baek';
const LOGIN_PASSWORD = __ENV.LOGIN_PASSWORD || 'baek1234';

export const options = {
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
    scenarios: {
        warmup: {
            executor: 'constant-vus',
            vus: USER_COUNT,
            duration: '1m',
            gracefulStop: '0s',
            tags: { phase: 'warmup' },
        },
        measured: {
            executor: 'constant-vus',
            vus: USER_COUNT,
            duration: '2m',
            startTime: '1m',
            tags: { phase: 'measured' },
        },
    },
    thresholds: {
        'http_req_failed{phase:measured}': ['rate<0.01'],
        // baseline 기록용 — 최적화 후 이 수치를 낮춰서 개선폭을 확인
        'http_req_duration{phase:measured}': ['p(95)<3000'],
    },
};

export function setup() {
    const loginRes = http.post(
        `${BASE_URL}/api/v1/auth/login`,
        JSON.stringify({ username: LOGIN_USERNAME, password: LOGIN_PASSWORD }),
        { headers: { 'Content-Type': 'application/json' } }
    );

    const loginOk = check(loginRes, {
        'login: status is 200': (r) => r.status === 200,
        'login: has accessToken cookie': (r) => r.cookies.accessToken && r.cookies.accessToken.length > 0,
    });
    if (!loginOk) {
        fail(`로그인 실패. status=${loginRes.status}, body=${loginRes.body}`);
    }

    const cookieHeader = `accessToken=${loginRes.cookies.accessToken[0].value}`;

    // 사전 검증: 방 접근 가능 여부 + 메시지 건수 확인 (측정 조건이 맞는지 로그로 남김)
    const probe = http.get(`${BASE_URL}/api/v1/chat/rooms/${ROOM_ID}/messages`, {
        headers: { Cookie: cookieHeader, Accept: 'application/json' },
    });
    if (probe.status !== 200) {
        fail(`방 조회 실패. roomId=${ROOM_ID}, status=${probe.status}, body=${probe.body}`);
    }
    const messageCount = (probe.json().data || []).length;
    console.log(`로그인 성공 | roomId=${ROOM_ID} | 메시지 ${messageCount}건 | VU=${USER_COUNT}`);

    return { cookieHeader, messageCount };
}

export default function (data) {
    const res = http.get(`${BASE_URL}/api/v1/chat/rooms/${ROOM_ID}/messages`, {
        headers: { Cookie: data.cookieHeader, Accept: 'application/json' },
        tags: { type: 'chat-messages', api: 'GET /chat/rooms/{roomId}/messages' },
    });

    if (res.status !== 200) {
        console.warn(`⚠️ status: ${res.status} | body: ${res.body}`);
    }

    check(res, {
        'chat messages: status is 200': (r) => r.status === 200,
    });

    sleep(1 + Math.random());
}

export function handleSummary(data) {
    return {
        stdout: textSummary(data, { indent: ' ', enableColors: true }),
        'K6/chat/results/03-chat-messages-baseline-summary.json': JSON.stringify(data, null, 2),
        'K6/chat/results/03-chat-messages-baseline-summary.md':
            '# Load Test Report: 03-chat-messages-baseline (최적화 전)\n\n' +
            textSummary(data, { indent: ' ', enableColors: false }),
    };
}