package com.codingshuttle.projects.airBnbApp.Repository;

import com.codingshuttle.projects.airBnbApp.DTO.HotelReviewDTO;
import com.codingshuttle.projects.airBnbApp.Entity.HotelReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelReviewRepository extends JpaRepository<HotelReview,Long> {
    @Query("""
    SELECT new com.codingshuttle.projects.airBnbApp.DTO.HotelReviewDTO(AVG(r.rating), COUNT(r))
    FROM HotelReview r
    WHERE r.booking.hotel.id = :hotelId
    """)
    HotelReviewDTO findAverageRatingAndCount(@Param("hotelId") Long hotelId);

    boolean existsByBooking_IdAndUser_Id(Long bookingId, Long userId);
}
