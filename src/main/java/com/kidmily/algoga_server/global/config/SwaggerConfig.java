package com.kidmily.algoga_server.global.config;

import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import com.kidmily.algoga_server.global.common.api.response.ErrorResponse;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import java.time.Instant;

@Configuration
public class SwaggerConfig {

    @Bean
    public OperationCustomizer customErrorCodeCustomizer() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            ApiErrorCodeExample[] annotations = handlerMethod.getMethod().getAnnotationsByType(ApiErrorCodeExample.class);
            if (annotations.length == 0) {
                return operation;
            }

            ApiResponses responses = operation.getResponses();

            for (ApiErrorCodeExample apiExample : annotations) {
                Class<? extends BaseErrorCode> domainClass = apiExample.domain();

                for (String codeName : apiExample.value()) {
                    BaseErrorCode errorCode = getErrorCodeInstance(domainClass, codeName);

                    if (errorCode == null) continue;

                    String statusCode = String.valueOf(errorCode.getStatus().value());

                    // 🌟 스웨거 예시용 ErrorResponse 생성 시 가상의 traceId를 주입합니다.
                    ErrorResponse errorResponseExample = new ErrorResponse(
                            Instant.parse("2026-05-21T07:09:00Z"),
                            errorCode.getStatus().value(),
                            errorCode.getCode(),
                            errorCode.getMessage(),
                            "example-trace-id-1234" // 고정된 예시값이나 UUID를 넣습니다.
                    );

                    Example example = new Example();
                    example.value(errorResponseExample);
                    example.description(errorCode.getMessage());

                    ApiResponse apiResponse = responses.containsKey(statusCode)
                            ? responses.get(statusCode)
                            : new ApiResponse().description(errorCode.getStatus().getReasonPhrase());

                    Content content = apiResponse.getContent();
                    if (content == null) content = new Content();

                    MediaType mediaType = content.get("application/json");
                    if (mediaType == null) mediaType = new MediaType();

                    String exampleKey = domainClass.getSimpleName().toUpperCase() + "_" + codeName;
                    mediaType.addExamples(exampleKey, example);

                    content.addMediaType("application/json", mediaType);
                    apiResponse.setContent(content);

                    responses.addApiResponse(statusCode, apiResponse);
                }
            }
            return operation;
        };
    }

    private BaseErrorCode getErrorCodeInstance(Class<? extends BaseErrorCode> domainClass, String codeName) {
        if (domainClass.isEnum()) {
            for (BaseErrorCode enumConstant : domainClass.getEnumConstants()) {
                if (((Enum<?>) enumConstant).name().equals(codeName)) {
                    return enumConstant;
                }
            }
        }
        return null;
    }

    @Bean // 스웨거에서 토큰 넣어줄 수 있게 승재 추가
    public OpenAPI openAPI() {
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("bearerAuth");

        return new OpenAPI()
                .addSecurityItem(securityRequirement)
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", securityScheme));
    }
}