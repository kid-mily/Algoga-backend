import http from 'k6/http';
import { check, fail, sleep } from 'k6';
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.2/index.js';
import { BASE_URL } from '../../common/config.js';

const USER_COUNT = Number(__ENV.USER_COUNT || 1000);
const TARGET = (__ENV.TARGET || 'both').toLowerCase();
const ADMIN_LOGIN_ID = __ENV.ADMIN_LOGIN_ID;
const ADMIN_PASSWORD = __ENV.ADMIN_PASSWORD;
const ADMIN_ACCESS_TOKEN = __ENV.ADMIN_ACCESS_TOKEN;
const PAGE = Number(__ENV.PAGE || 0);
const SIZE = Number(__ENV.SIZE || 20);
const COURSE_ID = __ENV.COURSE_ID ? Number(__ENV.COURSE_ID) : null;
const COUNTRY_ID = __ENV.COUNTRY_ID ? Number(__ENV.COUNTRY_ID) : null;

export const options = {
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
    scenarios: {
        admin_performance: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '1m', target: USER_COUNT },
                { duration: '2m', target: USER_COUNT },
                { duration: '30s', target: 0 },
            ],
            gracefulRampDown: '30s',
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.01'],
        'http_req_duration{type:admin_course_list}': ['p(95)<2000'],
        'http_req_duration{type:admin_coupon_statistics}': ['p(95)<2000'],
    },
};

export function setup() {
    if (ADMIN_ACCESS_TOKEN) {
        return {
            cookieHeader: `adminAccessToken=${ADMIN_ACCESS_TOKEN}`,
        };
    }

    if (!ADMIN_LOGIN_ID || !ADMIN_PASSWORD) {
        fail('ADMIN_ACCESS_TOKEN or ADMIN_LOGIN_ID/ADMIN_PASSWORD env vars are required.');
    }

    const loginRes = http.post(
        `${BASE_URL}/api/v1/auth/admin/login`,
        JSON.stringify({
            loginId: ADMIN_LOGIN_ID,
            password: ADMIN_PASSWORD,
        }),
        {
            headers: {
                'Content-Type': 'application/json',
                Accept: 'application/json',
            },
            tags: {
                type: 'admin_login_setup',
                api: 'POST /api/v1/auth/admin/login',
            },
        }
    );

    const loginOk = check(loginRes, {
        'admin login setup: status is 200': (r) => r.status === 200,
        'admin login setup: has adminAccessToken cookie': (r) =>
            r.cookies.adminAccessToken && r.cookies.adminAccessToken.length > 0,
    });

    if (!loginOk) {
        fail(`admin login setup failed. status=${loginRes.status}, body=${loginRes.body}`);
    }

    return {
        cookieHeader: `adminAccessToken=${loginRes.cookies.adminAccessToken[0].value}`,
    };
}

export default function (data) {
    if (TARGET === 'course' || TARGET === 'both') {
        requestAdminCourseList(data.cookieHeader);
    }

    if (TARGET === 'coupon' || TARGET === 'both') {
        requestCouponStatistics(data.cookieHeader);
    }

    sleep(Math.random() * 2 + 1);
}

function requestAdminCourseList(cookieHeader) {
    const res = http.get(
        `${BASE_URL}/api/v1/admin/courses?page=${PAGE}&size=${SIZE}`,
        {
            headers: {
                Accept: 'application/json',
                Cookie: cookieHeader,
            },
            tags: {
                type: 'admin_course_list',
                api: 'GET /api/v1/admin/courses',
            },
        }
    );

    check(res, {
        'admin course list: status is 200': (r) => r.status === 200,
        'admin course list: response has success envelope': (r) =>
            r.body && r.body.includes('ADMIN_COURSES_FOUND'),
    });
}

function requestCouponStatistics(cookieHeader) {
    const query = buildCouponStatisticsQuery();
    const res = http.get(
        `${BASE_URL}/api/v1/admin/coupon-statistics${query}`,
        {
            headers: {
                Accept: 'application/json',
                Cookie: cookieHeader,
            },
            tags: {
                type: 'admin_coupon_statistics',
                api: 'GET /api/v1/admin/coupon-statistics',
            },
        }
    );

    check(res, {
        'coupon statistics: status is 200': (r) => r.status === 200,
        'coupon statistics: response has success envelope': (r) =>
            r.body && r.body.includes('COUPON_STATISTICS_FOUND'),
    });
}

function buildCouponStatisticsQuery() {
    const params = [];

    if (COURSE_ID !== null) {
        params.push(`courseId=${COURSE_ID}`);
    }

    if (COUNTRY_ID !== null) {
        params.push(`countryId=${COUNTRY_ID}`);
    }

    return params.length === 0 ? '' : `?${params.join('&')}`;
}

export function handleSummary(data) {
    const name = `04-admin-performance-${TARGET}`;

    return {
        stdout: textSummary(data, { indent: ' ', enableColors: true }),
        [`K6/lms/results/${name}-summary.json`]: JSON.stringify(data, null, 2),
        [`K6/lms/results/${name}-summary.md`]:
            `# Load Test Report: ${name}\n\n` +
            textSummary(data, { indent: ' ', enableColors: false }),
    };
}
