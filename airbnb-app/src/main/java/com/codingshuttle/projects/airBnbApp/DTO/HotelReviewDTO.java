package com.codingshuttle.projects.airBnbApp.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelReviewDTO {
    private Double avgRating;
    private Long reviewCount;
}
