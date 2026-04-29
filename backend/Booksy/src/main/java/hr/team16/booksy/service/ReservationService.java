package hr.team16.booksy.service;

import hr.team16.booksy.dto.ReservationRequest;
import hr.team16.booksy.dto.ReservationResponse;
import hr.team16.booksy.model.Reservation;
import hr.team16.booksy.model.Room;
import hr.team16.booksy.model.User;
import hr.team16.booksy.repository.ReservationRepository;
import hr.team16.booksy.repository.RoomRepository;
import hr.team16.booksy.repository.UserRepository;
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
    private final UserRepository userRepository;

    public List<ReservationResponse> getMyReservations(String email) {
        User guest = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return reservationRepository.findByGuest(guest).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ReservationResponse> getForMyProperties(String email) {
        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return reservationRepository.findByRoomPropertyOwnerId(owner.getId()).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ReservationResponse getById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        return mapToResponse(reservation);
    }

    public ReservationResponse createReservation(ReservationRequest request, String email) {
        User guest = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // Provjeri dostupnost sobe u traženom terminu
        boolean isAvailable = reservationRepository
                .findByRoomIdAndStatusNot(request.getRoomId(), "CANCELLED")
                .stream()
                .noneMatch(r ->
                        request.getCheckIn().isBefore(r.getCheckOut()) &&
                                request.getCheckOut().isAfter(r.getCheckIn())
                );

        if (!isAvailable) {
            throw new RuntimeException("Room not available for selected dates");
        }

        // Izračunaj cijenu
        long nights = ChronoUnit.DAYS.between(request.getCheckIn(), request.getCheckOut());
        if (nights <= 0) throw new RuntimeException("Invalid dates");
        BigDecimal total = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));

        Reservation reservation = new Reservation();
        reservation.setGuest(guest);
        reservation.setRoom(room);
        reservation.setCheckIn(request.getCheckIn());
        reservation.setCheckOut(request.getCheckOut());
        reservation.setTotalPrice(total);
        reservation.setStatus("PENDING");
        reservationRepository.save(reservation);

        return mapToResponse(reservation);
    }

    public ReservationResponse updateStatus(Long id, String status, String email) {
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        boolean isOwner = reservation.getRoom().getProperty().getOwner()
                .getId().equals(currentUser.getId());
        boolean isGuest = reservation.getGuest().getId().equals(currentUser.getId());

        if (!isOwner && !isGuest) throw new RuntimeException("Forbidden");
        if (isGuest && !status.equals("CANCELLED")) throw new RuntimeException("Guests can only cancel");
        if (isOwner && status.equals("CANCELLED")) throw new RuntimeException("Owners cannot cancel, use DENIED");

        reservation.setStatus(status);
        reservationRepository.save(reservation);

        return mapToResponse(reservation);
    }

    public void delete(Long id, String email) {
        User guest = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        if (!reservation.getGuest().getId().equals(guest.getId())) {
            throw new RuntimeException("Forbidden");
        }

        reservationRepository.deleteById(id);
    }

    private ReservationResponse mapToResponse(Reservation reservation) {
        ReservationResponse response = new ReservationResponse();
        response.setId(reservation.getId());
        response.setRoomId(reservation.getRoom().getId());
        response.setRoomName(reservation.getRoom().getName());
        response.setPropertyName(reservation.getRoom().getProperty().getName());
        response.setCity(reservation.getRoom().getProperty().getCity());
        response.setCheckIn(reservation.getCheckIn());
        response.setCheckOut(reservation.getCheckOut());
        response.setTotalPrice(reservation.getTotalPrice());
        response.setStatus(reservation.getStatus());
        response.setCreatedAt(reservation.getCreatedAt());
        response.setGuestEmail(reservation.getGuest().getEmail());
        return response;
    }
}