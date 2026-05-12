package hr.team16.booksy.controller;

import hr.team16.booksy.dto.ReservationRequest;
import hr.team16.booksy.dto.ReservationResponse;
import hr.team16.booksy.dto.StatusRequest;
import hr.team16.booksy.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservation")
public class ReservationController {

    private final ReservationService reservationService;


    @GetMapping("/my")
    @Operation(summary = "My reservations",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReservationResponse>> getMyReservations(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(reservationService.getMyReservations(userId));
    }


    @GetMapping("/incoming")
    @Operation(summary = "Reservations for my properties",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<List<ReservationResponse>> getIncoming(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(reservationService.getForMyProperties(userId));
    }


    @GetMapping("/{id}")
    @Operation(summary = "Get reservation by ID",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReservationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getById(id));
    }


    @PostMapping
    @Operation(summary = "Create reservation",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReservationResponse> create(
            @RequestBody ReservationRequest request,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(reservationService.createReservation(request, userId));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update reservation status",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReservationResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody StatusRequest request,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(reservationService.updateStatus(id, request.getStatus(), userId));
    }

    @GetMapping("/room/{roomId}")
    @Operation(summary = "Update reservation status",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReservationResponse>> getReservationByRoomId(
            @PathVariable Long roomId ){
        return ResponseEntity.ok(reservationService.getReservationByRoomId(roomId));
    }




    @DeleteMapping("/{id}")
    @Operation(summary = "Delete reservation",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        reservationService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}