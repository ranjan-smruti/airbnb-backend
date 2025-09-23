package com.codingshuttle.projects.airBnbApp.Service;

import com.codingshuttle.projects.airBnbApp.Entity.Booking;
import com.codingshuttle.projects.airBnbApp.Entity.enums.BookingStatus;
import com.codingshuttle.projects.airBnbApp.Repository.BookingRepository;
import com.codingshuttle.projects.airBnbApp.Repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingMonitorService {

    private final BookingRepository bookingRepository;
    private final InventoryRepository inventoryRepository;

   @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean hasBookingExpired(Booking booking){
        //Booking only active for 10 minutes.
        //so adding of guest list should be with in this window.
        boolean status = booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());

        //if status has been expired then mark the status as expired and release the reserved inventory.
        if(status)
        {
            booking.setBookingStatus(BookingStatus.EXPIRED);
            bookingRepository.save(booking);

            inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(), booking.getCheckInDate(),
                    booking.getCheckOutDate(), booking.getRoomsCount());

            inventoryRepository.releaseReservedInventory(booking.getRoom().getId(), booking.getCheckInDate(),
                    booking.getCheckOutDate(), booking.getRoomsCount());
        }
        return status;
    }
}
