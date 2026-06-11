// k6/lib/client.js
// ------------------------------------------------------------
// Popular Shop API 호출을 함수로 감싼 파일입니다.
//
// 수업 의도:
// - 시나리오 파일은 "사용자 행동"에 집중한다.
// - HTTP 세부 구현은 client helper로 분리한다.
// - tags를 일관되게 붙여 k6 결과에서 API별 지표를 분리한다.
// ------------------------------------------------------------

import http from 'k6/http';
import { check } from 'k6';
import { BASE_URL, ORDER_QUANTITY, randomProductId } from './config.js';

export function getPopularProducts() {
    const res = http.get(`${BASE_URL}/products/popular`, {
        // k6 metric을 API 성격별로 나누기 위한 tag입니다.
        // threshold에서 http_req_duration{type:popular}처럼 사용할 수 있습니다.
        tags: {
            type: 'popular',
            api: 'GET /products/popular',
        },
    });

    check(res, {
        'popular: status is 200': (r) => r.status === 200,
        // 응답 JSON parsing이 실패하면 check가 실패합니다.
        // 성능 테스트에서도 응답 구조가 최소한 정상인지 확인해야 합니다.
        'popular: response is array': (r) => Array.isArray(r.json()),
    });

    return res;
}

export function getProduct(productId = randomProductId()) {
    const res = http.get(`${BASE_URL}/products/${productId}`, {
        tags: {
            type: 'product-detail',
            api: 'GET /products/{id}',
        },
    });

    check(res, {
        'product detail: status is 200': (r) => r.status === 200,
    });

    return res;
}

export function createOrder(productId = randomProductId(), quantity = ORDER_QUANTITY) {
    const body = JSON.stringify({
        items: [{ productId, quantity }],
    });

    const res = http.post(`${BASE_URL}/orders`, body, {
        tags: {
            type: 'order',
            api: 'POST /orders',
        },
        headers: {
            'Content-Type': 'application/json',
        },
    });

    check(res, {
        // 현재 실습 데이터는 재고가 제한되어 있으므로 장시간 테스트 시 400이 발생할 수 있습니다.
        // 수업에서는 400을 시스템 장애가 아니라 비즈니스 실패로 볼 수 있음을 설명합니다.
        'order: status is 201 or business failure 400': (r) => r.status === 201 || r.status === 400,
    });

    return res;
}

export function createInvalidOrder() {
    const body = JSON.stringify({
        items: [{ productId: randomProductId(), quantity: 0 }],
    });

    const res = http.post(`${BASE_URL}/orders`, body, {
        tags: {
            type: 'invalid-order',
            api: 'POST /orders invalid',
        },
        headers: {
            'Content-Type': 'application/json',
        },
    });

    check(res, {
        // validation 실패를 의도적으로 만드는 API 호출입니다.
        // Loki의 event=api_error reason=validation과 연결해서 설명합니다.
        'invalid order: status is 400': (r) => r.status === 400,
    });

    return res;
}