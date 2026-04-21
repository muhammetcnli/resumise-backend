package com.resumise.backend.dto;

import com.resumise.backend.model.AnalysisStatus;

public record AnalysisListItemResponse(
        Long id,
        AnalysisStatus status,
        Long cvId,
        String cvTitle,
        String jobDescription,
        Integer matchScore
) {
}

