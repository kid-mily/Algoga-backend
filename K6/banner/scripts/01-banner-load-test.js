// 파일 위치: K6/banner/01-banner-load-test.js
import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:15000';
const USER_COUNT = __ENV.USER_COUNT || 100;

export const options = {
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
    scenarios: {
        banner_load_test: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '20s', target: USER_COUNT }, // 20초 동안 100명까지 증가
                { duration: '1m', target: USER_COUNT },  // 1분 동안 100명 유지
                { duration: '20s', target: 0 },          // 20초 동안 0명으로 감소
            ],
        },
    },
    thresholds: {
        // 일반 조회 API이므로 챗봇(LLM)보다 훨씬 엄격한 기준을 적용합니다.
        http_req_failed: ['rate<0.01'],    // 실패율 1% 미만 허용
        http_req_duration: ['p(95)<500'],  // 95%의 요청이 0.5초(500ms) 이내에 응답해야 함
    }
};

// =====================================================================
// 🌟 가상 유저(VU) 실행 단계 - 배너 단순 조회 (토큰 불필요)
// =====================================================================
export default function () {
    const params = {
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }
    };

    // 일반 사용자용 배너 조회 API 호출
    const res = http.get(`${BASE_URL}/api/v1/banner`, params);

    // 약 1% 확률로 응답 결과를 콘솔에 출력 (디버깅용)
    if (Math.random() < 0.01) {
        console.log(`[VU: ${__VU}] 상태: ${res.status} | 응답: ${res.body.substring(0, 100)}...`);
    }

    if (res.status !== 200) {
        console.warn(`⚠️ 예외 상태 발생: ${res.status} | 응답: ${res.body}`);
    }

    // 정상 조회 여부 체크
    check(res, {
        'API 응답이 200(정상)인가?': (r) => r.status === 200,
    });

    // 1~2초 간격으로 랜덤하게 대기 (실제 유저의 페이지 체류/새로고침 시간 모사)
    sleep(Math.random() * 1 + 1);
}