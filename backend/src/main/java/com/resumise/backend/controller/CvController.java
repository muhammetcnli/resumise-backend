package com.resumise.backend.controller;

import com.resumise.backend.dto.CvGetResponse;
import com.resumise.backend.dto.CvUploadResponse;
import com.resumise.backend.dto.CvListItemResponse;
import com.resumise.backend.dto.CvUpdateRequest;
import com.resumise.backend.mapper.CvMapper;
import com.resumise.backend.model.Cv;
import com.resumise.backend.service.CvService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cvs")
@Validated
public class CvController {

    private final CvService cvService;
    private final CvMapper cvMapper;

    public CvController(CvService cvService, CvMapper cvMapper) {
        this.cvService = cvService;
        this.cvMapper = cvMapper;
    }

    /**
     * Uploads a CV file for the authenticated user and returns the saved CV metadata.
     *
     * @param authentication authenticated user details
     * @param file CV file to upload (pdf/doc/docx)
     * @param title optional CV title; if blank, file name is used
     * @return {@code 201 Created} with saved CV metadata in the response body ({@link CvUploadResponse})
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CvUploadResponse> uploadCv(
            Authentication authentication,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false)
            @Size(max = 120, message = "title must be at most 120 characters") String title
    ) {
        Cv savedCv = cvService.save(authentication, file, title);

        CvUploadResponse response = new CvUploadResponse(
                savedCv.getId(),
                savedCv.getFileName(),
                savedCv.getFileType(),
                savedCv.getFileSize(),
                savedCv.getTitle()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Returns the list of CVs owned by the authenticated user.
     *
     * @param authentication authenticated user details
     * @return {@code 200 OK} with the user's CV summary list in the response body ({@link CvListItemResponse})
     */
    @GetMapping("/list")
    public ResponseEntity<List<CvListItemResponse>> listMyCvs(
            Authentication authentication
    ) {
        // get cv list in cv service
        List<CvListItemResponse> items = cvService.listCvs(authentication);
        return ResponseEntity.ok(items);
    }

    /**
     * Returns metadata for the given CV id.
     *
     * @param authentication authenticated user details
     * @param cvId id of the CV to retrieve
     * @return {@code 200 OK} with CV metadata in the response body ({@link CvGetResponse})
     */
    @GetMapping("/{cvId}")
    public ResponseEntity<CvGetResponse> getCv(
            Authentication authentication,
            @PathVariable @Positive(message = "cvId must be positive") Long cvId
    ) {
        Cv cv = cvService.getCv(authentication, cvId);
        return ResponseEntity.ok(cvMapper.toGetResponse(cv));
    }

    /**
     * Returns raw file content for the given CV id with the appropriate content type.
     *
     * @param authentication authenticated user details
     * @param cvId id of the CV file to read/download
     * @return {@code 200 OK} with CV file content in the response body ({@code byte[]})
     */
    @GetMapping("/{cvId}/content")
    public ResponseEntity<byte[]> getCvContent(
            Authentication authentication,
            @PathVariable @Positive(message = "cvId must be positive") Long cvId
    ) {
        Cv cv = cvService.getCv(authentication, cvId);
        byte[] content = cvService.getCvContent(authentication, cvId);

        return buildCvContentResponse(cv, content);
    }

    /**
     * Returns metadata for the authenticated user's default CV.
     *
     * @param authentication authenticated user details
     * @return {@code 200 OK} with default CV metadata in the response body ({@link CvGetResponse})
     */
    @GetMapping("/default")
    public ResponseEntity<CvGetResponse> getDefaultCv(
            Authentication authentication
    ) {
        Cv cv = cvService.getDefaultCv(authentication);
        return ResponseEntity.ok(cvMapper.toGetResponse(cv));
    }

    /**
     * Returns raw file content for the authenticated user's default CV.
     *
     * @param authentication authenticated user details
     * @return {@code 200 OK} with default CV file content in the response body ({@code byte[]})
     */
    @GetMapping("/default/content")
    public ResponseEntity<byte[]> getDefaultCvContent(
            Authentication authentication
    ) {
        Cv cv = cvService.getDefaultCv(authentication);
        byte[] content = cvService.getDefaultCvContent(authentication);

        return buildCvContentResponse(cv, content);
    }

    private ResponseEntity<byte[]> buildCvContentResponse(Cv cv, byte[] content) {
        MediaType mediaType = StringUtils.hasText(cv.getFileType())
                ? MediaType.parseMediaType(cv.getFileType())
                : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + cv.getFileName() + "\"")
                .body(content);
    }

    /**
     * Updates the title of the given CV.
     *
     * @param authentication authenticated user details
     * @param cvId id of the CV to update
     * @param request update payload (title)
     * @return {@code 200 OK} with updated CV metadata in the response body ({@link CvGetResponse})
     */
    @PatchMapping("/{cvId}")
    public ResponseEntity<CvGetResponse> updateCv(
            Authentication authentication,
            @PathVariable @Positive(message = "cvId must be positive") Long cvId,
            @Valid @RequestBody CvUpdateRequest request
    ) {
        Cv updatedCv = cvService.updateCvTitle(authentication, cvId, request.title());
        return ResponseEntity.ok(cvMapper.toGetResponse(updatedCv));
    }

    /**
     * Marks the given CV as the authenticated user's default CV.
     *
     * @param authentication authenticated user details
     * @param cvId id of the CV to set as default
     * @return {@code 200 OK} with current CV metadata in the response body ({@link CvGetResponse})
     */
    @PostMapping("/{cvId}/default")
    public ResponseEntity<CvGetResponse> setDefaultCv(
            Authentication authentication,
            @PathVariable @Positive(message = "cvId must be positive") Long cvId
    ) {
        Cv updatedCv = cvService.setDefaultCv(authentication, cvId);
        return ResponseEntity.ok(cvMapper.toGetResponse(updatedCv));
    }

    /**
     * Deletes the given CV record and its associated file when present.
     *
     * @param authentication authenticated user details
     * @param cvId id of the CV to delete
     * @return {@code 204 No Content}
     */
    @DeleteMapping("/{cvId}")
    public ResponseEntity<Void> deleteCv(
            Authentication authentication,
            @PathVariable @Positive(message = "cvId must be positive") Long cvId
    ) {
        cvService.deleteCv(authentication, cvId);
        return ResponseEntity.noContent().build();
    }


}
