// K6/chat/scripts/01-chat-rooms-baseline.js
// ------------------------------------------------------------
// 목적:
// - GET /api/v1/chat/rooms 채팅방 목록 조회 성능 측정
// - 캐시 적용 전/후 p95 비교
//
// 실행:
//   k6 run --env USER_COUNT=100 ..\K6\chat\scripts\01-chat-rooms-baseline.js
// ------------------------------------------------------------

import http from 'k6/http';
import { check, sleep, fail } from 'k6';
import { BASE_URL } from '../../common/config.js';

const USER_COUNT = Number(__ENV.USER_COUNT || 100);

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
        'http_req_duration{phase:measured}': ['p(95)<2000'],
    },
};

export function setup() {
    const loginRes = http.post(
        `${BASE_URL}/api/v1/auth/login`,
        JSON.stringify({ username: 'baek', password: 'baek1234' }),
        { headers: { 'Content-Type': 'application/json' } }
    );

    const loginOk = check(loginRes, {
        'login: status is 200': (r) => r.status === 200,
    });

    if (!loginOk) {
        fail(`로그인 실패. status=${loginRes.status}, body=${loginRes.body}`);
    }

    const cookie = loginRes.headers['Set-Cookie'];
    console.log('로그인 성공, 쿠키 획득');
    return { authCookie: cookie };
}

export default function (data) {
    const res = http.get(`${BASE_URL}/api/v1/chat/rooms`, {
        tags: { type: 'chat-rooms', api: 'GET /chat/rooms' },
        headers: {
            'Cookie': data.authCookie,
            'Accept': 'application/json',
        },
    });

    if (Math.random() < 0.01) {
        console.log(`[VU: ${__VU}] status: ${res.status} | duration: ${res.timings.duration}ms`);
    }

    if (res.status !== 200) {
        console.warn(`⚠️ status: ${res.status} | body: ${res.body}`);
    }

    check(res, {
        'chat rooms: status is 200': (r) => r.status === 200,
    });

    sleep(1 + Math.random());
}