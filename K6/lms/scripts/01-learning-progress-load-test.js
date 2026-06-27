import http from 'k6/http';
import { check, fail, sleep } from 'k6';
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.2/index.js';
import { BASE_URL, USER_COUNT, randomSleepSeconds } from '../../common/config.js';

const COURSE_ID = Number(__ENV.COURSE_ID || 1);
const CHAPTER_ID = Number(__ENV.CHAPTER_ID || 1);
const ACCESS_TOKEN = __ENV.ACCESS_TOKEN || '';
const LOGIN_USERNAME = __ENV.LOGIN_USERNAME || 'lhy1234lhy';
const LOGIN_PASSWORD = __ENV.LOGIN_PASSWORD || 'test0302';
const WATCH_SECONDS_START = Number(__ENV.WATCH_SECONDS_START || 5);
const WATCH_SECONDS_STEP = Number(__ENV.WATCH_SECONDS_STEP || 5);
const CHAPTER_DURATION_SECONDS = Number(__ENV.CHAPTER_DURATION_SECONDS || 300);

export const options = {
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
    scenarios: {
        learning_progress_update: {
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
        'http_req_duration{type:lms_progress_update}': ['p(95)<1000'],
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
    const watchedSeconds = Math.min(
        CHAPTER_DURATION_SECONDS,
        WATCH_SECONDS_START + ((__ITER + __VU) * WATCH_SECONDS_STEP) % CHAPTER_DURATION_SECONDS
    );

    const payload = JSON.stringify({ watchedSeconds });
    const headers = {
        'Accept': 'application/json',
        'Content-Type': 'application/json',
        'Cookie': data.cookieHeader,
    };

    const res = http.post(
        `${BASE_URL}/api/v1/courses/${COURSE_ID}/chapters/${CHAPTER_ID}/progress`,
        payload,
        {
            headers,
            tags: {
                type: 'lms_progress_update',
                api: 'POST /api/v1/courses/{courseId}/chapters/{chapterId}/progress',
            },
        }
    );

    if (res.status !== 200) {
        console.warn(`progress update failed. status=${res.status}, body=${res.body}`);
    }

    check(res, {
        'progress update: status is 200': (r) => r.status === 200,
        'progress update: response has success envelope': (r) => r.body && r.body.includes('LEARNING_PROGRESS_UPDATED'),
    });

    sleep(randomSleepSeconds());
}

export function handleSummary(data) {
    return {
        'stdout': textSummary(data, { indent: ' ', enableColors: true }),
        'K6/lms/results/01-learning-progress-load-test-summary.json': JSON.stringify(data, null, 2),
        'K6/lms/results/01-learning-progress-load-test-summary.md': '# Load Test Report: 01-learning-progress-load-test\n\n' + textSummary(data, { indent: ' ', enableColors: false }),
    };
}