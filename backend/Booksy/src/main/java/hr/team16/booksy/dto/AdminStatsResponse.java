package hr.team16.booksy.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AdminStatsResponse {
    private long totalUsers;
    private long totalGuests;
    private long totalOwners;
    private long totalProperties;
    private long pendingProperties;
    private long acceptedProperties;
    private long deniedProperties;
    private long totalReservations;
    private long pendingReservations;
    private long acceptedReservations;
    private BigDecimal platformRevenue;
    private List<MonthlyEntry> monthlyReservations;
}
