// K6/user/scripts/01-login-load-test.js
// ------------------------------------------------------------
// 목적:
// - POST /api/v1/auth/login 로그인 API 성능 및 CPU 부하 측정
// - Bcrypt 암호화 연산으로 인한 병목 및 응답 지연 기준선 확보
//
// 관찰할 것:
// - Grafana: 시스템 CPU 사용량 급증 여부, http_req_duration p95
//
// 실행 방법:
//   k6 run K6/user/scripts/01-login-load-test.js
//   (유저 수 변경 시: k6 run -e USER_COUNT=100 K6/user/scripts/01-login-load-test.js)
// ------------------------------------------------------------

import http from 'k6/http';
import { check, sleep } from 'k6';
// 팀원들이 세팅해둔 공통 설정 파일 활용
import { BASE_URL, randomSleepSeconds } from '../../common/config.js';
// (선택) 결과 리포트 저장이 필요하다면 주석 해제
// import { createSummaryHandler } from '../../common/summary.js';

// 🌟 터미널에서 유저 수를 마음대로 바꿀 수 있도록 환경변수(__ENV) 처리
const USER_COUNT = Number(__ENV.USER_COUNT || 50);

export const options = {
    // 터미널 결과창에 보여줄 지표들 설정
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],

    // 🌟 고급 부하 시나리오 설정 (팀원분 코드 적용)
    scenarios: {
        login_baseline: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '10s', target: USER_COUNT }, // 10초 동안 서서히 증가
                { duration: '30s', target: USER_COUNT }, // 30초 동안 유지 (이때 CPU 피크!)
                { duration: '10s', target: 0 },          // 10초 동안 쿨다운
            ],
            gracefulRampDown: '10s',
        },
    },

    // 🌟 성공 기준 (그라파나에서 필터링할 수 있도록 type:login 태그 추가)
    thresholds: {
        http_req_failed: ['rate<0.01'], // 에러율 1% 미만이어야 합격
        'http_req_duration{type:login}': ['p(95)<1000'], // 95%의 요청이 1초 이내여야 합격
    },
};

export default function () {
    const url = `${BASE_URL}/api/v1/auth/login`;

    // ⚠️ 질문자님의 실제 계정 정보
    const payload = JSON.stringify({
        username: 'lhy1234lhy',
        password: 'test0302'
    });

    const params = {
        // 🌟 그라파나 대시보드와 완벽하게 연결하기 위한 태그(tags) 설정
        tags: {
            type: 'login',
            api: 'POST /api/v1/auth/login',
        },
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const res = http.post(url, payload, params);

    // 🌟 가끔씩 로그를 찍어줘서 터미널에서 현재 상태를 눈으로 확인 가능하게 함
    if (Math.random() < 0.01) {
        console.log(`[VU: ${__VU}] 로그인 시도 | status: ${res.status} | duration: ${res.timings.duration}ms`);
    }

    // 🌟 에러 발생 시 즉시 경고창을 띄워 원인 파악을 쉽게 함
    if (res.status !== 200) {
        console.warn(`⚠️ 로그인 실패! status: ${res.status} | body: ${res.body}`);
    }

    check(res, {
        'login: status is 200': (r) => r.status === 200,
        'login: has cookie': (r) => r.headers['Set-Cookie'] !== undefined
    });

    // 🌟 팀원분의 randomSleepSeconds()를 사용하여 실제 사람처럼 불규칙하게 대기
    sleep(randomSleepSeconds());
}

// (선택) 결과 리포트 저장이 필요하다면 주석 해제
// export const handleSummary = createSummaryHandler('01-login-load-test');