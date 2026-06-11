package com.kidmily.algoga_server.stats.presentation.api.request;

import jakarta.validation.constraints.NotNull;

public record PaymentAttemptRequest(
        @NotNull Long accommodationId
) {
}
