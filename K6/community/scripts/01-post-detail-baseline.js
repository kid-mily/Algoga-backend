// K6/community/scripts/01-post-detail-baseline.js
// ------------------------------------------------------------
// 목적:
// - GET /api/v1/posts/{postId} 게시글 단건 조회 성능 측정
// - 댓글 N+1 쿼리 최적화 전 기준선 확보
//
// 관찰할 것:
// - Grafana: http_req_duration p95, http_req_failed
//
// 실행:
//   k6 run .\K6\community\scripts\01-post-detail-baseline.js
// ------------------------------------------------------------

import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, randomSleepSeconds } from '../../common/config.js';

const USER_COUNT = Number(__ENV.USER_COUNT || 20);

export const options = {
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
    scenarios: {
        post_detail_baseline: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '30s', target: USER_COUNT },
                { duration: '1m',  target: USER_COUNT },
                { duration: '30s', target: 0 },
            ],
            gracefulRampDown: '10s',
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.01'],
        // N+1 최적화 전 기준선 — 최적화 후 얼마나 줄어드는지 Grafana에서 비교
        'http_req_duration{type:post-detail}': ['p(95)<2000'],
    },
};

// 테스트 시작 전 실제 존재하는 게시글 ID 수집
export function setup() {
    const res = http.get(`${BASE_URL}/api/v1/posts`);
    const posts = res.json('data.posts');

    if (!posts || posts.length === 0) {
        console.error('게시글 목록이 비어있습니다. DB 데이터를 확인하세요.');
        return { postIds: [1] };
    }

    const postIds = posts.map(p => p.postId);
    console.log(`수집된 게시글 ID: ${postIds.length}개 (${postIds[0]} ~ ${postIds[postIds.length - 1]})`);
    return { postIds };
}

export default function (data) {
    const postIds = data.postIds;
    const postId = postIds[Math.floor(Math.random() * postIds.length)];

    const res = http.get(`${BASE_URL}/api/v1/posts/${postId}`, {
        tags: {
            type: 'post-detail',
            api: 'GET /posts/{postId}',
        },
        headers: {
            'Accept': 'application/json',
        },
    });

    if (Math.random() < 0.01) {
        console.log(`[VU: ${__VU}] postId: ${postId} | status: ${res.status} | duration: ${res.timings.duration}ms`);
    }

    if (res.status !== 200) {
        console.warn(`⚠️ postId: ${postId} | status: ${res.status} | body: ${res.body}`);
    }

    check(res, {
        'post detail: status is 200': (r) => r.status === 200,
    });

    sleep(randomSleepSeconds());
}