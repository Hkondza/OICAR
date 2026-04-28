package hr.team16.booksy.controller;

import hr.team16.booksy.dto.ReservationRequest;
import hr.team16.booksy.dto.StatusRequest;
import hr.team16.booksy.model.Reservation;
import hr.team16.booksy.model.User;
import hr.team16.booksy.service.ReservationService;
import hr.team16.booksy.service.UserService;
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

    private final UserService userService;
    private final ReservationService reservationService;

    @GetMapping("/my")
    @Operation(summary = "My reservations (guest)", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("isAuthenticated()")
    public List<Reservation> getMyReservations(@AuthenticationPrincipal String email) {
        User user = userService.getByEmail(email);
        return reservationService.getMyReservations(user);
    }

    @GetMapping("/incoming")
    @Operation(summary = "Reservations for my properties (host)", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    public List<Reservation> getIncoming(@AuthenticationPrincipal String email) {
        User user = userService.getByEmail(email);
        return reservationService.getForMyProperties(user);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reservation by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("isAuthenticated()")
    public Reservation getById(@PathVariable Long id) {
        return reservationService.getById(id);
    }

    @PostMapping
    @Operation(summary = "Create reservation", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("isAuthenticated()")
    public Reservation create(@RequestBody ReservationRequest req, @AuthenticationPrincipal String email) {
        User user = userService.getByEmail(email);
        return reservationService.create(req, user);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update reservation status", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("isAuthenticated()")
    public Reservation updateStatus(@PathVariable Long id, @RequestBody StatusRequest req,
                                    @AuthenticationPrincipal String email) {
        User user = userService.getByEmail(email);
        return reservationService.updateStatus(id, req.getStatus(), user);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete reservation", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal String email) {
        User user = userService.getByEmail(email);
        reservationService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}