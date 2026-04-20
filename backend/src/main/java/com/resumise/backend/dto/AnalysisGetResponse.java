package com.resumise.backend.dto;

import com.resumise.backend.model.AnalysisStatus;

public record AnalysisGetResponse(
        Long id,
        AnalysisStatus status,
        Long cvId,
        String cvTitle,
        String jobLink,
        String jobDescription,
        String correlationId,
        String errorMessage,
        Integer matchScore,
        String summary,
        String strengths,
        String gaps,
        String actionItems
) {
}

