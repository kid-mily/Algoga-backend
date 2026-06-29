import http from 'k6/http';
import { check, fail, sleep } from 'k6';
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.2/index.js';
import { BASE_URL, USER_COUNT, randomSleepSeconds } from '../../common/config.js';

const ACCESS_TOKEN = __ENV.ACCESS_TOKEN || '';
const LOGIN_USERNAME = __ENV.LOGIN_USERNAME || 'test1';
const LOGIN_PASSWORD = __ENV.LOGIN_PASSWORD || 'test1234';
const PAGE = Number(__ENV.PAGE || 0);
const SIZE = Number(__ENV.SIZE || 10);

export const options = {
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
    scenarios: {
        my_course_list: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '20s', target: USER_COUNT },
                { duration: '1m', target: USER_COUNT },
                { duration: '20s', target: 0 },
            ],
            gracefulRampDown: '10s',
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.01'],
        'http_req_duration{type:lms_my_course_list}': ['p(95)<1000'],
    },
};

export function setup() {
    if (ACCESS_TOKEN) {
        return { cookieHeader: `accessToken=${ACCESS_TOKEN}` };
    }

    const loginRes = http.post(
        `${BASE_URL}/api/v1/auth/login`,
        JSON.stringify({
            username: LOGIN_USERNAME,
            password: LOGIN_PASSWORD,
        }),
        {
            headers: {
                'Content-Type': 'application/json',
            },
            tags: {
                type: 'lms_login_setup',
                api: 'POST /api/v1/auth/login',
            },
        }
    );

    const loginOk = check(loginRes, {
        'login setup: status is 200': (r) => r.status === 200,
        'login setup: has accessToken cookie': (r) => r.cookies.accessToken && r.cookies.accessToken.length > 0,
    });

    if (!loginOk) {
        fail(`login setup failed. status=${loginRes.status}, body=${loginRes.body}`);
    }

    const accessToken = loginRes.cookies.accessToken[0].value;
    return { cookieHeader: `accessToken=${accessToken}` };
}

export default function (data) {
    const headers = {
        Accept: 'application/json',
        Cookie: data.cookieHeader,
    };

    const res = http.get(
        `${BASE_URL}/api/v1/my/courses?page=${PAGE}&size=${SIZE}`,
        {
            headers,
            tags: {
                type: 'lms_my_course_list',
                api: 'GET /api/v1/my/courses',
            },
        }
    );

    if (res.status !== 200) {
        console.warn(`my course list failed. status=${res.status}, body=${res.body}`);
    }

    check(res, {
        'my course list: status is 200': (r) => r.status === 200,
        'my course list: response has success envelope': (r) => r.body && r.body.includes('MY_COURSES_FOUND'),
    });

    sleep(randomSleepSeconds());
}

export function handleSummary(data) {
    return {
        stdout: textSummary(data, { indent: ' ', enableColors: true }),
        'K6/lms/results/03-my-course-list-load-test-summary.json': JSON.stringify(data, null, 2),
        'K6/lms/results/03-my-course-list-load-test-summary.md':
            '# Load Test Report: 03-my-course-list-load-test\n\n' +
            textSummary(data, { indent: ' ', enableColors: false }),
    };
}