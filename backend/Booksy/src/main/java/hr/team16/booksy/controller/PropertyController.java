package hr.team16.booksy.controller;

import hr.team16.booksy.dto.PropertyRequest;
import hr.team16.booksy.dto.PropertyResponse;
import hr.team16.booksy.service.PropertyService;
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
@Tag(name = "Property")
public class PropertyController {

    private final PropertyService propertyService;

    @GetMapping
    @Operation(summary = "List all approved properties")
    public List<PropertyResponse> getAll() {
        return propertyService.getApprovedProperties();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get property by ID")
    public ResponseEntity<PropertyResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(propertyService.getById(id));
    }

    @PostMapping("/submit")
    @Operation(summary = "Submit property for approval",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<PropertyResponse> submit(
            @RequestBody PropertyRequest request,
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(propertyService.submitProperty(request, email));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update property",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<PropertyResponse> update(
            @PathVariable Long id,
            @RequestBody PropertyRequest request,
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(propertyService.update(id, request, email));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete property",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal String email) {
        propertyService.delete(id, email);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    @Operation(summary = "Get my properties",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<PropertyResponse>> getMine(
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(propertyService.getOwnerProperties(email));
    }

    @GetMapping("/pending")
    @Operation(summary = "Admin: list pending requests",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PropertyResponse>> getPending() {
        return ResponseEntity.ok(propertyService.getPendingRequests());
    }

    @PatchMapping("/{id}/approve")
    @Operation(summary = "Admin: approve property",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PropertyResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(propertyService.approveProperty(id));
    }

    @PatchMapping("/{id}/deny")
    @Operation(summary = "Admin: deny property",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PropertyResponse> deny(@PathVariable Long id) {
        return ResponseEntity.ok(propertyService.denyProperty(id));
    }
}