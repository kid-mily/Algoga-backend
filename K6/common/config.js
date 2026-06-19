export const BASE_URL = __ENV.BASE_URL || 'http://localhost:15000';
export const USER_COUNT = __ENV.USER_COUNT || 100;

export function randomSleepSeconds() {
    return Math.random() * 2 + 1; // 1~3초 랜덤
}