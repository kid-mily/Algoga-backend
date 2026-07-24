// K6/friend/scripts/02-friend-business-metrics.js
// ------------------------------------------------------------
// 목적:
// - 부하 테스트가 아니라, 통계 대시보드에 보여줄 커스텀 비즈니스 지표(algoga_friend_*)에
//   실제 값이 쌓이도록 트래픽을 발생시키는 용도.
// - algoga_friend_request_total{result=sent|accepted|rejected}
// - algoga_friend_blocked_total
//
// 전제: silver001~silver100 더미 유저(비밀번호 password123)가 이미 DB에 있어야 함.
// personal_code는 DB 삽입 SQL에서 쓴 것과 완전히 동일한 스크램블 공식으로 계산해서 쓴다
// (n * 104729 % 32^6 을 32문자 알파벳으로 base-32 인코딩) — 그래서 DB를 따로 조회할 필요가 없음.
//
// 실행 방법:
//   k6 run K6/friend/scripts/02-friend-business-metrics.js
// ------------------------------------------------------------

import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL } from '../../common/config.js';

const VU_COUNT = Number(__ENV.USER_COUNT || 20);

export const options = {
    scenarios: {
        friend_business_metrics: {
            executor: 'constant-vus',
            vus: VU_COUNT,
            duration: __ENV.DURATION || '2m',
        },
    },
};

const CHARSET = '23456789ABCDEFGHJKLMNPQRSTUVWXYZ';
const POWERS = [33554432, 1048576, 32768, 1024, 32, 1]; // 32^5 ~ 32^0

// DB 더미데이터 생성 SQL과 완전히 동일한 계산식 (personal_code 재현용)
function personalCodeFor(n) {
    const s = (n * 104729) % 1073741824;
    let code = '';
    for (const p of POWERS) {
        const digit = Math.floor(s / p) % 32;
        code += CHARSET[digit];
    }
    return code;
}

function randomUserNumber(excluding) {
    let n;
    do {
        n = 1 + Math.floor(Math.random() * 100);
    } while (n === excluding);
    return n;
}

function loginAndGetCookie(n) {
    const username = `silver${String(n).padStart(3, '0')}`;
    const res = http.post(
        `${BASE_URL}/api/v1/auth/login`,
        JSON.stringify({ username, password: 'password123' }),
        { headers: { 'Content-Type': 'application/json' }, tags: { type: 'friend-login' } }
    );
    if (res.status !== 200) return null;
    return res.headers['Set-Cookie'];
}

function authedParams(cookie, type) {
    return {
        headers: { 'Cookie': cookie, 'Content-Type': 'application/json' },
        tags: { type },
    };
}

export default function () {
    const myNumber = randomUserNumber(-1);
    const cookie = loginAndGetCookie(myNumber);
    if (!cookie) {
        sleep(1);
        return;
    }

    const action = Math.random();

    if (action < 0.4) {
        // 40%: 친구 요청 보내기
        const targetNumber = randomUserNumber(myNumber);
        const targetCode = personalCodeFor(targetNumber);
        const res = http.post(
            `${BASE_URL}/api/v1/friends/requests`,
            JSON.stringify({ targetUserCode: targetCode }),
            authedParams(cookie, 'friend-request-send')
        );
        check(res, { '친구 요청: 처리됨': (r) => r.status === 200 || r.status === 409 });

    } else if (action < 0.7) {
        // 30%: 내가 받은 요청 목록 확인 후 랜덤으로 수락/거절
        const listRes = http.get(`${BASE_URL}/api/v1/friends/requests/received`, authedParams(cookie, 'friend-request-list'));
        if (listRes.status === 200) {
            const body = JSON.parse(listRes.body);
            const requests = body.data || [];
            if (requests.length > 0) {
                const target = requests[Math.floor(Math.random() * requests.length)];
                if (Math.random() < 0.7) {
                    const res = http.patch(
                        `${BASE_URL}/api/v1/friends/requests/${target.relationId}/accept`,
                        null,
                        authedParams(cookie, 'friend-request-accept')
                    );
                    check(res, { '친구 요청 수락: 처리됨': (r) => r.status === 200 });
                } else {
                    const res = http.patch(
                        `${BASE_URL}/api/v1/friends/requests/${target.relationId}/reject`,
                        null,
                        authedParams(cookie, 'friend-request-reject')
                    );
                    check(res, { '친구 요청 거절: 처리됨': (r) => r.status === 200 });
                }
            }
        }

    } else {
        // 30%: 랜덤 유저 차단
        const targetNumber = randomUserNumber(myNumber);
        const targetCode = personalCodeFor(targetNumber);
        const res = http.post(
            `${BASE_URL}/api/v1/friends/blocks`,
            JSON.stringify({ targetUserCode: targetCode }),
            authedParams(cookie, 'friend-block')
        );
        check(res, { '유저 차단: 처리됨': (r) => r.status === 200 || r.status === 403 });
    }

    sleep(1 + Math.random() * 2);
}
