package com.codingshuttle.projects.airBnbApp.Service;

import com.codingshuttle.projects.airBnbApp.DTO.BookingDto;
import com.codingshuttle.projects.airBnbApp.DTO.BookingRequest;
import com.codingshuttle.projects.airBnbApp.DTO.GuestDto;

import java.util.List;

public interface BookingService {

    BookingDto intializeBooking(BookingRequest bookingRequest);

    BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList);
}
