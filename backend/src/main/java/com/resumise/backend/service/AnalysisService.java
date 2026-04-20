package com.resumise.backend.service;

import com.resumise.backend.dto.AnalysisCreateRequest;
import com.resumise.backend.dto.AnalysisGetResponse;
import com.resumise.backend.dto.AnalysisListItemResponse;
import com.resumise.backend.mapper.AnalysisMapper;
import com.resumise.backend.model.AnalysisRequest;
import com.resumise.backend.model.AnalysisResult;
import com.resumise.backend.model.AnalysisStatus;
import com.resumise.backend.model.Cv;
import com.resumise.backend.model.JobPosting;
import com.resumise.backend.model.User;
import com.resumise.backend.repository.AnalysisRequestRepository;
import com.resumise.backend.repository.AnalysisResultRepository;
import com.resumise.backend.repository.CvRepository;
import com.resumise.backend.repository.JobPostingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class AnalysisService {

    private final AuthProvisioningService authProvisioningService;
    private final CvRepository cvRepository;
    private final JobPostingRepository jobPostingRepository;
    private final AnalysisRequestRepository analysisRequestRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final AiAnalysisClient aiAnalysisClient;
    private final AnalysisMapper analysisMapper;

    public AnalysisService(AuthProvisioningService authProvisioningService,
                           CvRepository cvRepository,
                           JobPostingRepository jobPostingRepository,
                           AnalysisRequestRepository analysisRequestRepository,
                           AnalysisResultRepository analysisResultRepository,
                           AiAnalysisClient aiAnalysisClient,
                           AnalysisMapper analysisMapper) {
        this.authProvisioningService = authProvisioningService;
        this.cvRepository = cvRepository;
        this.jobPostingRepository = jobPostingRepository;
        this.analysisRequestRepository = analysisRequestRepository;
        this.analysisResultRepository = analysisResultRepository;
        this.aiAnalysisClient = aiAnalysisClient;
        this.analysisMapper = analysisMapper;
    }

    public AnalysisGetResponse createAnalysis(Authentication authentication, AnalysisCreateRequest request) {
        User user = requireUser(authentication);
        Cv cv = findUserCv(user.getId(), request.cvId());

        JobPosting jobPosting = buildJobPosting(
                user,
                request.jobDescription(),
                request.jobLink(),
                request.jobDescription()
        );
        jobPostingRepository.save(jobPosting);

        AnalysisRequest analysisRequest = new AnalysisRequest();
        analysisRequest.setUser(user);
        analysisRequest.setCv(cv);
        analysisRequest.setJobPosting(jobPosting);
        analysisRequest.setStatus(AnalysisStatus.QUEUED);
        analysisRequest.setCorrelationId(UUID.randomUUID().toString());
        analysisRequest = analysisRequestRepository.save(analysisRequest);

        try {
            analysisRequest.setStatus(AnalysisStatus.RUNNING);
            analysisRequest = analysisRequestRepository.save(analysisRequest);

            AiAnalysisClient.AiAnalysisPayload payload = aiAnalysisClient.analyze(cv, jobPosting);

            AnalysisResult result = new AnalysisResult();
            result.setAnalysisRequest(analysisRequest);
            result.setMatchScore(payload.matchScore());
            result.setSummary(payload.summary());
            result.setStrengths(payload.strengths());
            result.setGaps(payload.gaps());
            result.setActionItems(payload.actionItems());
            result.setRawResponse(payload.rawResponse());
            analysisResultRepository.save(result);

            analysisRequest.setAnalysisResult(result);
            analysisRequest.setStatus(AnalysisStatus.DONE);
            analysisRequest.setErrorMessage(null);
            analysisRequest = analysisRequestRepository.save(analysisRequest);

            return analysisMapper.toGetResponse(analysisRequest);
        } catch (ResponseStatusException ex) {
            analysisRequest.setStatus(AnalysisStatus.FAILED);
            analysisRequest.setErrorMessage(ex.getReason() != null ? ex.getReason() : "AI analysis failed");
            analysisRequestRepository.save(analysisRequest);
            throw ex;
        } catch (RuntimeException ex) {
            analysisRequest.setStatus(AnalysisStatus.FAILED);
            analysisRequest.setErrorMessage("AI analysis failed");
            analysisRequestRepository.save(analysisRequest);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI analysis failed", ex);
        }
    }

    @Transactional(readOnly = true)
    public List<AnalysisListItemResponse> listAnalyses(Authentication authentication) {
        User user = requireUser(authentication);

        return analysisRequestRepository.findAllByUserIdOrderByIdDesc(user.getId())
                .stream()
                .map(analysisMapper::toListItemResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AnalysisGetResponse getAnalysis(Authentication authentication, Long analysisId) {
        User user = requireUser(authentication);
        AnalysisRequest request = findUserAnalysisRequest(user.getId(), analysisId);
        return analysisMapper.toGetResponse(request);
    }


    private User requireUser(Authentication authentication) {
        return authProvisioningService.resolveAuthenticatedUser(authentication);
    }

    private Cv findUserCv(Long userId, Long cvId) {
        if (cvId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cvId is required");
        }

        return cvRepository.findByIdAndUserId(cvId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CV not found"));
    }

    private AnalysisRequest findUserAnalysisRequest(Long userId, Long analysisId) {
        return analysisRequestRepository.findByIdAndUserId(analysisId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Analysis request not found"));
    }

    private JobPosting buildJobPosting(
            User user,
            String jobDescription,
            String jobLink,
            String notes
    ) {
        if (!StringUtils.hasText(jobDescription)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "jobDescription is required");
        }

        String resolvedJobLink = StringUtils.hasText(jobLink) ? jobLink.trim() : jobDescription.trim();

        JobPosting jobPosting = new JobPosting();
        jobPosting.setUser(user);
        jobPosting.setJobLink(resolvedJobLink);
        jobPosting.setNormalizedJobLink(normalizeJobLink(resolvedJobLink));
        jobPosting.setCompanyName(null);
        jobPosting.setPositionName(null);
        jobPosting.setNotes(normalizeOptional(notes));
        return jobPosting;
    }

    private String normalizeJobLink(String jobLink) {
        return jobLink.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeOptional(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}

