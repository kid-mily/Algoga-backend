import http from 'k6/http';
import { check, sleep } from 'k6';

// 🌟 요청하신 대로 동시에 공격하는 가상 유저 수를 100명으로 늘렸습니다! (부하 2배)
const USER_COUNT = Number(__ENV.USER_COUNT || 100);

export const options = {
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
    scenarios: {
        friend_list_baseline: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '10s', target: USER_COUNT }, // 100명까지 10초간 증가
                { duration: '30s', target: USER_COUNT }, // 100명 유지 (서버 폭발 구간!)
                { duration: '10s', target: 0 },
            ],
            gracefulRampDown: '10s',
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.01'],
        'http_req_duration{type:friend-list}': ['p(95)<1000'],
    },
};

export function setup() {
    const loginRes = http.post('http://localhost:15000/api/v1/auth/login', JSON.stringify({
        username: 'lhy1234lhy',
        password: 'test0302'
    }), { headers: { 'Content-Type': 'application/json' } });

    // HttpOnly 쿠키 추출
    const cookie = loginRes.headers['Set-Cookie'];
    return { authCookie: cookie };
}

export default function (data) {
    const url = 'http://localhost:15000/api/v1/friends';
    const params = {
        tags: {
            type: 'friend-list',
            api: 'GET /api/v1/friends',
        },
        headers: {
            // 추출한 쿠키를 담아 인증 통과
            'Cookie': data.authCookie,
            'Content-Type': 'application/json',
        },
    };

    const res = http.get(url, params);

    // 100명이나 되므로 로그가 너무 많이 찍히지 않게 확률을 0.005로 낮췄습니다.
    if (Math.random() < 0.005) {
        console.log(`[VU: ${__VU}] 친구 목록 조회 | status: ${res.status} | duration: ${res.timings.duration}ms`);
    }
    if (res.status !== 200) {
        console.warn(`⚠️ 친구 조회 실패! status: ${res.status} | body: ${res.body}`);
    }

    check(res, {
        'is status 200': (r) => r.status === 200,
    });

    sleep(1);
}