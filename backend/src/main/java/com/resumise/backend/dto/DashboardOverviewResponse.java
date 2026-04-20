package com.resumise.backend.dto;

public record DashboardOverviewResponse(
        long totalCvs,
        Long defaultCvId,
        String defaultCvTitle,
        long totalAnalyses,
        long completedAnalyses,
        Integer latestMatchScore
) {
}

