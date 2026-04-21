package com.resumise.backend.mapper;

import com.resumise.backend.dto.AnalysisGetResponse;
import com.resumise.backend.dto.AnalysisListItemResponse;
import com.resumise.backend.model.AnalysisRequest;
import com.resumise.backend.model.AnalysisResult;
import org.springframework.stereotype.Component;

@Component
public class AnalysisMapper {

    public AnalysisListItemResponse toListItemResponse(AnalysisRequest request) {
        Integer matchScore = request.getAnalysisResult() != null ? request.getAnalysisResult().getMatchScore() : null;

        return new AnalysisListItemResponse(
                request.getId(),
                request.getStatus(),
                request.getCv().getId(),
                request.getCv().getTitle(),
                request.getJobPosting().getNotes(),
                matchScore
        );
    }

    public AnalysisGetResponse toGetResponse(AnalysisRequest request) {
        AnalysisResult result = request.getAnalysisResult();

        return new AnalysisGetResponse(
                request.getId(),
                request.getStatus(),
                request.getCv().getId(),
                request.getCv().getTitle(),
                request.getJobPosting().getNotes(),
                request.getCorrelationId(),
                request.getErrorMessage(),
                result != null ? result.getMatchScore() : null,
                result != null ? result.getSummary() : null,
                result != null ? result.getStrengths() : null,
                result != null ? result.getGaps() : null,
                result != null ? result.getActionItems() : null
        );
    }
}

