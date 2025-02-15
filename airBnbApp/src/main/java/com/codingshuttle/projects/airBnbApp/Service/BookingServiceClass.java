package com.codingshuttle.projects.airBnbApp.Service;

import com.codingshuttle.projects.airBnbApp.DTO.*;
import com.codingshuttle.projects.airBnbApp.Entity.*;
import com.codingshuttle.projects.airBnbApp.Entity.enums.BookingStatus;
import com.codingshuttle.projects.airBnbApp.ExceptionHandler.UnauthorizedException;
import com.codingshuttle.projects.airBnbApp.Repository.*;
import com.codingshuttle.projects.airBnbApp.Service.interfaces.BookingService;
import com.codingshuttle.projects.airBnbApp.Service.interfaces.CheckOutService;
import com.codingshuttle.projects.airBnbApp.Strategy.PricingService;
import com.codingshuttle.projects.airBnbApp.exception.ResourceNotFoundException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.codingshuttle.projects.airBnbApp.Util.AppUtils.getCurrentUser;

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
    private final CheckOutService checkOutService;
    private final PricingService pricingService;

    @Value("${frontend.url}")
    private String frontEndUrl;

    @Override
    @Transactional
    public BookingDto initializeBooking(BookingRequestDTO bookingRequestDTO) {
        Hotel hotel = hotelRepository.findById(bookingRequestDTO.getHotelId()).orElseThrow(()->
                new ResourceNotFoundException("Hotel not found with id: "+ bookingRequestDTO.getHotelId()));

        Room room = roomRepository.findById(bookingRequestDTO.getRoomId()).orElseThrow(()->
                new ResourceNotFoundException("Room not found with id: "+ bookingRequestDTO.getRoomId()));

        if(!room.getHotel().getId().equals(hotel.getId())){
            throw new ResourceNotFoundException("room doesn't exist in the hotel!!");
        }

        List<Inventory> inventoryList = inventoryRepository.findAndLockAvailableInventory(
                room.getId(),
                bookingRequestDTO.getCheckInDate(),
                bookingRequestDTO.getCheckOutDate(),
                bookingRequestDTO.getRoomsCount()
        );

        long daysCount = ChronoUnit.DAYS.between(bookingRequestDTO.getCheckInDate(), bookingRequestDTO.getCheckOutDate())+1;
        if(inventoryList.size()!=daysCount){
            throw new IllegalStateException("Room is not available anymore");
        }

        //Reserve the room/Update the booked count of inventories
        inventoryRepository.initBooking(room.getId(), bookingRequestDTO.getCheckInDate(), bookingRequestDTO.getCheckOutDate(),
                bookingRequestDTO.getRoomsCount());

        BigDecimal priceForOneRoom = pricingService.calculateTotalPrice(inventoryList);
        BigDecimal totalPrice = priceForOneRoom.multiply(BigDecimal.valueOf(bookingRequestDTO.getRoomsCount()));

        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .checkInDate(bookingRequestDTO.getCheckInDate())
                .checkOutDate(bookingRequestDTO.getCheckOutDate())
                .user(getCurrentUser())
                .roomsCount(bookingRequestDTO.getRoomsCount())
                .amount(totalPrice)
                .build();

        booking = bookingRepository.save(booking);

        return modelMapper.map(booking, BookingDto.class);
    }

    @Override
    @Transactional
    public BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(()->
                new ResourceNotFoundException("No bookings found with id: " + bookingId));

        User user = getCurrentUser();

        if(!user.equals(booking.getUser())){
            throw new UnauthorizedException("Booking does not belong to this user with id " + user.getId());
        }

        if(hasBookingExpired(booking)){
            throw new IllegalStateException("Booking has already expired!!");
        }

        if(booking.getBookingStatus() != BookingStatus.RESERVED){
            throw new IllegalStateException("Booking is not under reserved state, cannot add guest.");
        }

        for(GuestDto guestDto: guestDtoList){
            Guest guest = modelMapper.map(guestDto, Guest.class);
            guest.setUser(user);
            guest = guestRepository.save(guest);
            booking.getGuests().add(guest);
        }
        booking.setBookingStatus(BookingStatus.GUESTS_ADDED);
        booking = bookingRepository.save(booking);

        return modelMapper.map(booking, BookingDto.class);
    }

    @Override
    @Transactional
    public String initiatePayments(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                ()->new ResourceNotFoundException("Booking not found with id: " + bookingId)
        );

        User user = getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new UnauthorizedException("Booking does not belong to this user with id " + user.getId());
        }

        if(hasBookingExpired(booking)){
            throw new IllegalStateException("Booking has already expired!!");
        }

        String sessionUrl = checkOutService.getCheckoutSession(booking, frontEndUrl+"/payments/success", frontEndUrl+"/payments/failure");

        booking.setBookingStatus(BookingStatus.PAYMENT_PENDING);
        bookingRepository.save(booking);

        return sessionUrl;
    }

    @Override
    @Transactional
    public void capturePayment(Event event) {
        if("checkout.session.completed".equals(event.getType())){
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if(session == null) return;

            String sessionId = session.getId();
            Booking booking =
                    bookingRepository.findByPaymentSessionId(sessionId).orElseThrow(()->
                            new ResourceNotFoundException("No payment session found with id "+sessionId));
            booking.setBookingStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            //acquired lock for reserved count
            inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(), booking.getCheckInDate(),
                      booking.getCheckOutDate(), booking.getRoomsCount());

            inventoryRepository.confirmBooking(booking.getRoom().getId(), booking.getCheckInDate(),
                      booking.getCheckOutDate(), booking.getRoomsCount());

            log.info("Booking confirmed for session id: {}",sessionId);

        }else {
            log.warn("Unhandled event type: {}",event.getType());
        }
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                ()->new ResourceNotFoundException("Booking not found with id: " + bookingId)
        );

        User user = getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new UnauthorizedException("Booking does not belong to this user with id " + user.getId());
        }

        if(booking.getBookingStatus() != BookingStatus.CONFIRMED){
            throw new IllegalStateException("Only confirmed bookings can be cancelled!!");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        //acquired lock for reserved count
        inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(), booking.getCheckInDate(),
                booking.getCheckOutDate(), booking.getRoomsCount());

        inventoryRepository.cancelBooking(booking.getRoom().getId(), booking.getCheckInDate(),
                booking.getCheckOutDate(), booking.getRoomsCount());

        //handle the refund
        try {
            Session session = Session.retrieve(booking.getPaymentSessionId());
            RefundCreateParams refundParams = RefundCreateParams.builder()
                    .setPaymentIntent(session.getPaymentIntent())
                    .build();

            Refund.create(refundParams);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBookingStatus(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                ()->new ResourceNotFoundException("Booking not found with id: " + bookingId)
        );

        User user = getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new UnauthorizedException("Booking does not belong to this user with id " + user.getId());
        }
        return booking.getBookingStatus().name();
    }

    @Override
    public List<HotelReportDTO> getReportByHotelId(Long hotelId, LocalDate startDate, LocalDate endDate) {

        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(()->
                new ResourceNotFoundException("Hotel not found with id: "+hotelId));

        User user = getCurrentUser();

        log.info("Generating report for hotel with id {}",hotelId);

        if(!user.equals(hotel.getOwner())){
            throw new AccessDeniedException("No access to view resources of hotel "+hotelId);
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Booking> bookings = bookingRepository.findByHotelAndCreatedAtBetween(hotel,startDateTime,endDateTime);

        long totalConfirmBookings = bookings.stream().filter(
                booking -> booking.getBookingStatus() == BookingStatus.CONFIRMED)
                .count();

        BigDecimal totalRevenueOfConfirmBookings = bookings.stream().filter(
                booking -> booking.getBookingStatus() == BookingStatus.CONFIRMED)
                .map(Booking::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgRevenue = totalConfirmBookings == 0 ? BigDecimal.ZERO :
                totalRevenueOfConfirmBookings.divide(BigDecimal.valueOf(totalConfirmBookings), RoundingMode.HALF_UP);

        return Collections.singletonList(new HotelReportDTO(totalConfirmBookings, totalRevenueOfConfirmBookings, avgRevenue));
    }

    @Override
    public List<BookingDto> getBookings() {
        User user = getCurrentUser();

        return bookingRepository.findByUser(user)
                .stream()
                .map((element)->modelMapper.map(element, BookingDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getAllBookingsByHotelId(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(()->
                new ResourceNotFoundException("Hotel not found with id: "+hotelId));

        User user = getCurrentUser();

        log.info("Fetching all bookings for hotel with id {}",hotelId);

        if(!user.equals(hotel.getOwner())){
            throw new AccessDeniedException("No access to view resources of hotel +"+hotelId);
        }

        List<Booking> booking = bookingRepository.findByHotel(hotel);

        return booking.stream()
                .map((element)->modelMapper.map(element, BookingDto.class))
                .collect(Collectors.toList());
    }

    public boolean hasBookingExpired(Booking booking){
        //Booking only active for 10 minutes.
        //so adding of guest list should be with in this window.
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }
}
