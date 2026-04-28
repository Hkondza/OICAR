package hr.team16.booksy.dto;

import lombok.Data;

@Data
public class PropertyRequest {
    private String name;
    private String address;
    private String city;
    private String country;
}