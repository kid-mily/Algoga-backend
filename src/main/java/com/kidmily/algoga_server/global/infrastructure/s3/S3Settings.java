package com.kidmily.algoga_server.global.infrastructure.s3;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * S3/MinIO 공통 스토리지 설정 해석기.
 *
 * <p>단일 버킷을 사용하며, {@code S3_MODE} 환경변수로 동작 모드를 명시적으로 지정한다.
 * <ul>
 *     <li>{@code S3_MODE=aws} → AWS 모드: virtual-hosted, 엔드포인트 미지정 시 리전 엔드포인트 자동</li>
 *     <li>그 외/미설정(기본) → MinIO 모드: path-style, 엔드포인트 미지정 시 localhost:9000</li>
 * </ul>
 * 엔드포인트({@code S3_ENDPOINT})와 CDN 루트({@code S3_CDN_URL})는 직접 지정할 수 있으며,
 * 미지정 시 모드에 맞춰 자동 조합한다.
 *
 * <p>DB에는 object key(상대경로)만 저장하고, 응답 시 {@link #getCdnBase()}를 앞에 붙여 절대 URL을 만든다.
 */
@Slf4j
@Getter
@Component
public class S3Settings {

    private static final String DEFAULT_MINIO_ENDPOINT = "http://localhost:9000";

    /** 단일 버킷명. 환경변수 미설정 시 팀 기본 버킷으로 고정된다. */
    private final String bucket;

    /** AWS 리전 (virtual-hosted URL 조합 및 클라이언트 리전용). */
    private final String region;

    /** AWS 모드 여부 (S3_MODE=aws). */
    private final boolean awsMode;

    /** 실제 사용할 엔드포인트. 비어 있으면 endpoint override를 하지 않는다(AWS 리전 자동). */
    private final String endpoint;

    /** path-style 접근 여부. MinIO=true, AWS=false 가 기본이며 환경변수로 오버라이드 가능. */
    private final boolean pathStyle;

    /** 응답 URL 조합용 루트. key 앞에 붙는다. (trailing slash 제거) */
    private final String cdnBase;

    public S3Settings(
            @Value("${cloud.aws.s3.mode:minio}") String mode,
            @Value("${cloud.aws.s3.bucket:team02-01-635249349258-ap-northeast-2-an}") String bucket,
            @Value("${cloud.aws.region.static:ap-northeast-2}") String region,
            @Value("${cloud.aws.s3.endpoint:}") String endpoint,
            @Value("${cloud.aws.s3.path-style-access:}") String pathStyleOverride,
            @Value("${cloud.aws.s3.cdn-url:}") String cdnOverride
    ) {
        // 환경변수에 실수로 붙은 앞뒤 공백/개행이 버킷명·리전에 섞이면
        // AWS가 잘못된 버킷/호스트로 인식해 업로드가 500으로 실패하므로 방어적으로 trim 한다.
        this.bucket = bucket == null ? null : bucket.trim();
        this.region = region == null ? null : region.trim();
        this.awsMode = "aws".equalsIgnoreCase(mode == null ? "" : mode.trim());

        String rawEndpoint = endpoint == null ? "" : endpoint.trim();
        // 엔드포인트를 직접 주면 그대로 사용. 미지정 시 MinIO는 localhost 기본, AWS는 빈 값(리전 자동)
        this.endpoint = !rawEndpoint.isBlank()
                ? rawEndpoint
                : (awsMode ? "" : DEFAULT_MINIO_ENDPOINT);

        // path-style: 명시값이 있으면 그 값을, 없으면 모드 기본값(MinIO=true, AWS=false)을 사용
        this.pathStyle = (pathStyleOverride == null || pathStyleOverride.isBlank())
                ? !this.awsMode
                : Boolean.parseBoolean(pathStyleOverride.trim());

        this.cdnBase = resolveCdnBase(cdnOverride);

        log.info("[S3Settings] mode={}, bucket={}, endpoint={}, pathStyle={}, cdnBase={}",
                awsMode ? "AWS" : "MinIO",
                this.bucket,
                this.endpoint.isBlank() ? "(aws-regional-default)" : this.endpoint,
                this.pathStyle,
                this.cdnBase);
    }

    /** endpoint override가 필요한지 (엔드포인트가 지정된 경우). */
    public boolean hasEndpointOverride() {
        return !endpoint.isBlank();
    }

    private String resolveCdnBase(String cdnOverride) {
        String base;
        if (cdnOverride != null && !cdnOverride.isBlank()) {
            // 사용자가 직접 지정한 CDN 루트를 최우선으로 사용 (예: CloudFront 도메인)
            base = cdnOverride.trim();
        } else if (awsMode) {
            base = pathStyle
                    // path-style: https://s3.{region}.amazonaws.com/{bucket}
                    ? "https://s3." + region + ".amazonaws.com/" + bucket
                    // virtual-hosted(기본): https://{bucket}.s3.{region}.amazonaws.com
                    : "https://" + bucket + ".s3." + region + ".amazonaws.com";
        } else {
            // MinIO: path-style이면 버킷을 경로로 붙인다
            base = pathStyle ? endpoint + "/" + bucket : endpoint;
        }
        return base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    }
}
