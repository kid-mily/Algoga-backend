import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.2/index.js';

export function createSummaryHandler(domain, scriptName) {
    return function(data) {
        // 결과를 각 도메인의 results 폴더에 저장
        const jsonPath = `../${domain}/results/${scriptName}-summary.json`;
        const mdPath = `../${domain}/results/${scriptName}-summary.md`;

        const result = {
            'stdout': textSummary(data, { indent: ' ', enableColors: true }),
        };

        result[jsonPath] = JSON.stringify(data, null, 2);
        // 간단한 Markdown 생성 (필요시 커스텀 가능)
        result[mdPath] = `# Load Test Report: ${scriptName}\n\n` + textSummary(data, { indent: ' ', enableColors: false });

        return result;
    };
}