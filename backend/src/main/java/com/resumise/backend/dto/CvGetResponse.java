package com.resumise.backend.dto;

public record CvGetResponse(
        Long id,
        String title,
        String fileName,
        String fileType,
        Long fileSize,
        boolean isDefault
) {
}

