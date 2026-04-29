package hr.team16.booksy.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RoomResponse {
    private Long id;
    private String name;
    private Integer capacity;
    private BigDecimal pricePerNight;
    private LocalDate availableFrom;
    private LocalDate availableTo;
    private Long propertyId;
    private String propertyName;
    private String city;
}