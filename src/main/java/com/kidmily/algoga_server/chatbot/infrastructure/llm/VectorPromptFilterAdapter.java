package com.kidmily.algoga_server.chatbot.infrastructure.llm;

import com.kidmily.algoga_server.chatbot.application.port.out.PromptFilterPort;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class VectorPromptFilterAdapter implements PromptFilterPort {

    private final EmbeddingModel embeddingModel;

    // 🌟 오직 정답(White) 질문 벡터만 저장합니다.
    private final List<float[]> whiteVectors = new ArrayList<>();

    @Value("classpath:chatbot/data-filter.csv")
    private Resource csvResource;

    // 🌟 빡빡한 통과 기준 (필요시 0.75까지 올려도 됩니다)
    private static final double PASS_THRESHOLD = 0.85;

    @PostConstruct
    public void initVectorsFromCsv() {
        int whiteCount = 0;

        if (csvResource == null || !csvResource.exists()) {
            log.error("[Vector Filter] ❌ CSV 파일을 찾을 수 없습니다.");
            return;
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(csvResource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",", 2);
                if (parts.length < 2) continue;

                String type = parts[0].trim().toUpperCase();
                String question = parts[1].trim();

                // WHITE 타입인 경우에만 임베딩하여 리스트에 추가 (나머지는 무시)
                if ("WHITE".equals(type)) {
                    Object rawVector = embeddingModel.embed("search_document: " + question);
                    float[] vector = normalizeToFloatArray(rawVector);

                    if (vector.length > 0) {
                        whiteVectors.add(vector);
                        whiteCount++;
                    }
                }
            }
            log.info("[Vector Filter] 🎉 화이트리스트 전용 데이터 로드 및 임베딩 완료 (총 {}개)", whiteCount);

        } catch (Exception e) {
            log.error("[Vector Filter] ❌ CSV 파일을 읽거나 임베딩하는 중 오류 발생", e);
        }
    }

    @Override
    public boolean isValidQuestion(String question) {
        if (whiteVectors.isEmpty()) return false;

        // 너무 짧은 질문(예: "안녕", "야")은 아예 유사도 검사도 안 하고 즉시 차단
        if (question == null || question.trim().length() < 2) {
            log.warn("[Vector Filter] ⛔ 차단됨: 너무 짧거나 무의미한 질문 ({})", question);
            return false;
        }

        Object rawVector = embeddingModel.embed("search_query: " + question);
        float[] userVector = normalizeToFloatArray(rawVector);

        if (userVector.length == 0) return false;

        double maxWhiteSimilarity = -1.0;

        // 화이트리스트 벡터들과만 비교
        for (float[] whiteVector : whiteVectors) {
            double sim = calculateCosineSimilarity(userVector, whiteVector);
            if (sim > maxWhiteSimilarity) {
                maxWhiteSimilarity = sim;
            }
        }

        log.info("[Vector Filter] 입력 질문: '{}' | 최대 일치도: {}", question, maxWhiteSimilarity);

        // 🌟 지정된 임계치(0.70) 이상일 때만 통과
        if (maxWhiteSimilarity >= PASS_THRESHOLD) {
            log.info("[Vector Filter] ✅ 필터 통과 (허용된 질문 유형)");
            return true;
        } else {
            log.warn("[Vector Filter] ⛔ 차단됨: 서비스와 무관한 질문 (유사도 미달)");
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private float[] normalizeToFloatArray(Object raw) {
        if (raw instanceof float[]) {
            return (float[]) raw;
        } else if (raw instanceof List) {
            List<Double> list = (List<Double>) raw;
            float[] arr = new float[list.size()];
            for (int i = 0; i < list.size(); i++) {
                arr[i] = list.get(i).floatValue();
            }
            return arr;
        } else if (raw instanceof double[]) {
            double[] dArr = (double[]) raw;
            float[] arr = new float[dArr.length];
            for (int i = 0; i < dArr.length; i++) {
                arr[i] = (float) dArr[i];
            }
            return arr;
        }
        return new float[0];
    }

    private double calculateCosineSimilarity(float[] v1, float[] v2) {
        double dotProduct = 0.0, norm1 = 0.0, norm2 = 0.0;
        int size = Math.min(v1.length, v2.length);

        for (int i = 0; i < size; i++) {
            dotProduct += v1[i] * v2[i];
            norm1 += Math.pow(v1[i], 2);
            norm2 += Math.pow(v2[i], 2);
        }
        if (norm1 == 0 || norm2 == 0) return 0.0;
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}