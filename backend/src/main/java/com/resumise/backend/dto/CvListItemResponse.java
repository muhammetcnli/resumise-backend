package com.resumise.backend.dto;

public record CvListItemResponse(
        Long id,
        String title,
        String fileName,
        String fileType,
        Long fileSize,
        boolean isDefault
) {
}
