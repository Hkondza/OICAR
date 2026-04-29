package hr.team16.booksy.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewResponse {
    private Long id;
    private Long reservationId;
    private String propertyName;
    private String city;
    private Integer rating;
    private String comment;
    private String guestEmail;
    private LocalDateTime createdAt;
}