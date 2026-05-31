package com.kidmily.algoga_server.banner.presentation.api;

import com.kidmily.algoga_server.banner.application.usecase.BannerCommandUseCase;
import com.kidmily.algoga_server.banner.application.usecase.BannerQueryUseCase;
import com.kidmily.algoga_server.banner.presentation.api.response.BannerResponse;

import com.kidmily.algoga_server.global.security.GlobalJwtProvider;
// 🌟 1. 방금 에러에서 요구한 CustomUserDetailsService 임포트 추가!
import com.kidmily.algoga_server.user.settings.CustomUserDetailsService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = BannerController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class}
)
@AutoConfigureMockMvc(addFilters = false)
class BannerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GlobalJwtProvider globalJwtProvider;

    // 🌟 2. 방금 에러에서 찾지 못했다고 한 빈(Bean)을 가짜로 추가!
    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private BannerCommandUseCase bannerCommandUseCase;

    @MockitoBean
    private BannerQueryUseCase bannerQueryUseCase;

    @Test
    @DisplayName("배너 목록을 성공적으로 조회한다.")
    void getBanners_success() throws Exception {
        // given
        List<BannerResponse> mockResponses = List.of(
                new BannerResponse(1L, "url1", "IMAGE", "link1", "text1"),
                new BannerResponse(2L, "url2", "VIDEO", "link2", "text2")
        );
        given(bannerQueryUseCase.getActiveBanners()).willReturn(mockResponses);

        // when & then
        mockMvc.perform(get("/api/v1/banner")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].bannerId").value(1L))
                .andExpect(jsonPath("$.data[0].fileType").value("IMAGE"))
                .andDo(print());
    }

    @Test
    @DisplayName("배너를 성공적으로 등록한다.")
    @WithMockUser(roles = "CS_MANAGER")
    void registerBanner_success() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test data".getBytes());
        given(bannerCommandUseCase.registerBanner(any())).willReturn(1L);

        // when & then
        mockMvc.perform(multipart("/api/v1/banner/register")
                        .file(file)
                        .param("linkUrl", "http://test.com")
                        .param("text", "테스트 배너")
                        .param("isVisible", "true")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.bannerId").value(1L))
                .andDo(print());
    }

    @Test
    @DisplayName("배너를 성공적으로 삭제한다.")
    @WithMockUser(roles = "CS_MANAGER")
    void deleteBanner_success() throws Exception {
        // given
        Long bannerId = 1L;

        // when & then
        mockMvc.perform(delete("/api/v1/banner/delete/{bannerId}", bannerId))
                .andExpect(status().isNoContent())
                .andDo(print());

        verify(bannerCommandUseCase).deleteBanner(bannerId);
    }
}