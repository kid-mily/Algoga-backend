// 파일 위치: K6/notice/01-notice-load-test.js
import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:15000';
const USER_COUNT = __ENV.USER_COUNT || 100;

export const options = {
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
    scenarios: {
        notice_load_test: {
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
        // 단순 DB Read API이므로 배너 테스트와 동일하게 엄격한 기준을 적용합니다.
        http_req_failed: ['rate<0.01'],    // 실패율 1% 미만 허용
        http_req_duration: ['p(95)<500'],  // 95%의 요청이 0.5초(500ms) 이내에 응답
    }
};

// =====================================================================
// 🌟 가상 유저(VU) 실행 단계 - 공지사항 조회 (토큰 불필요)
// =====================================================================
export default function () {
    const params = {
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }
    };

    // 테스트할 퍼블릭 공지사항 API 목록
    // (상세 조회 API의 경우 noticeId=1이 없을 경우 404 에러가 나므로 목록 조회 위주로 구성)
    const endpoints = [
        '/api/v1/public/notices/main',  // 메인 페이지 최신 공지 3개 조회
        '/api/v1/public/notices/ALL/1', // 전체 공지사항 1페이지 조회
        '/api/v1/public/notices/tags'   // 공지사항 태그 목록 조회
    ];

    // 위 3개의 API 중 하나를 무작위로 선택하여 다양한 부하 패턴을 생성
    const randomEndpoint = endpoints[Math.floor(Math.random() * endpoints.length)];
    const res = http.get(`${BASE_URL}${randomEndpoint}`, params);

    // 약 1% 확률로 응답 결과를 콘솔에 출력 (디버깅용)
    if (Math.random() < 0.01) {
        console.log(`[VU: ${__VU}] API: ${randomEndpoint} | 상태: ${res.status}`);
    }

    if (res.status !== 200) {
        console.warn(`⚠️ 예외 상태 발생: ${res.status} | API: ${randomEndpoint} | 응답: ${res.body}`);
    }

    // 정상 조회 여부 체크
    check(res, {
        'API 응답이 200(정상)인가?': (r) => r.status === 200,
    });

    // 1~2초 간격으로 랜덤하게 대기 (실제 유저의 페이지 체류 시간 모사)
    sleep(Math.random() * 1 + 1);
}