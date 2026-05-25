package hr.team16.booksy.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class GuestStatsResponse {
    private long totalReservations;
    private long pendingCount;
    private long acceptedCount;
    private long cancelledCount;
    private BigDecimal totalSpent;
    private List<MonthlyEntry> monthlyReservations;
}
