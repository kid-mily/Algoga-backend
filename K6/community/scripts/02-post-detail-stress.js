// K6/community/scripts/02-post-detail-stress.js
// ------------------------------------------------------------
// 목적:
// - GET /api/v1/posts/{postId} 한계(Stress) 테스트
// - VU를 계단식으로 올려 서버가 꺾이는 지점(에러율↑, p95 급증) 탐색
//
// 관찰할 것:
// - Grafana: 에러율, DB 커넥션 Pending, p95
//   → Pending/CPU 치솟으면 서버 한계, 멀쩡한데 응답만 느리면 k6(클라) 한계
//
// 실행:
//   k6 run ..\K6\community\scripts\02-post-detail-stress.js
// ------------------------------------------------------------

import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, randomSleepSeconds } from '../../common/config.js';

export const options = {
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
    scenarios: {
        post_detail_stress: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '30s', target: 200 },
                { duration: '30s', target: 400 },
                { duration: '30s', target: 600 },
                { duration: '30s', target: 800 },
                { duration: '30s', target: 1000 },
                { duration: '30s', target: 0 },
            ],
            gracefulRampDown: '10s',
        },
    },
    // 한계 테스트는 threshold로 막지 않고 끝까지 관찰 (abortOnFail 안 씀)
    thresholds: {
        http_req_failed: ['rate<0.50'],   // 50% 넘으면 사실상 붕괴, 참고용
        'http_req_duration{type:post-detail}': ['p(95)<5000'],
    },
};

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
        headers: { 'Accept': 'application/json' },
    });

    if (res.status !== 200) {
        console.warn(`⚠️ postId: ${postId} | status: ${res.status}`);
    }

    check(res, {
        'post detail: status is 200': (r) => r.status === 200,
    });

    sleep(randomSleepSeconds());
}