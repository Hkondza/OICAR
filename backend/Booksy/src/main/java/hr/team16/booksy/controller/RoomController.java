package hr.team16.booksy.controller;

import hr.team16.booksy.dto.RoomRequest;
import hr.team16.booksy.dto.RoomResponse;
import hr.team16.booksy.service.RoomService;
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
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Tag(name = "Room")
public class RoomController {

    private final RoomService roomService;


    @GetMapping
    @Operation(summary = "List all rooms")
    public ResponseEntity<List<RoomResponse>> getAll() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    @GetMapping("/property/{propertyId}")
    @Operation(summary = "List rooms by property")
    public ResponseEntity<List<RoomResponse>> getByProperty(@PathVariable Long propertyId) {
        return ResponseEntity.ok(roomService.getRoomsByProperty(propertyId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get room by ID")
    public ResponseEntity<RoomResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    @PostMapping("/property/{propertyId}")
    @Operation(summary = "Create room",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<RoomResponse> create(
            @PathVariable Long propertyId,
            @RequestBody RoomRequest request,
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(roomService.addRoom(propertyId, request, email));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update room",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<RoomResponse> update(
            @PathVariable Long id,
            @RequestBody RoomRequest request,
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(roomService.updateRoom(id, request, email));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete room",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal String email) {
        roomService.deleteRoom(id, email);
        return ResponseEntity.noContent().build();
    }
}