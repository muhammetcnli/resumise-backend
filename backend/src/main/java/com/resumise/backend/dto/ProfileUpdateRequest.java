package com.resumise.backend.dto;

import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @Size(max = 120, message = "firstName must be at most 120 characters") String firstName,
        @Size(max = 120, message = "lastName must be at most 120 characters") String lastName,
        @Size(max = 120, message = "headline must be at most 120 characters") String headline,
        @Size(max = 120, message = "location must be at most 120 characters") String location,
        @Size(max = 30, message = "phone must be at most 30 characters") String phone,
        @Size(max = 255, message = "linkedinUrl must be at most 255 characters") String linkedinUrl,
        @Size(max = 255, message = "githubUrl must be at most 255 characters") String githubUrl,
        @Size(max = 2500, message = "professionalSummary must be at most 2500 characters") String professionalSummary,
        @Size(max = 500, message = "targetRoles must be at most 500 characters") String targetRoles
) {
}

