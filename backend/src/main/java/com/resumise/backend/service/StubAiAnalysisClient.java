package com.resumise.backend.service;

import com.resumise.backend.model.Cv;
import com.resumise.backend.model.JobPosting;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class StubAiAnalysisClient implements AiAnalysisClient {

    @Override
    public AiAnalysisPayload analyze(Cv cv, JobPosting jobPosting) {
        throw new ResponseStatusException(
                HttpStatus.NOT_IMPLEMENTED,
                "AI API integration is not configured yet"
        );
    }
}

