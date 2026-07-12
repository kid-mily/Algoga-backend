package com.kidmily.algoga_server.review.presentation.request.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Update course review visibility request")
public record UpdateCourseReviewVisibilityRequest(
        @NotNull Boolean hidden
) {
}
