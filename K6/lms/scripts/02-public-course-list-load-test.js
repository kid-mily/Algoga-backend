import http from 'k6/http';
import { check, sleep } from 'k6';
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.2/index.js';
import { BASE_URL, USER_COUNT, randomSleepSeconds } from '../../common/config.js';

const COUNTRY_ID = Number(__ENV.COUNTRY_ID || 1);

export const options = {
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
    scenarios: {
        public_course_list: {
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
        'http_req_duration{type:lms_public_course_list}': ['p(95)<1000'],
    },
};

export default function () {
    const res = http.get(
        `${BASE_URL}/api/v1/courses/countries/${COUNTRY_ID}`,
        {
            headers: {
                Accept: 'application/json',
            },
            tags: {
                type: 'lms_public_course_list',
                api: 'GET /api/v1/courses/countries/{countryId}',
            },
        }
    );

    if (res.status !== 200) {
        console.warn(`public course list failed. status=${res.status}, body=${res.body}`);
    }

    check(res, {
        'public course list: status is 200': (r) => r.status === 200,
        'public course list: response has success envelope': (r) =>
            r.body && r.body.includes('COUNTRY_COURSES_FOUND'),
    });

    sleep(randomSleepSeconds());
}

export function handleSummary(data) {
    return {
        stdout: textSummary(data, { indent: ' ', enableColors: true }),
        'K6/lms/results/02-public-course-list-load-test-summary.json': JSON.stringify(data, null, 2),
        'K6/lms/results/02-public-course-list-load-test-summary.md':
            '# Load Test Report: 02-public-course-list-load-test\n\n' +
            textSummary(data, { indent: ' ', enableColors: false }),
    };
}