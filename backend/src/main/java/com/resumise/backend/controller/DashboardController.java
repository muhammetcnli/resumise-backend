package com.resumise.backend.controller;

import com.resumise.backend.dto.DashboardOverviewResponse;
import com.resumise.backend.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Returns dashboard counters and latest score cards for the authenticated user.
     */
    @GetMapping("/overview")
    public ResponseEntity<DashboardOverviewResponse> getOverview(
            Authentication authentication
    ) {
        return ResponseEntity.ok(dashboardService.getOverview(authentication));
    }
}

