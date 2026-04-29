package hr.team16.booksy.service;

import hr.team16.booksy.dto.ReviewRequest;
import hr.team16.booksy.dto.ReviewResponse;
import hr.team16.booksy.model.Reservation;
import hr.team16.booksy.model.Review;
import hr.team16.booksy.model.User;
import hr.team16.booksy.repository.ReservationRepository;
import hr.team16.booksy.repository.ReviewRepository;
import hr.team16.booksy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    public List<ReviewResponse> getByProperty(Long propertyId) {
        return reviewRepository.findByReservationRoomPropertyId(propertyId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ReviewResponse getById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        return mapToResponse(review);
    }

    public ReviewResponse create(ReviewRequest request, String email) {
        User guest = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Reservation reservation = reservationRepository.findById(request.getReservationId())
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        // Provjeri je li ovo gostova rezervacija
        if (!reservation.getGuest().getId().equals(guest.getId()))
            throw new RuntimeException("Forbidden");

        // Provjeri je li rezervacija završena
        if (!reservation.getStatus().equals("COMPLETED"))
            throw new RuntimeException("Can only review completed reservations");

        // Provjeri postoji li već recenzija
        if (reviewRepository.findByReservationId(request.getReservationId()).isPresent())
            throw new RuntimeException("Review already exists for this reservation");

        // Validiraj rating
        if (request.getRating() < 1 || request.getRating() > 5)
            throw new RuntimeException("Rating must be between 1 and 5");

        Review review = new Review();
        review.setReservation(reservation);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        reviewRepository.save(review);

        return mapToResponse(review);
    }

    public void delete(Long id, String email) {
        User guest = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (!review.getReservation().getGuest().getId().equals(guest.getId()))
            throw new RuntimeException("Forbidden");

        reviewRepository.deleteById(id);
    }

    private ReviewResponse mapToResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setReservationId(review.getReservation().getId());
        response.setPropertyName(review.getReservation().getRoom().getProperty().getName());
        response.setCity(review.getReservation().getRoom().getProperty().getCity());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setGuestEmail(review.getReservation().getGuest().getEmail());
        response.setCreatedAt(review.getCreatedAt());
        return response;
    }
}