package hr.team16.booksy.controller;

import hr.team16.booksy.dto.ReviewRequest;
import hr.team16.booksy.model.Review;
import hr.team16.booksy.model.User;
import hr.team16.booksy.service.ReviewService;
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
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Review")
public class ReviewController {

    private final UserService userService;
    private final ReviewService reviewService;

    @GetMapping("/property/{propertyId}")
    @Operation(summary = "Get reviews for a property")
    public List<Review> getByProperty(@PathVariable Long propertyId) {
        return reviewService.getByProperty(propertyId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get review by ID")
    public Review getById(@PathVariable Long id) {
        return reviewService.getById(id);
    }

    @PostMapping
    @Operation(summary = "Create review", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("isAuthenticated()")
    public Review create(@RequestBody ReviewRequest req, @AuthenticationPrincipal String email) {
        User user = userService.getByEmail(email);
        return reviewService.create(req, user);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete own review", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal String email) {
        User user = userService.getByEmail(email);
        reviewService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
