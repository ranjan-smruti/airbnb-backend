package com.codingshuttle.projects.airBnbApp.DTO;

import com.codingshuttle.projects.airBnbApp.Entity.HotelContactInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelPriceResponseDto {
    private Long id;
    private String name;
    private String city;
    private String[] photos;
    private String[] amenities;
    private Integer star;
    private BigDecimal rating;
    private HotelContactInfo contactInfo;
    private Double price;
}
