package com.resumise.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CvUpdateRequest(
        @NotBlank(message = "title is required")
        @Size(max = 120, message = "title must be at most 120 characters")
        String title
) {
}

