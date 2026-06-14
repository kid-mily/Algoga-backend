package com.kidmily.algoga_server.accommodation.application.service;

import com.kidmily.algoga_server.accommodation.domain.model.Accommodation;
import com.kidmily.algoga_server.accommodation.domain.repository.AccommodationRepository;
import com.kidmily.algoga_server.global.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/*
 * AccommodationQueryService 단위 테스트
 * - 숙소 단건 조회 / 국가별 조회 / 없는 숙소 예외 검증
 */
@ExtendWith(MockitoExtension.class)
class AccommodationQueryServiceTest {

    @Mock private AccommodationRepository accommodationRepository;

    @InjectMocks
    private AccommodationQueryService accommodationQueryService;

    @BeforeEach
    @DisplayName("Mock 객체 주입 확인")
    void setUp() {
        assertNotNull(accommodationQueryService);
    }

    @Test
    @DisplayName("ID로 숙소 조회 성공")
    void ID로_숙소_조회_성공() {
        // given
        Accommodation accommodation = mock(Accommodation.class);
        when(accommodation.getId()).thenReturn(1L);
        when(accommodation.getName()).thenReturn("제주 호텔");
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(accommodation));

        // when
        var response = accommodationQueryService.getById(1L);

        // then
        assertNotNull(response);
    }

    @Test
    @DisplayName("국가 ID로 숙소 목록 조회 성공")
    void 국가별_숙소_목록_조회_성공() {
        // given
        Accommodation acc1 = mock(Accommodation.class);
        Accommodation acc2 = mock(Accommodation.class);
        when(accommodationRepository.findByCountryId(1L)).thenReturn(List.of(acc1, acc2));

        // when
        var result = accommodationQueryService.getByCountry(1L);

        // then
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("존재하지 않는 숙소 조회 시 예외 발생")
    void 없는_숙소_조회_시_예외_발생() {
        // given
        when(accommodationRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(BusinessException.class, () ->
                accommodationQueryService.getById(999L));
    }
}