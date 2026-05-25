package hr.team16.booksy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyEntry {
    private String month;   // e.g. "2025-01"
    private long count;
    private BigDecimal amount;
}
