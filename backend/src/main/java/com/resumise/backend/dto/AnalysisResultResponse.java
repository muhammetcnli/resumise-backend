package com.resumise.backend.dto;

public record AnalysisResultResponse(
        Integer matchScore,
        String summary,
        String strengths,
        String gaps,
        String actionItems
) {
}

