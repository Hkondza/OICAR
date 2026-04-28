package hr.team16.booksy.dto;

import lombok.Data;

@Data
public class ReviewRequest {
    private Long reservationId;
    private Integer rating;
    private String comment;
}