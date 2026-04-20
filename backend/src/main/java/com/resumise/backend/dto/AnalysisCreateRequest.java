package com.resumise.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AnalysisCreateRequest(
        @NotNull @Positive(message = "cvId must be positive") Long cvId,
        @NotBlank @Size(max = 4000) String jobDescription,
        @Size(max = 2048) String jobLink
) {
}

