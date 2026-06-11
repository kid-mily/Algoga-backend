package com.kidmily.algoga_server.service.banner.service;

import com.kidmily.algoga_server.banner.application.command.CreateBannerCommand;
import com.kidmily.algoga_server.banner.application.service.BannerCommandService;
import com.kidmily.algoga_server.banner.domain.model.Banner;
import com.kidmily.algoga_server.banner.domain.repository.BannerRepository;
import com.kidmily.algoga_server.banner.exception.BannerErrorCode;
import com.kidmily.algoga_server.banner.exception.BannerException;
import com.kidmily.algoga_server.banner.settings.BannerStorageSettings;
import com.kidmily.algoga_server.global.port.out.FileStoragePort;
import com.kidmily.algoga_server.global.type.FileType;
import com.kidmily.algoga_server.global.util.FileTypeDetector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BannerCommandServiceTest {

    @InjectMocks
    private BannerCommandService bannerCommandService;

    @Mock
    private BannerRepository bannerRepository;

    @Mock
    private FileStoragePort fileStoragePort;

    @Mock
    private BannerStorageSettings storageSettings;

    @Test
    @DisplayName("배너 등록 시 스토리지에 파일을 업로드하고 도메인을 저장한다.")
    void registerBanner_success() {
        // given
        MockMultipartFile file = new MockMultipartFile("image", "test.jpg", "image/jpeg", "data".getBytes());
        CreateBannerCommand command = new CreateBannerCommand(file, "link", "text", true, 999L);

        given(storageSettings.getBucketName()).willReturn("test-bucket");
        given(storageSettings.getDirectory()).willReturn("test-dir");
        given(fileStoragePort.uploadFile(any(), any(), any())).willReturn("https://uploaded-url.com/test.jpg");

        try (MockedStatic<FileTypeDetector> mockedStatic = mockStatic(FileTypeDetector.class)) {
            mockedStatic.when(() -> FileTypeDetector.determineFileType(file)).thenReturn(FileType.IMAGE);

            Banner savedBanner = Banner.create(999L, "https://uploaded-url.com/test.jpg", null, "link", "text", true, Instant.now());
            given(bannerRepository.save(any(Banner.class))).willReturn(savedBanner);

            // when
            bannerCommandService.registerBanner(command);

            // then
            verify(fileStoragePort, times(1)).uploadFile(file, "test-bucket", "test-dir");
            verify(bannerRepository, times(1)).save(any(Banner.class));
        }
    }

    @Test
    @DisplayName("배너 삭제 시 스토리지 파일을 지우고 레포지토리에서 제거한다.")
    void deleteBanner_success() {
        // given
        Long bannerId = 1L;
        Banner banner = Banner.create(999L, "image-url", null, "link", "text", true, Instant.now());

        given(bannerRepository.findById(bannerId)).willReturn(Optional.of(banner));
        given(storageSettings.getBucketName()).willReturn("test-bucket");

        // when
        bannerCommandService.deleteBanner(bannerId);

        // then
        verify(fileStoragePort, times(1)).deleteFile("test-bucket", "image-url");
        verify(bannerRepository, times(1)).deleteById(bannerId);
    }

    @Test
    @DisplayName("삭제하려는 배너가 없으면 예외가 발생한다.")
    void deleteBanner_notFound() {
        // given
        Long bannerId = 1L;
        given(bannerRepository.findById(bannerId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> bannerCommandService.deleteBanner(bannerId))
                .isInstanceOf(BannerException.class)
                .hasMessage(BannerErrorCode.BANNER_NOT_FOUND.getMessage());
    }
}