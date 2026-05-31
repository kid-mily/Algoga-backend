package com.kidmily.algoga_server.service.banner.service;

import com.kidmily.algoga_server.banner.application.service.BannerQueryService;
import com.kidmily.algoga_server.banner.domain.model.Banner;
import com.kidmily.algoga_server.banner.domain.repository.BannerRepository;
import com.kidmily.algoga_server.banner.presentation.api.response.BannerResponse;
import com.kidmily.algoga_server.global.type.FileType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class BannerQueryServiceTest {

    @InjectMocks
    private BannerQueryService bannerQueryService;

    @Mock
    private BannerRepository bannerRepository;

    @Test
    @DisplayName("활성화된 배너 목록을 응답 DTO로 변환하여 반환한다.")
    void getActiveBanners_success() {
        // given
        Banner banner1 = Banner.builder()
                .bannerId(1L)
                .managerId(10L)
                .imageUrl("url1")
                .fileType(FileType.IMAGE)
                .linkUrl("link1")
                .text("text1")
                .isVisible(true)
                .createdAt(Instant.now())
                .build();

        given(bannerRepository.findVisibleBanners()).willReturn(List.of(banner1));

        // when
        List<BannerResponse> responses = bannerQueryService.getActiveBanners();

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).bannerId()).isEqualTo(1L);
        assertThat(responses.get(0).imageUrl()).isEqualTo("url1");
        assertThat(responses.get(0).fileType()).isEqualTo("IMAGE");
    }
}