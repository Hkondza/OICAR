package hr.team16.booksy.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OwnerStatsResponse {
    private long totalReservations;
    private long pendingCount;
    private long acceptedCount;
    private long deniedCount;
    private long cancelledCount;
    private BigDecimal totalRevenue;
    private long totalProperties;
    private long totalRooms;
    private List<MonthlyEntry> monthlyRevenue;
}
