package com.resumise.backend.dto;

public record CvUploadResponse(
        Long id,
        String fileName,
        String fileType,
        Long fileSize,
        String title
) {
}
