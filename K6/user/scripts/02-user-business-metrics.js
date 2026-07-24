// K6/user/scripts/02-user-business-metrics.js
// ------------------------------------------------------------
// 목적:
// - 부하 테스트가 아니라, 통계 대시보드에 보여줄 커스텀 비즈니스 지표(algoga_user_*)에
//   실제 값이 쌓이도록 트래픽을 발생시키는 용도.
// - algoga_user_login_total{result=success|failed} : silver001~silver100 더미 계정으로
//   로그인 성공/실패를 섞어서 호출
// - algoga_user_signup_total{socialType=GOOGLE|KAKAO} : 소셜 추가정보 가입은 이메일 인증이
//   필요 없어서(코드 확인 결과 socialSignup()에는 이메일 인증 체크가 없음) k6로 그대로 호출 가능
//
// ⚠️ 자동화 못 하는 지표:
// - algoga_user_signup_total{socialType=LOCAL} : 일반 회원가입은 이메일로 발송되는 인증번호를
//   실제로 받아서 입력해야 하는데, k6는 이메일함을 읽을 수 없어서 자동화가 안 됩니다.
// - algoga_user_withdraw_total : 탈퇴도 마이페이지 이메일 인증이 선행되어야 해서 마찬가지로
//   자동화가 안 됩니다.
//   → 이 두 개는 데모 전에 Swagger나 실제 화면에서 몇 번 수동으로 눌러서 값을 만들어두세요.
//
// 실행 방법:
//   k6 run K6/user/scripts/02-user-business-metrics.js
// ------------------------------------------------------------

import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL } from '../../common/config.js';

const VU_COUNT = Number(__ENV.USER_COUNT || 20);

export const options = {
    scenarios: {
        user_business_metrics: {
            executor: 'constant-vus',
            vus: VU_COUNT,
            duration: __ENV.DURATION || '2m',
        },
    },
};

function randomDummyUserNumber() {
    return 1 + Math.floor(Math.random() * 100); // silver001 ~ silver100
}

function loginAttempt(username, password) {
    const url = `${BASE_URL}/api/v1/auth/login`;
    const payload = JSON.stringify({ username, password });
    const params = {
        tags: { type: 'user-login' },
        headers: { 'Content-Type': 'application/json' },
    };
    return http.post(url, payload, params);
}

function socialSignupAttempt(socialType) {
    const uniqueSuffix = `${__VU}-${__ITER}-${Date.now()}`;
    const url = `${BASE_URL}/api/v1/auth/social/signup`;
    const payload = JSON.stringify({
        email: `k6-social-${uniqueSuffix}@test.com`,
        name: 'K6 테스트유저',
        phone: `010-8${String(Math.floor(Math.random() * 1000)).padStart(3, '0')}-${String(Math.floor(Math.random() * 10000)).padStart(4, '0')}`,
        birthDate: '1995-05-05',
        gender: 'MALE',
        nickname: `k6닉네임${uniqueSuffix}`,
        socialType,
        referralCode: null,
        signupPath: '광고',
        termsServiceAgreed: true,
        termsPrivacyAgreed: true,
        termsMarketingAgreed: false,
    });
    const params = {
        tags: { type: 'user-social-signup' },
        headers: { 'Content-Type': 'application/json' },
    };
    return http.post(url, payload, params);
}

export default function () {
    // 80%는 로그인(성공/실패 섞어서), 20%는 소셜 추가정보 가입
    if (Math.random() < 0.8) {
        const n = randomDummyUserNumber();
        const username = `silver${String(n).padStart(3, '0')}`;

        // 70%는 정상 비밀번호(성공), 30%는 일부러 틀린 비밀번호(실패) 유도
        const password = Math.random() < 0.7 ? 'password123' : 'wrong-password';
        const res = loginAttempt(username, password);

        check(res, {
            'login: 응답이 왔다': (r) => r.status === 200 || r.status === 400 || r.status === 401 || r.status === 409,
        });
    } else {
        const socialType = Math.random() < 0.5 ? 'GOOGLE' : 'KAKAO';
        const res = socialSignupAttempt(socialType);

        check(res, {
            'social signup: 정상 처리': (r) => r.status === 200 || r.status === 201,
        });
    }

    sleep(1 + Math.random() * 2);
}
