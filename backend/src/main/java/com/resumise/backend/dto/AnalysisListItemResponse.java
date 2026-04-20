package com.resumise.backend.dto;

import com.resumise.backend.model.AnalysisStatus;

public record AnalysisListItemResponse(
        Long id,
        AnalysisStatus status,
        Long cvId,
        String cvTitle,
        String jobLink,
        String jobDescription,
        Integer matchScore
) {
}

