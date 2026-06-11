// k6/scripts/04-error-scenario.js
// ------------------------------------------------------------
// 목적:
// - 실패를 의도적으로 만들어 Prometheus와 Loki에서 원인 추적 흐름을 연습합니다.
// - validation 실패와 재고 부족 실패를 분리해서 관찰합니다.
//
// 관찰할 것:
// - Prometheus: shop_api_errors_total{reason="validation"}
// - Prometheus: shop_orders_failed_total{reason="insufficient_stock"}
// - Loki: event=api_error reason=validation
// - Loki: event=order_create_failed exceptionType=InsufficientStockException
//
// 실행:
//   k6 run .\k6\scripts\04-error-scenario.js
// ------------------------------------------------------------

import http from 'k6/http';
import { sleep } from 'k6';
import { createInvalidOrder, createOrder } from '../lib/client.js';
import { randomSleepSeconds } from '../lib/config.js';
import { createSummaryHandler } from '../lib/summary.js';

// 이 스크립트는 validation 실패와 재고 부족 실패를 의도적으로 만든다.
// 따라서 400 응답은 HTTP 레벨 실패가 아니라 수업에서 관찰할 정상적인 테스트 결과다.
http.setResponseCallback(http.expectedStatuses({ min: 200, max: 399 }, 400));

export const options = {
    // 오류 시나리오도 p99를 출력해둡니다.
    // 다만 이 스크립트의 목적은 latency 합격 판정이 아니라 오류 로그/메트릭 관찰입니다.
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],

    scenarios: {
        error_scenario: {
            // constant-vus는 지정한 VU 수를 duration 동안 일정하게 유지합니다.
            // 오류 로그를 안정적으로 계속 만들기 위한 단순한 실행 모델입니다.
            executor: 'constant-vus',

            // 동시에 5명의 사용자가 잘못된 주문 또는 재고 부족 주문을 반복합니다.
            // 숫자가 너무 크면 로그가 과도하게 쌓여 수업 중 해석이 어려워질 수 있습니다.
            vus: 5,

            // 1분 동안 오류 로그와 메트릭이 충분히 발생하도록 유지합니다.
            duration: '1m',
        },
    },
    thresholds: {
        // 이 시나리오는 오류 응답을 의도적으로 만들기 때문에 http_req_failed 기준을 두지 않습니다.
        // 대신 check에서 기대한 400인지 확인합니다.
        // checks rate가 95% 이상이면 "의도한 실패가 의도한 방식으로 발생했다"고 판단합니다.
        checks: ['rate>0.95'],
    },
};

export default function () {
    const traffic = Math.random();

    if (traffic < 0.5) {
        // validation 실패: quantity=0으로 400 응답을 의도합니다.
        createInvalidOrder();
    } else {
        // 재고 부족 실패: 매우 큰 quantity로 InsufficientStockException을 의도합니다.
        createOrder(1, 999999);
    }

    sleep(randomSleepSeconds());
}

export const handleSummary = createSummaryHandler('04-error-scenario');