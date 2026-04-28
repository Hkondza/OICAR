package hr.team16.booksy.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RoomRequest {
    private Long propertyId;
    private String name;
    private Integer capacity;
    private BigDecimal pricePerNight;
    private LocalDate availableFrom;
    private LocalDate availableTo;
}