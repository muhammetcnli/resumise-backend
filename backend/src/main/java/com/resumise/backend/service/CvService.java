package com.resumise.backend.service;

import com.resumise.backend.dto.CvListItemResponse;
import com.resumise.backend.model.Cv;
import com.resumise.backend.model.User;
import com.resumise.backend.repository.AnalysisRequestRepository;
import com.resumise.backend.repository.CvRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class CvService {

    private static final Path UPLOAD_ROOT = Paths.get("uploads", "cvs");

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private final CvRepository cvRepository;
    private final AnalysisRequestRepository analysisRequestRepository;
    private final AuthProvisioningService authProvisioningService;

    public CvService(CvRepository cvRepository,
                     AnalysisRequestRepository analysisRequestRepository,
                     AuthProvisioningService authProvisioningService) {
        this.cvRepository = cvRepository;
        this.analysisRequestRepository = analysisRequestRepository;
        this.authProvisioningService = authProvisioningService;
    }

    public Cv save(Authentication authentication, MultipartFile file, String title) {

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is required");
        }

        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported file type");
        }

        User user = requireUser(authentication);
        boolean isFirstCvForUser = !cvRepository.existsByUserId(user.getId());

        String fileName = StringUtils.hasText(file.getOriginalFilename())
                ? Paths.get(file.getOriginalFilename()).getFileName().toString()
                : "uploaded-cv";
        String resolvedTitle = StringUtils.hasText(title) ? title.trim() : fileName;
        String storedFileName = System.currentTimeMillis() + "-" + fileName;
        Path userDir = UPLOAD_ROOT.resolve("user-" + user.getId());
        Path targetPath = userDir.resolve(storedFileName);

        try {
            Files.createDirectories(userDir);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "File could not be stored", e);
        }

        Cv cv = Cv.builder()
                .fileName(fileName)
                .filePath(targetPath.toString())
                .fileType(contentType)
                .fileSize(file.getSize())
                .title(resolvedTitle)
                .user(user)
                .isDefault(isFirstCvForUser)
                .build();

        return cvRepository.save(cv);
    }

    // bak
    public List<CvListItemResponse> listCvs(Authentication authentication) {
        User user = requireUser(authentication);

        return cvRepository.findAllByUserIdOrderByIdDesc(user.getId())
                .stream()
                .map(cv -> new CvListItemResponse(
                        cv.getId(),
                        cv.getTitle(),
                        cv.getFileName(),
                        cv.getFileType(),
                        cv.getFileSize(),
                        cv.isDefault()
                ))
                .toList();
    }

    public Cv getCv(Authentication authentication, Long cvId) {
        User user = requireUser(authentication);

        return cvRepository.findByIdAndUserId(cvId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CV not found"));
    }

    public Cv getDefaultCv(Authentication authentication) {
        User user = requireUser(authentication);

        return cvRepository.findByUserIdAndIsDefaultTrue(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Default CV not found"));
    }

    public byte[] getCvContent(Authentication authentication, Long cvId) {
        Cv cv = getCv(authentication, cvId);
        Path filePath = Paths.get(cv.getFilePath());

        if (!Files.exists(filePath)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CV file not found");
        }

        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "CV file could not be read", e);
        }
    }

    public byte[] getDefaultCvContent(Authentication authentication) {
        Cv cv = getDefaultCv(authentication);
        Path filePath = Paths.get(cv.getFilePath());

        if (!Files.exists(filePath)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Default CV file not found");
        }

        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Default CV file could not be read", e);
        }
    }

    public Cv updateCvTitle(Authentication authentication, Long cvId, String title) {
        if (!StringUtils.hasText(title)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title is required");
        }

        Cv cv = getCv(authentication, cvId);
        cv.setTitle(title.trim());
        return cvRepository.save(cv);
    }

    @Transactional
    public Cv setDefaultCv(Authentication authentication, Long cvId) {
        Cv cv = getCv(authentication, cvId);
        Long userId = cv.getUser().getId();

        cvRepository.clearDefaultByUserId(userId);
        cv.setDefault(true);
        return cvRepository.save(cv);
    }

    public void deleteCv(Authentication authentication, Long cvId) {
        Cv cv = getCv(authentication, cvId);
        Long userId = cv.getUser().getId();

        boolean hasLinkedAnalyses = analysisRequestRepository.existsByCv_IdAndUser_Id(cvId, userId);
        if (hasLinkedAnalyses) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "CV is used by existing analyses. Delete analyses first or upload a new CV."
            );
        }

        Path filePath = Paths.get(cv.getFilePath());

        cvRepository.delete(cv);

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "CV file could not be deleted", e);
        }
    }

    private User requireUser(Authentication authentication) {
        return authProvisioningService.resolveAuthenticatedUser(authentication);
    }
}
