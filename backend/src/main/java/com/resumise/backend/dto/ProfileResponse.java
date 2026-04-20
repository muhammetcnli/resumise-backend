package com.resumise.backend.dto;

public record ProfileResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String profileImageUrl,
        String headline,
        String location,
        String phone,
        String linkedinUrl,
        String githubUrl,
        String professionalSummary,
        String targetRoles,
        boolean notifyHighMatch,
        boolean notifyNewsletter,
        boolean notifyInterviewReminders
) {
}

