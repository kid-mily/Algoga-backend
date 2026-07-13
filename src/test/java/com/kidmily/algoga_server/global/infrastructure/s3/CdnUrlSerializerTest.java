package com.kidmily.algoga_server.global.infrastructure.s3;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.kidmily.algoga_server.banner.presentation.api.response.BannerResponse;
import com.kidmily.algoga_server.community.presentation.api.response.PostResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link CdnMappable} + {@link CdnUrlSerializerModifier}가 응답 DTO의 {@code ~Url}/{@code ~Urls} 필드를
 * 상대경로 → 절대 URL로 매핑하고, {@link NoCdnUrl} 필드는 제외하는지 검증한다. (순수 Jackson)
 */
class CdnUrlSerializerTest {

    private static final String CDN = "https://cdn.example.com";

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        CdnUrlSerializer.configure(CDN);

        SimpleModule module = new SimpleModule("CdnUrlModule");
        module.setSerializerModifier(new CdnUrlSerializerModifier());
        objectMapper = new ObjectMapper().registerModule(module);
    }

    @Test
    @DisplayName("~Url 필드는 CDN 루트를 붙이고, @NoCdnUrl 필드는 상대경로여도 그대로 둔다")
    void urlField_prependsCdn_butNoCdnUrlExcluded() throws Exception {
        // linkUrl(@NoCdnUrl)에 일부러 상대경로를 넣어 미변환을 검증
        BannerResponse response = new BannerResponse(1L, "banners/abc.png", "IMAGE", "some/relative/link", "text");

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("\"imageUrl\":\"" + CDN + "/banners/abc.png\"");
        // linkUrl은 @NoCdnUrl → 상대경로여도 변환하지 않음
        assertThat(json).contains("\"linkUrl\":\"some/relative/link\"");
    }

    @Test
    @DisplayName("이미 절대 URL이거나 null이면 그대로 둔다")
    void absoluteOrNull_passthrough() throws Exception {
        BannerResponse absolute = new BannerResponse(1L, "https://other.com/x.png", "IMAGE", null, null);
        BannerResponse nullUrl = new BannerResponse(2L, null, "IMAGE", null, null);

        assertThat(objectMapper.writeValueAsString(absolute))
                .contains("\"imageUrl\":\"https://other.com/x.png\"");
        assertThat(objectMapper.writeValueAsString(nullUrl))
                .contains("\"imageUrl\":null");
    }

    @Test
    @DisplayName("List<String> 이미지 목록의 각 요소에도 CDN 루트를 붙인다")
    void listContent_prependsCdn() throws Exception {
        PostResponse response = new PostResponse(
                1L, 1L, "nick", "profiles/p.png", List.of(),
                "title", "content", 1L, "일본",
                List.of("posts/a.png", "posts/b.png"),
                0, 0L, 0L, 0L, List.of(), null
        );

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains(CDN + "/posts/a.png");
        assertThat(json).contains(CDN + "/posts/b.png");
        assertThat(json).contains("\"authorProfileImageUrl\":\"" + CDN + "/profiles/p.png\"");
    }
}
