import http from 'k6/http';
import { sleep, check } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:15000';

export const options = {
    // 100명의 가상 유저(VUser)가 1만 건의 배열 연산을 발생시키면 CPU 부하가 엄청나므로
    // 타겟(target)을 너무 높게 잡지 않고 100명 정도로 유지합니다.
    stages: [
        { duration: '30s', target: 50 },  // 점진적 부하 증가 (예열)
        { duration: '1m', target: 100 },  // 100명 동시 타격 유지 (병목 관찰 구간)
        { duration: '30s', target: 0 },   // 트래픽 감소
    ],
    thresholds: {
        // MySQL은 1만 번의 코사인 연산 부하를 받으므로 응답 시간이 엄청나게 튈 것을 대비해 임계치를 넉넉히 잡습니다.
        'http_req_duration{scenario:mysql}': ['p(95)<15000'],
        'http_req_duration{scenario:vector}': ['p(95)<5000'],
    }
};

export function setup() {
    return { token: "load-test-token" };
}

export default function () {
    const params = { headers: { 'Content-Type': 'application/json' } };

    // 🌟 50:50 확률로 트래픽을 분산하여 실시간 공정 비교 (Apples-to-Apples)
    const isRedisTest = Math.random() < 0.5;

    let url = '';
    // 양쪽 모두 실제 임베딩 모델(Ollama)을 타기 때문에 질문은 동일하게 세팅합니다.
    let payloadStr = JSON.stringify({ question: "환불 규정이 어떻게 되나요?" });
    let metricTag = '';

    if (isRedisTest) {
        // [실험군] Redis Native Vector Search (O(log N))
        url = `${BASE_URL}/api/v1/load-test/chatbot/redis-vector-search`;
        metricTag = 'vector';
    } else {
        // [대조군] MySQL 전수조사 10,000건 시뮬레이션 (O(N))
        url = `${BASE_URL}/api/v1/load-test/chatbot/mysql-vector-calc`;
        metricTag = 'mysql';
    }

    // k6에서 쏘는 요청에 metricTag를 달아주어야 그라파나의 {scenario="mysql" | "vector"} 필터에 정확히 꽂힙니다.
    const res = http.post(url, payloadStr, Object.assign({}, params, { tags: { scenario: metricTag } }));

    check(res, {
        [`[${metricTag}] 상태코드 200`]: (r) => r.status === 200,
    });

    // 실사용자처럼 1초 대기 (Think Time)
    sleep(1);
}