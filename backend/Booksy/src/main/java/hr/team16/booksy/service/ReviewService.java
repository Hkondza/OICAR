package hr.team16.booksy.service;



import hr.team16.booksy.dto.ReviewRequest;
import hr.team16.booksy.model.Reservation;
import hr.team16.booksy.model.Review;
import hr.team16.booksy.model.User;
import hr.team16.booksy.repository.ReservationRepository;
import hr.team16.booksy.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;

    public List<Review> getByProperty(Long propertyId) {
        return reviewRepository.findByReservationRoomPropertyId(propertyId);
    }

    public Review getById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));
    }

    public Review create(ReviewRequest req, User guest) {
        Reservation reservation = reservationRepository.findById(req.getReservationId())
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        if (!reservation.getGuest().getId().equals(guest.getId()))
            throw new RuntimeException("Forbidden");
        if (!reservation.getStatus().equals("COMPLETED"))
            throw new RuntimeException("Can only review completed reservations");
        if (reviewRepository.findByReservationId(req.getReservationId()).isPresent())
            throw new RuntimeException("Review already exists for this reservation");
        if (req.getRating() < 1 || req.getRating() > 5)
            throw new RuntimeException("Rating must be between 1 and 5");
        Review review = new Review();
        review.setReservation(reservation);
        review.setRating(req.getRating());
        review.setComment(req.getComment());
        return reviewRepository.save(review);
    }

    public void delete(Long id, User guest) {
        Review review = getById(id);
        if (!review.getReservation().getGuest().getId().equals(guest.getId()))
            throw new RuntimeException("Forbidden");
        reviewRepository.deleteById(id);
    }
}
