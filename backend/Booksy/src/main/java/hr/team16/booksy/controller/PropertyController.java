package hr.team16.booksy.controller;

import hr.team16.booksy.dto.PropertyRequest;
import hr.team16.booksy.dto.StatusRequest;
import hr.team16.booksy.model.Property;
import hr.team16.booksy.model.User;
import hr.team16.booksy.model.UserProperty;
import hr.team16.booksy.service.PropertyService;
import hr.team16.booksy.service.UserPropertyService;
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
@RequestMapping("/api/properties")
@RequiredArgsConstructor
@Tag(name="Propety")
public class PropertyController {

    private final UserService userService;
    private final PropertyService propertyService;
    private final UserPropertyService userPropertyService;

    @GetMapping
    @Operation(summary = "List all properties")
    public List<Property> getAll() {
        return propertyService.getAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get property by ID")
    public Property getById(@PathVariable Long id) {
        return propertyService.getById(id);
    }

    @PostMapping
    @Operation(summary = "Create property", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    public Property create(@RequestBody PropertyRequest req, @AuthenticationPrincipal String email) {
        User user = userService.getByEmail(email);
        return propertyService.create(req, user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update property", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    public Property update(@PathVariable Long id, @RequestBody PropertyRequest req,
                           @AuthenticationPrincipal String email) {
        User user = userService.getByEmail(email);
        return propertyService.update(id, req, user);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete property", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal String email) {
        User user = userService.getByEmail(email);
        propertyService.delete(id, user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/request-access")
    @Operation(summary = "Request HOST access for a property", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("isAuthenticated()")
    public UserProperty requestAccess(@PathVariable Long id, @AuthenticationPrincipal String email) {
        User user = userService.getByEmail(email);
        return userPropertyService.requestAccess(id, user);
    }

    @GetMapping("/access-requests")
    @Operation(summary = "Admin: list pending access requests", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserProperty> getPending() {
        return userPropertyService.getPending();
    }

    @PatchMapping("/access-requests/{id}")
    @Operation(summary = "Admin: approve or reject access request", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public UserProperty updateAccessStatus(@PathVariable Long id, @RequestBody StatusRequest req) {
        return userPropertyService.updateStatus(id, req.getStatus());
    }
}
