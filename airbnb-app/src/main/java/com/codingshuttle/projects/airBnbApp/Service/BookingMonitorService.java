package com.codingshuttle.projects.airBnbApp.Service;

import com.codingshuttle.projects.airBnbApp.Entity.Booking;
import com.codingshuttle.projects.airBnbApp.Entity.enums.BookingStatus;
import com.codingshuttle.projects.airBnbApp.Repository.BookingRepository;
import com.codingshuttle.projects.airBnbApp.Repository.InventoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookingMonitorService {

    private final BookingRepository bookingRepository;
    private final InventoryRepository inventoryRepository;

    @Scheduled(cron="${app.scheduling.cron.booking}")
    public void updateBookings()
    {
        int page = 0;
        int batchSize = 100;

        while(true){
            Page<Booking> bookingPage = bookingRepository.findAll(PageRequest.of(page,batchSize));
            if(bookingPage.isEmpty()){
                break;
            }
            bookingPage.getContent().forEach(this::updateBookingStatus);
            processBatch(bookingPage.getContent());
            page++;
        }
    }

    private void processBatch(List<Booking> bookings)
    {
        bookings.forEach(this::updateBookingStatus);
        bookingRepository.saveAll(bookings);

        // flush + clear for memory safety
        entityManager.flush();
        entityManager.clear();
    }

    private void updateBookingStatus(Booking booking)
    {
        if (hasBookingExpired(booking)
                && !EnumSet.of(BookingStatus.CONFIRMED,BookingStatus.CANCELLED,BookingStatus.EXPIRED).contains(booking.getBookingStatus()))
        {
            log.info("Booking status of id: {} --> {} set to EXPIRED",booking.getId(),booking.getBookingStatus());
            booking.setBookingStatus(BookingStatus.EXPIRED);

            inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(), booking.getCheckInDate(),
                    booking.getCheckOutDate(), booking.getRoomsCount());

            inventoryRepository.releaseReservedInventory(booking.getRoom().getId(), booking.getCheckInDate(),
                    booking.getCheckOutDate(), booking.getRoomsCount());
        }
    }

    public boolean hasBookingExpired(Booking booking){
        //Booking only active for 10 minutes.
        //so adding of guest list should be with in this window.
        return booking.getCreatedAt().plusMinutes(2).isBefore(LocalDateTime.now());
    }

    @PersistenceContext
    private EntityManager entityManager;
}
