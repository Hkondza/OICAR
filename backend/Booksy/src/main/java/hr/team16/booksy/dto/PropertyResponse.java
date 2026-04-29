package hr.team16.booksy.dto;

import lombok.Data;

@Data
public class PropertyResponse {
    private Long id;
    private String name;
    private String address;
    private String city;
    private String country;
    private String status;
    private String ownerEmail;
}