package com.codingshuttle.projects.airBnbApp.Service.interfaces;

import com.codingshuttle.projects.airBnbApp.DTO.BookingReviewDTO;

public interface HotelReviewService {
    void submitReview(Long bookingId, BookingReviewDTO bookingReviewDTO);
}
