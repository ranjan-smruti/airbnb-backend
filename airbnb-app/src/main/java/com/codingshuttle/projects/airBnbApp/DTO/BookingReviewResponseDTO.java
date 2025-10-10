package com.codingshuttle.projects.airBnbApp.DTO;

import com.codingshuttle.projects.airBnbApp.Entity.Booking;
import com.codingshuttle.projects.airBnbApp.Entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingReviewResponseDTO {
    private Long id;
    private Long bookingId;
    private Long userId;
    private BigDecimal rating;
    private String review;
}
