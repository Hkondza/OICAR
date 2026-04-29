package hr.team16.booksy.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ReservationResponse {
    private Long id;
    private Long roomId;
    private String roomName;
    private String propertyName;
    private String city;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private BigDecimal totalPrice;
    private String status;
    private LocalDateTime createdAt;
    private String guestEmail;
}