package com.codingshuttle.projects.airBnbApp.DTO;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingReviewDTO {
    private BigDecimal rating;
    private String review;
}
