package com.resumise.backend.dto;

import com.resumise.backend.model.AnalysisStatus;

public record AnalysisCreateResponse(
        Long id,
        AnalysisStatus status,
        Long cvId,
        String jobLink
) {
}

