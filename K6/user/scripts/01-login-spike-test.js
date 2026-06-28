// K6/user/scripts/01-login-spike-test.js
// ------------------------------------------------------------
// 목적:
// - POST /api/v1/auth/login 로그인 API 성능 및 트랜잭션 병목 측정
// - DB 커넥션 풀(HikariCP) 고갈 상황 유도 (500 에러 발생 목표)
//
// 실행 방법:
//   k6 run K6/user/scripts/01-login-spike-test.js
// ------------------------------------------------------------

import http from 'k6/http';
import { check, sleep } from 'k6';
// ⚠️ 경로가 맞는지 확인해 주세요! (팀원 공통 설정 파일)
import { BASE_URL } from '../../common/config.js';

// 🔥 병목을 확실히 보기 위해 200명의 유저가 한 번에 몰리도록 설정
const USER_COUNT = Number(__ENV.USER_COUNT || 200);

export const options = {
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],

    scenarios: {
        login_spike: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '10s', target: USER_COUNT }, // 10초 만에 1000명까지 미친듯이 치솟음 (Spike)
                { duration: '30s', target: USER_COUNT }, // 30초 동안 최대 부하 유지 (여기서 DB 풀 고갈 유도!)
                { duration: '10s', target: 0 },          // 10초 동안 쿨다운
            ],
            gracefulRampDown: '5s',
        },
    },
};

export default function () {
    const url = `${BASE_URL}/api/v1/auth/login`;

    // ⚠️ DB에 존재하는 실제 계정 정보로 변경해주세요. (단, 이 유저가 Lock 걸리지 않도록 주의)
    const payload = JSON.stringify({
        username: 'lhy1234lhy',
        password: 'test0302'
    });

    const params = {
        tags: {
            type: 'login',
            api: 'POST /api/v1/auth/login',
        },
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const res = http.post(url, payload, params);

    // 🚨 500 에러(커넥션 풀 고갈 등) 발생 시 즉시 터미널에 경고창을 띄워 성공(목표 달성)을 알림
    if (res.status === 500) {
        console.warn(`🚨 [목표 달성] 500 ERROR (DB 병목 의심)! status: ${res.status}`);
    } else if (res.status !== 200) {
        console.warn(`⚠️ 기타 에러 발생: status ${res.status}`);
    }

    check(res, {
        'login: status is 200': (r) => r.status === 200,
        // 우리가 기다리던 500 에러 검증 로직
        'login: db connection timeout (500)': (r) => r.status === 500,
    });

    // 짧게 대기하여 서버에 쉴 틈을 주지 않고 지속적인 타격을 줌
    sleep(1);
}