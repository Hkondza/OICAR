package hr.team16.booksy.controller;

import hr.team16.booksy.dto.RoomRequest;
import hr.team16.booksy.model.Room;
import hr.team16.booksy.model.User;
import hr.team16.booksy.service.RoomService;
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
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Tag(name = "Room")
public class RoomController {

    private final UserService userService;
    private final RoomService roomService;

    @GetMapping
    @Operation(summary = "List all rooms")
    public List<Room> getAll() { return roomService.getAll(); }

    @GetMapping("/property/{propertyId}")
    @Operation(summary = "List rooms by property")
    public List<Room> getByProperty(@PathVariable Long propertyId) {
        return roomService.getByProperty(propertyId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get room by ID")
    public Room getById(@PathVariable Long id) { return roomService.getById(id); }

    @PostMapping
    @Operation(summary = "Create room", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    public Room create(@RequestBody RoomRequest req, @AuthenticationPrincipal String email) {
        User user = userService.getByEmail(email);
        return roomService.create(req, user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update room", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    public Room update(@PathVariable Long id, @RequestBody RoomRequest req,
                       @AuthenticationPrincipal String email) {
        User user = userService.getByEmail(email);
        return roomService.update(id, req, user);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete room", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal String email) {
        User user = userService.getByEmail(email);
        roomService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
