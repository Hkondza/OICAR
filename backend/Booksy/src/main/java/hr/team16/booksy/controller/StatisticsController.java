package hr.team16.booksy.controller;

import hr.team16.booksy.dto.AdminStatsResponse;
import hr.team16.booksy.dto.GuestStatsResponse;
import hr.team16.booksy.dto.OwnerStatsResponse;
import hr.team16.booksy.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@Tag(name = "Statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/guest")
    @Operation(summary = "Guest statistics", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GuestStatsResponse> getGuestStats(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(statisticsService.getGuestStats(userId));
    }

    @GetMapping("/owner")
    @Operation(summary = "Owner statistics", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<OwnerStatsResponse> getOwnerStats(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(statisticsService.getOwnerStats(userId));
    }

    @GetMapping("/admin")
    @Operation(summary = "Admin statistics", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminStatsResponse> getAdminStats() {
        return ResponseEntity.ok(statisticsService.getAdminStats());
    }
}
