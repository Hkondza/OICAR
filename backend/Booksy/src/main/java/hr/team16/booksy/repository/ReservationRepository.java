package hr.team16.booksy.repository;

import hr.team16.booksy.model.Reservation;
import hr.team16.booksy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByGuest(User guest);
    List<Reservation> findByRoomPropertyOwnerId(Long ownerId);
}
