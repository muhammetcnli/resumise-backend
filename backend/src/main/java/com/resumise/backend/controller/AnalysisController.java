package com.resumise.backend.controller;

import com.resumise.backend.dto.AnalysisCreateRequest;
import com.resumise.backend.dto.AnalysisGetResponse;
import com.resumise.backend.dto.AnalysisListItemResponse;
import com.resumise.backend.service.AnalysisService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analyses")
@Validated
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    /**
     * Creates a new analysis request for the selected CV and job description.
     *
     * @param authentication authenticated user details
     * @param request payload containing cvId and jobDescription
     * @return {@code 201 Created} with created analysis detail ({@link AnalysisGetResponse})
     */
    @PostMapping
    public ResponseEntity<AnalysisGetResponse> createAnalysis(
            Authentication authentication,
            @Valid @RequestBody AnalysisCreateRequest request
    ) {
        AnalysisGetResponse response = analysisService.createAnalysis(authentication, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Returns all analysis requests owned by the authenticated user.
     *
     * @param authentication authenticated user details
     * @return {@code 200 OK} with analysis summary list ({@link AnalysisListItemResponse})
     */
    @GetMapping
    public ResponseEntity<List<AnalysisListItemResponse>> listAnalyses(
            Authentication authentication
    ) {
        List<AnalysisListItemResponse> items = analysisService.listAnalyses(authentication);
        return ResponseEntity.ok(items);
    }

    /**
     * Returns detail for a single analysis request.
     *
     * @param authentication authenticated user details
     * @param analysisId analysis request id
     * @return {@code 200 OK} with analysis detail ({@link AnalysisGetResponse})
     */
    @GetMapping("/{analysisId}")
    public ResponseEntity<AnalysisGetResponse> getAnalysis(
            Authentication authentication,
            @PathVariable @Positive(message = "analysisId must be positive") Long analysisId
    ) {
        AnalysisGetResponse response = analysisService.getAnalysis(authentication, analysisId);
        return ResponseEntity.ok(response);
    }
}

