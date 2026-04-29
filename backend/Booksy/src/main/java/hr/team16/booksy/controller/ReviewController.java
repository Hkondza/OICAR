package hr.team16.booksy.controller;

import hr.team16.booksy.dto.ReviewRequest;
import hr.team16.booksy.dto.ReviewResponse;
import hr.team16.booksy.service.ReviewService;
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
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Review")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/property/{propertyId}")
    @Operation(summary = "Get reviews for a property")
    public ResponseEntity<List<ReviewResponse>> getByProperty(
            @PathVariable Long propertyId) {
        return ResponseEntity.ok(reviewService.getByProperty(propertyId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get review by ID")
    public ResponseEntity<ReviewResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Create review",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewResponse> create(
            @RequestBody ReviewRequest request,
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(reviewService.create(request, email));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete own review",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal String email) {
        reviewService.delete(id, email);
        return ResponseEntity.noContent().build();
    }
}