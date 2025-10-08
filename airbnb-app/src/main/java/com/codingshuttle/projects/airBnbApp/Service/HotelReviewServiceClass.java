package com.codingshuttle.projects.airBnbApp.Service;

import com.codingshuttle.projects.airBnbApp.DTO.BookingReviewDTO;
import com.codingshuttle.projects.airBnbApp.DTO.BookingReviewResponseDTO;
import com.codingshuttle.projects.airBnbApp.DTO.HotelReviewDTO;
import com.codingshuttle.projects.airBnbApp.Entity.Booking;
import com.codingshuttle.projects.airBnbApp.Entity.Hotel;
import com.codingshuttle.projects.airBnbApp.Entity.HotelReview;
import com.codingshuttle.projects.airBnbApp.Entity.enums.BookingStatus;
import com.codingshuttle.projects.airBnbApp.Repository.BookingRepository;
import com.codingshuttle.projects.airBnbApp.Repository.HotelRepository;
import com.codingshuttle.projects.airBnbApp.Repository.HotelReviewRepository;
import com.codingshuttle.projects.airBnbApp.Service.interfaces.HotelReviewService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.module.ResolutionException;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class HotelReviewServiceClass implements HotelReviewService {
    private final HotelReviewRepository reviewRepository;
    private final HotelRepository hotelRepository;
    private final BookingRepository bookingRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public BookingReviewResponseDTO submitReview(Long bookingId, BookingReviewDTO bookingReviewDTO)
    {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(()-> new ResolutionException("Booking not found with id " + bookingId));

        if(booking.getBookingStatus() != BookingStatus.CONFIRMED)
        {
            throw new RuntimeException("This booking is either CANCELED/EXPIRED, cannot share review.");
        }

        if(reviewRepository.existsByBooking_IdAndUser_Id(bookingId,booking.getUser().getId()))
        {
            throw new RuntimeException("You have already reviewed this booking.");
        }

        HotelReview hotelReview = new HotelReview();
        hotelReview.setBooking(booking);
        hotelReview.setUser(booking.getUser());
        hotelReview.setRating(bookingReviewDTO.getRating());
        hotelReview.setReview(bookingReviewDTO.getReview());

        reviewRepository.save(hotelReview);

        //update the average count for the hotel rating and reviewCount column.
        HotelReviewDTO record = reviewRepository.findAverageRatingAndCount(booking.getHotel().getId());
        BigDecimal avgRating = record.getAvgRating() != null
                ? BigDecimal.valueOf(record.getAvgRating()).setScale(1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Long reviewCount = record.getReviewCount() != null ? record.getReviewCount() : 0;

        Hotel hotel = booking.getHotel();
        hotel.setRating(avgRating);
        hotel.setReviewCount(reviewCount);

        hotelRepository.save(hotel);

        return modelMapper.map(hotelReview,BookingReviewResponseDTO.class);
    }
}
