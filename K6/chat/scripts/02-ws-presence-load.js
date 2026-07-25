// K6/chat/scripts/02-ws-presence-load.js
// ------------------------------------------------------------
// 목적:
// - /ws/chat STOMP WebSocket 에 N개의 동시 연결을 열어
//   "동시 접속 수(Gauge)" 와 "연결/해제 rate(Counter)" 패널을 실제로 채워 검증
// - 게이지 상승 → 유지 → 하강 곡선이 Grafana 에 그려지는지 확인
//
// 계측 대상 서버 메트릭:
//   algoga_friend_online_users              (Gauge)   ← CONNECT/DISCONNECT 로 증감
//   algoga_chat_ws_connection_total{event}  (Counter) ← rate() 로 churn 관찰
//
// 실행 (monitoring 스택 + 서버가 로컬에 떠 있어야 함):
//   k6 run --env VUS=50 --env HOLD=20 ..\K6\chat\scripts\02-ws-presence-load.js
// ------------------------------------------------------------

import ws from 'k6/ws';
import http from 'k6/http';
import { check, fail } from 'k6';
import { BASE_URL } from '../../common/config.js';

const VUS = Number(__ENV.VUS || 50);       // 목표 동시 연결 수
const HOLD = Number(__ENV.HOLD || 20);     // 각 연결 유지 시간(초). 짧을수록 churn(rate)이 잦아짐

// http(s):// → ws(s):// 로 스킴만 변환
const WS_URL = `${BASE_URL.replace(/^http/, 'ws')}/ws/chat`;

// STOMP 프레임 (프레임 끝은 NULL 문자)
const NULL = '\u0000';
const STOMP_CONNECT =
    'CONNECT\n' +
    'accept-version:1.2\n' +
    'heart-beat:0,0\n' +   // 하트비트 비활성 — 테스트 단순화
    '\n' +
    NULL;
const STOMP_DISCONNECT =
    'DISCONNECT\n' +
    '\n' +
    NULL;

export const options = {
    // 게이지가 오르고(연결) → 평평하게 유지 → 내려가는(해제) 곡선을 만들기 위한 단계 구성
    scenarios: {
        presence: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '30s', target: VUS },  // 상승: 게이지 ↑, "연결/s" 스파이크
                { duration: '1m', target: VUS },   // 유지: 게이지 ~VUS 에서 평평
                { duration: '30s', target: 0 },    // 하강: 게이지 ↓, "해제/s" 스파이크
            ],
            gracefulStop: '5s',
        },
    },
    thresholds: {
        ws_connecting: ['p(95)<1000'],      // 핸드셰이크 지연
        ws_session_duration: ['p(90)>0'],   // 세션이 실제로 유지됐는지
    },
};

export function setup() {
    const loginRes = http.post(
        `${BASE_URL}/api/v1/auth/login`,
        JSON.stringify({ username: 'baek', password: 'baek1234' }),
        { headers: { 'Content-Type': 'application/json' } }
    );

    check(loginRes, { 'login: status 200': (r) => r.status === 200 })
    || fail(`로그인 실패. status=${loginRes.status}, body=${loginRes.body}`);

    // HttpOnly 여도 k6 는 Set-Cookie 를 파싱해줌
    const accessToken = loginRes.cookies.accessToken
        && loginRes.cookies.accessToken[0]
        && loginRes.cookies.accessToken[0].value;

    if (!accessToken) {
        fail('accessToken 쿠키를 응답에서 찾지 못함');
    }
    console.log('로그인 성공, accessToken 확보');
    return { accessToken };
}

export default function (data) {
    const params = {
        headers: {
            // WS 핸드셰이크에서 JwtHandshakeInterceptor 가 읽는 쿠키
            'Cookie': `accessToken=${data.accessToken}`,
            // StompWebSocketConfig 의 allowedOriginPatterns 통과용 (없으면 403)
            'Origin': 'http://localhost:17000',
        },
    };

    const res = ws.connect(WS_URL, params, function (socket) {
        socket.on('open', function () {
            // 이 프레임이 SessionConnectEvent → 게이지 +1, chatWsConnectTotal +1 을 유발
            socket.send(STOMP_CONNECT);
        });

        // CONNECTED / ERROR 프레임 확인용 (디버깅)
        socket.on('message', function (msg) {
            if (msg.indexOf('ERROR') === 0) {
                console.warn(`STOMP ERROR 프레임 수신: ${msg}`);
            }
        });

        socket.on('error', function (e) {
            console.warn(`WS 에러: ${e && e.error ? e.error() : e}`);
        });

        // HOLD 초 유지 후 정상 종료 → SessionDisconnectEvent → 게이지 -1, chatWsDisconnectTotal +1
        socket.setTimeout(function () {
            socket.send(STOMP_DISCONNECT);
            socket.close();
        }, HOLD * 1000);
    });

    check(res, { 'ws: 핸드셰이크 101': (r) => r && r.status === 101 });
}