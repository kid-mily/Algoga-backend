package com.kidmily.algoga_server.booking.presentation.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kidmily.algoga_server.booking.application.command.CreateBookingCommand;
import com.kidmily.algoga_server.booking.application.usecase.BookingCommandUseCase;
import com.kidmily.algoga_server.booking.application.usecase.BookingQueryUseCase;
import com.kidmily.algoga_server.booking.exception.BookingErrorCode;
import com.kidmily.algoga_server.booking.presentation.api.request.CreateBookingRequest;
import com.kidmily.algoga_server.booking.presentation.api.response.BookingResponse;
import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.user.settings.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Booking", description = "예약 API")
public class BookingController {

    private final BookingCommandUseCase bookingCommandUseCase;
    private final BookingQueryUseCase bookingQueryUseCase;
    private final ObjectMapper objectMapper;

    @PostMapping("/api/v1/bookings")
    @Operation(summary = "예약 생성", description = "숙소와 항공편을 선택하여 예약을 생성합니다.")
    @ApiErrorCodeExample(domain = BookingErrorCode.class, value = {"PACKAGE_NOT_AVAILABLE"})
    public ResponseEntity<ApiResponse<Long>> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws Exception {
        Long userId = userDetails.getUser().getId();
        CreateBookingCommand command = new CreateBookingCommand(
                request.accommodationId(),
                userId,
                objectMapper.writeValueAsString(request.flightInfo()),
                request.flightPrice(),
                request.checkInDate(),
                request.checkOutDate()
        );
        Long bookingId = bookingCommandUseCase.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("BOOKING_CREATED", "예약이 생성되었습니다.", bookingId));
    }

    @GetMapping("/api/v1/bookings/{bookingId}")
    @Operation(summary = "예약 상세 조회", description = "예약 상세 정보를 조회합니다.")
    @ApiErrorCodeExample(domain = BookingErrorCode.class, value = {"BOOKING_NOT_FOUND"})
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(
            @Parameter(description = "예약 ID", example = "1")
            @PathVariable Long bookingId
    ) {
        BookingResponse response = bookingQueryUseCase.getBooking(bookingId);
        return ResponseEntity.ok(ApiResponse.success("BOOKING_FOUND", "예약 조회에 성공했습니다.", response));
    }

    @GetMapping("/api/v1/bookings/me")
    @Operation(summary = "내 예약 목록 조회", description = "로그인한 유저의 전체 예약 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyBookings(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        List<BookingResponse> response = bookingQueryUseCase.getMyBookings(userId);
        return ResponseEntity.ok(ApiResponse.success("MY_BOOKINGS_FOUND", "내 예약 목록 조회에 성공했습니다.", response));
    }

    @DeleteMapping("/api/v1/bookings/{bookingId}/cancel")
    @Operation(summary = "예약 취소", description = "예약 취소를 요청합니다. CANCEL_REQUESTED 상태로 변경됩니다.")
    @ApiErrorCodeExample(domain = BookingErrorCode.class, value = {"BOOKING_NOT_FOUND"})
    public ResponseEntity<ApiResponse<Void>> cancelBooking(
            @Parameter(description = "예약 ID", example = "1")
            @PathVariable Long bookingId,
            @AuthenticationPrincipal CustomUserDetails userDetails // 추가
    ) {
        bookingCommandUseCase.cancel(bookingId, userDetails.getUser().getId()); // userId 전달
        return ResponseEntity.ok(ApiResponse.success("BOOKING_CANCELLED", "예약 취소 요청이 완료되었습니다."));
    }
}