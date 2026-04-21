package com.resumise.backend.service;

import com.resumise.backend.dto.DashboardOverviewResponse;
import com.resumise.backend.model.AnalysisRequest;
import com.resumise.backend.model.AnalysisStatus;
import com.resumise.backend.model.Cv;
import com.resumise.backend.model.User;
import com.resumise.backend.repository.AnalysisRequestRepository;
import com.resumise.backend.repository.CvRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DashboardService {

    private final AuthProvisioningService authProvisioningService;
    private final CvRepository cvRepository;
    private final AnalysisRequestRepository analysisRequestRepository;

    public DashboardService(AuthProvisioningService authProvisioningService,
                            CvRepository cvRepository,
                            AnalysisRequestRepository analysisRequestRepository) {
        this.authProvisioningService = authProvisioningService;
        this.cvRepository = cvRepository;
        this.analysisRequestRepository = analysisRequestRepository;
    }

    @Transactional(readOnly = true)
    public DashboardOverviewResponse getOverview(Authentication authentication) {
        User user = requireUser(authentication);

        long totalCvs = cvRepository.countByUserId(user.getId());
        Cv defaultCv = cvRepository.findByUserIdAndIsDefaultTrue(user.getId()).orElse(null);

        long totalAnalyses = analysisRequestRepository.countByUserId(user.getId());
        long completedAnalyses = analysisRequestRepository.countByUserIdAndStatus(user.getId(), AnalysisStatus.DONE);

        List<AnalysisRequest> analyses = analysisRequestRepository.findAllByUserIdOrderByIdDesc(user.getId());
        Integer latestMatchScore = analyses.stream()
                .map(AnalysisRequest::getAnalysisResult)
                .filter(result -> result != null)
                .map(result -> result.getMatchScore())
                .findFirst()
                .orElse(null);

        return new DashboardOverviewResponse(
                totalCvs,
                defaultCv != null ? defaultCv.getId() : null,
                defaultCv != null ? defaultCv.getTitle() : null,
                totalAnalyses,
                completedAnalyses,
                latestMatchScore
        );
    }

    private User requireUser(Authentication authentication) {
        return authProvisioningService.resolveAuthenticatedUser(authentication);
    }
}

