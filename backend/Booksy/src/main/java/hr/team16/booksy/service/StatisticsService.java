package hr.team16.booksy.service;

import hr.team16.booksy.dto.AdminStatsResponse;
import hr.team16.booksy.dto.GuestStatsResponse;
import hr.team16.booksy.dto.MonthlyEntry;
import hr.team16.booksy.dto.OwnerStatsResponse;
import hr.team16.booksy.repository.PropertyRepository;
import hr.team16.booksy.repository.ReservationRepository;
import hr.team16.booksy.repository.RoomRepository;
import hr.team16.booksy.repository.UserPropertyRepository;
import hr.team16.booksy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final ReservationRepository reservationRepository;
    private final PropertyRepository propertyRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final UserPropertyRepository userPropertyRepository;

    // ─── GUEST ────────────────────────────────────────────────────────────────

    public GuestStatsResponse getGuestStats(Long userId) {
        var reservations = reservationRepository.findByGuestId(userId);

        GuestStatsResponse stats = new GuestStatsResponse();
        stats.setTotalReservations(reservations.size());
        stats.setPendingCount(reservations.stream().filter(r -> "PENDING".equals(r.getStatus())).count());
        stats.setAcceptedCount(reservations.stream().filter(r -> "ACCEPTED".equals(r.getStatus())).count());
        stats.setCancelledCount(reservations.stream().filter(r -> "CANCELLED".equals(r.getStatus())).count());

        BigDecimal totalSpent = reservations.stream()
                .filter(r -> !"CANCELLED".equals(r.getStatus()) && !"DENIED".equals(r.getStatus()))
                .map(r -> r.getTotalPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTotalSpent(totalSpent);

        List<MonthlyEntry> monthly = reservationRepository.findMonthlyStatsByGuest(userId)
                .stream()
                .map(row -> new MonthlyEntry(
                        toMonthLabel(row),
                        ((Number) row[2]).longValue(),
                        new BigDecimal(row[3].toString())
                ))
                .collect(Collectors.toList());
        stats.setMonthlyReservations(monthly);

        return stats;
    }

    // ─── OWNER ────────────────────────────────────────────────────────────────

    public OwnerStatsResponse getOwnerStats(Long userId) {
        var reservations = reservationRepository.findByRoomPropertyOwnerId(userId);

        OwnerStatsResponse stats = new OwnerStatsResponse();
        stats.setTotalReservations(reservations.size());
        stats.setPendingCount(reservations.stream().filter(r -> "PENDING".equals(r.getStatus())).count());
        stats.setAcceptedCount(reservations.stream().filter(r -> "ACCEPTED".equals(r.getStatus())).count());
        stats.setDeniedCount(reservations.stream().filter(r -> "DENIED".equals(r.getStatus())).count());
        stats.setCancelledCount(reservations.stream().filter(r -> "CANCELLED".equals(r.getStatus())).count());

        BigDecimal totalRevenue = reservations.stream()
                .filter(r -> !"CANCELLED".equals(r.getStatus()) && !"DENIED".equals(r.getStatus()))
                .map(r -> r.getTotalPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTotalRevenue(totalRevenue);

        stats.setTotalProperties(propertyRepository.countByOwnerId(userId));

        long totalRooms = propertyRepository.findByOwnerId(userId).stream()
                .mapToLong(p -> roomRepository.findByPropertyId(p.getId()).size())
                .sum();
        stats.setTotalRooms(totalRooms);

        List<MonthlyEntry> monthly = reservationRepository.findMonthlyStatsByOwner(userId)
                .stream()
                .map(row -> new MonthlyEntry(
                        toMonthLabel(row),
                        ((Number) row[2]).longValue(),
                        new BigDecimal(row[3].toString())
                ))
                .collect(Collectors.toList());
        stats.setMonthlyRevenue(monthly);

        return stats;
    }

    // ─── HELPER ───────────────────────────────────────────────────────────────

    private String toMonthLabel(Object[] row) {
        int year = ((Number) row[0]).intValue();
        int month = ((Number) row[1]).intValue();
        return String.format("%04d-%02d", year, month);
    }

    // ─── ADMIN ────────────────────────────────────────────────────────────────

    public AdminStatsResponse getAdminStats() {
        AdminStatsResponse stats = new AdminStatsResponse();

        stats.setTotalUsers(userRepository.count());
        stats.setTotalGuests(userRepository.countByRole("GUEST"));
        stats.setTotalOwners(userRepository.countByRole("OWNER"));

        stats.setTotalProperties(propertyRepository.count());
        stats.setPendingProperties(userPropertyRepository.findByStatus("PENDING").size());
        stats.setAcceptedProperties(userPropertyRepository.findByStatus("ACCEPTED").size());
        stats.setDeniedProperties(userPropertyRepository.findByStatus("DENIED").size());

        var allReservations = reservationRepository.findAll();
        stats.setTotalReservations(allReservations.size());
        stats.setPendingReservations(reservationRepository.countByStatus("PENDING"));
        stats.setAcceptedReservations(reservationRepository.countByStatus("ACCEPTED"));

        BigDecimal platformRevenue = allReservations.stream()
                .filter(r -> !"CANCELLED".equals(r.getStatus()) && !"DENIED".equals(r.getStatus()))
                .map(r -> r.getTotalPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setPlatformRevenue(platformRevenue);

        List<MonthlyEntry> monthly = reservationRepository.findMonthlyStatsAll()
                .stream()
                .map(row -> new MonthlyEntry(
                        toMonthLabel(row),
                        ((Number) row[2]).longValue(),
                        new BigDecimal(row[3].toString())
                ))
                .collect(Collectors.toList());
        stats.setMonthlyReservations(monthly);

        return stats;
    }
}
