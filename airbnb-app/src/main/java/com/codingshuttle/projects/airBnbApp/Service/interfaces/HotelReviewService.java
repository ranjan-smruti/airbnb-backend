package com.codingshuttle.projects.airBnbApp.Service.interfaces;

import com.codingshuttle.projects.airBnbApp.DTO.BookingReviewDTO;
import com.codingshuttle.projects.airBnbApp.DTO.BookingReviewResponseDTO;
import com.codingshuttle.projects.airBnbApp.Entity.HotelReview;

import java.math.BigDecimal;

public interface HotelReviewService {
    BookingReviewResponseDTO submitReview(Long bookingId, BookingReviewDTO bookingReviewDTO);
}
