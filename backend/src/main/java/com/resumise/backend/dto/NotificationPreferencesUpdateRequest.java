package com.resumise.backend.dto;

public record NotificationPreferencesUpdateRequest(
        Boolean notifyHighMatch,
        Boolean notifyNewsletter,
        Boolean notifyInterviewReminders
) {
}

