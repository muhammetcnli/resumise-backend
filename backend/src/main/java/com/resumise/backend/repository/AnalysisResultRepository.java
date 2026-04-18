package com.resumise.backend.repository;

import com.resumise.backend.model.AnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {

    Optional<AnalysisResult> findByAnalysisRequestIdAndAnalysisRequestUserId(Long analysisRequestId, Long userId);
}

