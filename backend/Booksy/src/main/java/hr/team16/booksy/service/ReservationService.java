package hr.team16.booksy.service;

import hr.team16.booksy.dto.ReservationRequest;
import hr.team16.booksy.model.Reservation;
import hr.team16.booksy.model.Room;
import hr.team16.booksy.model.User;
import hr.team16.booksy.repository.ReservationRepository;
import hr.team16.booksy.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;

    public List<Reservation> getMyReservations(User user) {
        return reservationRepository.findByGuest(user);
    }

    public List<Reservation> getForMyProperties(User owner) {
        return reservationRepository.findByRoomPropertyOwnerId(owner.getId());
    }

    public Reservation getById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
    }

    public Reservation create(ReservationRequest req, User guest) {
        Room room = roomRepository.findById(req.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));
        long nights = ChronoUnit.DAYS.between(req.getCheckIn(), req.getCheckOut());
        if (nights <= 0) throw new RuntimeException("Invalid dates");
        BigDecimal total = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));
        Reservation res = new Reservation();
        res.setGuest(guest);
        res.setRoom(room);
        res.setCheckIn(req.getCheckIn());
        res.setCheckOut(req.getCheckOut());
        res.setTotalPrice(total);
        res.setStatus("PENDING");
        return reservationRepository.save(res);
    }

    public Reservation updateStatus(Long id, String status, User currentUser) {
        Reservation res = getById(id);
        boolean isOwner = res.getRoom().getProperty().getOwner().getId().equals(currentUser.getId());
        boolean isGuest = res.getGuest().getId().equals(currentUser.getId());
        if (!isOwner && !isGuest) throw new RuntimeException("Forbidden");
        if (isGuest && !status.equals("CANCELLED")) throw new RuntimeException("Guests can only cancel");
        res.setStatus(status);
        return reservationRepository.save(res);
    }

    public void delete(Long id, User guest) {
        Reservation res = getById(id);
        if (!res.getGuest().getId().equals(guest.getId())) throw new RuntimeException("Forbidden");
        reservationRepository.deleteById(id);
    }
}
