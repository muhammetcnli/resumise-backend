package com.resumise.backend.repository;

import com.resumise.backend.model.AnalysisRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnalysisRequestRepository extends JpaRepository<AnalysisRequest, Long> {

    Optional<AnalysisRequest> findByIdAndUserId(Long id, Long userId);

    List<AnalysisRequest> findAllByUserIdOrderByIdDesc(Long userId);
}

