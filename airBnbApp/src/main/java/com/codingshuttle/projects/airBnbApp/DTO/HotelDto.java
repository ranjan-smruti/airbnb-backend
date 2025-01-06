package com.codingshuttle.projects.airBnbApp.DTO;

import com.codingshuttle.projects.airBnbApp.Entity.HotelContactInfo;
import lombok.Data;

import java.util.List;

@Data
public class HotelDto {
    private Long id;
    private String name;
    private String city;
    private List<String> images;
    private List<String> amenities;
    private HotelContactInfo contactInfo;
    private Boolean active;
}
