package com.codingshuttle.projects.airBnbApp.Service.interfaces;

import com.codingshuttle.projects.airBnbApp.DTO.*;
import com.stripe.model.Event;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    BookingDto initializeBooking(BookingRequestDTO bookingRequestDTO);

    BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList);

    String initiatePayments(Long bookingId);

    void capturePayment(Event event);

    void cancelBooking(Long bookingId);

    String getBookingStatus(Long bookingId);

    List<BookingDto> getAllBookingsByHotelId(Long hotelId);

    List<HotelReportDTO> getReportByHotelId(Long hotelId, LocalDate startDate, LocalDate endDate);

    List<BookingDto> getBookings();
}
