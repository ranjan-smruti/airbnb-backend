package com.codingshuttle.projects.airBnbApp.Service;

import com.codingshuttle.projects.airBnbApp.DTO.BookingDto;
import com.codingshuttle.projects.airBnbApp.DTO.BookingRequest;
import com.codingshuttle.projects.airBnbApp.DTO.GuestDto;
import com.codingshuttle.projects.airBnbApp.Entity.*;
import com.codingshuttle.projects.airBnbApp.Entity.enums.BookingStatus;
import com.codingshuttle.projects.airBnbApp.Repository.*;
import com.codingshuttle.projects.airBnbApp.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.awt.print.Book;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceClass implements BookingService {
    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final InventoryRepository inventoryRepository;
    private final ModelMapper modelMapper;
    private final GuestRepository guestRepository;

    @Override
    @Transactional
    public BookingDto intializeBooking(BookingRequest bookingRequest) {
        Hotel hotel = hotelRepository.findById(bookingRequest.getHotelId()).orElseThrow(()->
                new ResourceNotFoundException("Hotel not found with id: "+bookingRequest.getHotelId()));

        Room room = roomRepository.findById(bookingRequest.getRoomId()).orElseThrow(()->
                new ResourceNotFoundException("Room not found with id: "+bookingRequest.getRoomId()));

        List<Inventory> inventoryList = inventoryRepository.findAndLockAvailableInventory(
                room.getId(),
                bookingRequest.getCheckInDate(),
                bookingRequest.getCheckOutDate(),
                bookingRequest.getRoomsCount()
        );

        long daysCount = ChronoUnit.DAYS.between(bookingRequest.getCheckInDate(),bookingRequest.getCheckOutDate())+1;
        if(inventoryList.size()!=daysCount){
            throw new IllegalStateException("Room is not available anymore");
        }

        //Reserve the room/Update the booked count of inventories
        for(Inventory inventory: inventoryList){
            inventory.setReservedCount(inventory.getReservedCount() + bookingRequest.getRoomsCount());
        }
        inventoryRepository.saveAll(inventoryList);

        //Create the booking
        //TODO: Calculate the dynamic price

        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .checkInDate(bookingRequest.getCheckInDate())
                .checkOutDate(bookingRequest.getCheckOutDate())
                .user(getCurrentUser())
                .roomsCount(bookingRequest.getRoomsCount())
                .amount(BigDecimal.TEN)
                .build();

        booking = bookingRepository.save(booking);

        return modelMapper.map(booking, BookingDto.class);
    }

    @Override
    public BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(()->
                new ResourceNotFoundException("No bookings found with id: " + bookingId));

        if(hasBookingExpired(booking)){
            throw new IllegalStateException("Booking has already expired!!");
        }

        if(booking.getBookingStatus() != BookingStatus.RESERVED){
            throw new IllegalStateException("Booking is not under reserved state, cannot add guest.");
        }

        for(GuestDto guestDto: guestDtoList){
            Guest guest = modelMapper.map(guestDto, Guest.class);
            guest.setUser(getCurrentUser());
            guest = guestRepository.save(guest);
            booking.getGuests().add(guest);
        }
        booking.setBookingStatus(BookingStatus.GUESTS_ADDED);
        booking = bookingRepository.save(booking);

        return modelMapper.map(booking, BookingDto.class);
    }

    public boolean hasBookingExpired(Booking booking){
        //Booking only active for 10 minutes.
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }

    public User getCurrentUser(){
        User user = new User();
        user.setId(1L); //TODO: REMOVE DUMMY.
        return user;
    }
}
