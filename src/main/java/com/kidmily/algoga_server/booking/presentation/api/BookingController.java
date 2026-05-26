package com.kidmily.algoga_server.booking.presentation.api;

import com.kidmily.algoga_server.booking.application.command.CreateBookingCommand;
import com.kidmily.algoga_server.booking.application.usecase.BookingCommandUseCase;
import com.kidmily.algoga_server.booking.application.usecase.BookingQueryUseCase;
import com.kidmily.algoga_server.booking.exception.BookingErrorCode;
import com.kidmily.algoga_server.booking.presentation.api.request.CreateBookingRequest;
import com.kidmily.algoga_server.booking.presentation.api.response.BookingResponse;
import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking", description = "예약 API")
public class BookingController {

    private final BookingCommandUseCase bookingCommandUseCase;
    private final BookingQueryUseCase bookingQueryUseCase;

    @PostMapping
    @Operation(summary = "예약 생성", description = "항공편과 숙소를 선택해 예약합니다.")
    @ApiErrorCodeExample(domain = BookingErrorCode.class, value = {"PACKAGE_NOT_AVAILABLE"})
    public ResponseEntity<ApiResponse<Long>> createBooking(
            @Valid @RequestBody CreateBookingRequest request
    ) {
        CreateBookingCommand command = new CreateBookingCommand(
                request.accommodationId(),
                request.userId(),
                request.flightInfo(),
                request.flightPrice(),
                request.checkInDate(),
                request.checkOutDate()
        );
        Long bookingId = bookingCommandUseCase.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("BOOKING_CREATED", "예약이 생성됐습니다.", bookingId));
    }

    @GetMapping("/{bookingId}")
    @Operation(summary = "예약 상세 조회", description = "예약 상세 정보를 조회합니다.")
    @ApiErrorCodeExample(domain = BookingErrorCode.class, value = {"BOOKING_NOT_FOUND"})
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(
            @Parameter(description = "예약 ID", example = "1")
            @PathVariable Long bookingId
    ) {
        BookingResponse response = bookingQueryUseCase.getBooking(bookingId);
        return ResponseEntity.ok(ApiResponse.success("BOOKING_FOUND", "예약 조회에 성공했습니다.", response));
    }

    @DeleteMapping("/{bookingId}/cancel")
    @Operation(summary = "예약 취소", description = "예약 취소를 요청합니다. CANCEL_REQUESTED 상태로 변경됩니다.")
    @ApiErrorCodeExample(domain = BookingErrorCode.class, value = {"BOOKING_NOT_FOUND"})
    public ResponseEntity<ApiResponse<Void>> cancelBooking(
            @Parameter(description = "예약 ID", example = "1")
            @PathVariable Long bookingId
    ) {
        bookingCommandUseCase.cancel(bookingId);
        return ResponseEntity.ok(ApiResponse.success("BOOKING_CANCELLED", "예약 취소 요청이 완료됐습니다."));
    }
}