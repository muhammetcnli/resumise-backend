package com.resumise.backend.service;

import com.resumise.backend.model.Cv;
import com.resumise.backend.model.JobPosting;

public interface AiAnalysisClient {

    AiAnalysisPayload analyze(Cv cv, JobPosting jobPosting);

    record AiAnalysisPayload(
            int matchScore,
            String summary,
            String strengths,
            String gaps,
            String actionItems,
            String rawResponse
    ) {
    }
}

