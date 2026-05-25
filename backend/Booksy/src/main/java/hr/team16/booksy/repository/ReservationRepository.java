package hr.team16.booksy.repository;

import hr.team16.booksy.model.Reservation;
import hr.team16.booksy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByGuest(User guest);
    List<Reservation> findByRoomId(Long roomId);
    List<Reservation> findByGuestId(Long guestId);
    List<Reservation> findByRoomPropertyOwnerId(Long ownerId);
    List<Reservation> findByRoomIdAndStatusNot(Long roomId, String status);

    long countByGuestIdAndStatus(Long guestId, String status);
    long countByRoomPropertyOwnerIdAndStatus(Long ownerId, String status);

    @Query("SELECT YEAR(r.createdAt), MONTH(r.createdAt), COUNT(r), COALESCE(SUM(r.totalPrice), 0) " +
           "FROM Reservation r WHERE r.guest.id = :guestId " +
           "GROUP BY YEAR(r.createdAt), MONTH(r.createdAt) ORDER BY YEAR(r.createdAt), MONTH(r.createdAt)")
    List<Object[]> findMonthlyStatsByGuest(@Param("guestId") Long guestId);

    @Query("SELECT YEAR(r.createdAt), MONTH(r.createdAt), COUNT(r), COALESCE(SUM(r.totalPrice), 0) " +
           "FROM Reservation r WHERE r.room.property.owner.id = :ownerId " +
           "GROUP BY YEAR(r.createdAt), MONTH(r.createdAt) ORDER BY YEAR(r.createdAt), MONTH(r.createdAt)")
    List<Object[]> findMonthlyStatsByOwner(@Param("ownerId") Long ownerId);

    @Query("SELECT YEAR(r.createdAt), MONTH(r.createdAt), COUNT(r), COALESCE(SUM(r.totalPrice), 0) " +
           "FROM Reservation r " +
           "GROUP BY YEAR(r.createdAt), MONTH(r.createdAt) ORDER BY YEAR(r.createdAt), MONTH(r.createdAt)")
    List<Object[]> findMonthlyStatsAll();

    long countByStatus(String status);
}