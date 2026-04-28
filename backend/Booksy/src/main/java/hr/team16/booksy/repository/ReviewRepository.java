package hr.team16.booksy.repository;

import hr.team16.booksy.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByReservationId(Long reservationId);
    List<Review> findByReservationRoomPropertyId(Long propertyId);
}
