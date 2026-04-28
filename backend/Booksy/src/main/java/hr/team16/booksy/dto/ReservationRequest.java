package hr.team16.booksy.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ReservationRequest {
    private Long roomId;
    private LocalDate checkIn;
    private LocalDate checkOut;
}
