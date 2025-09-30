package com.codingshuttle.projects.airBnbApp.Service;

import com.codingshuttle.projects.airBnbApp.DTO.*;
import com.codingshuttle.projects.airBnbApp.Entity.Hotel;
import com.codingshuttle.projects.airBnbApp.Entity.Room;
import com.codingshuttle.projects.airBnbApp.Entity.User;
import com.codingshuttle.projects.airBnbApp.ExceptionHandler.UnauthorizedException;
import com.codingshuttle.projects.airBnbApp.Repository.HotelRepository;
import com.codingshuttle.projects.airBnbApp.Repository.InventoryRepository;
import com.codingshuttle.projects.airBnbApp.Repository.RoomRepository;
import com.codingshuttle.projects.airBnbApp.Service.interfaces.HotelService;
import com.codingshuttle.projects.airBnbApp.Service.interfaces.InventoryService;
import com.codingshuttle.projects.airBnbApp.ExceptionHandler.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.dialect.function.InverseDistributionWindowEmulation;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.codingshuttle.projects.airBnbApp.Util.AppUtils.getCurrentUser;

@Service
@Slf4j
@RequiredArgsConstructor
public class HotelServiceClass implements HotelService {
    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;
    private final RoomRepository roomRepository;
    private final PricingUpdateService pricingUpdateService;
    private final InventoryRepository inventoryRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Override
    @PreAuthorize("hasRole('HOTEL_MANAGER')") //only manager is allowed to create hotel
    public HotelDto createNewHotel(HotelDto hotelDto){
        log.info("Creating new hotel with name: {}",hotelDto.getName());
        Hotel hotel = modelMapper.map(hotelDto,Hotel.class);
        hotel.setActive(false);

        User user = getCurrentUser();
        hotel.setOwner(user);

        hotel = hotelRepository.save(hotel);
        log.info("Created a new hotel with id: {} name: {}",hotel.getId(), hotel.getName());

        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    public HotelDto getHotelById(Long id){
        log.info("Getting hotel with id: {}",id);
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Hotel no found with id: "+id));
        User user = getCurrentUser();
        if(!user.equals(hotel.getOwner()))
        {
            throw new UnauthorizedException("This user doesn't own this hotel with id "+id);
        }
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    @PreAuthorize("hasRole('HOTEL_MANAGER')") //only manager is allowed to update hotel
    public HotelDto updateHotelById(Long id, HotelDto hotelDto){
        log.info("Updating the hotel with ID: {}",id);
        Hotel hotel = hotelRepository
                .findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with id " + id));

        User user = getCurrentUser();
        if(!user.equals(hotel.getOwner()))
        {
            throw new UnauthorizedException("This user doesn't own this hotel with id "+id);
        }

        hotelDto.setActive(hotel.getActive());

        modelMapper.map(hotelDto,hotel);    //mapping the dto to hotel entity
        hotel.setId(id);
        hotel = hotelRepository.save(hotel);
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')") //only admin is allowed to delete
    public void deleteHotelById(Long id) {
        Hotel hotel = hotelRepository
                .findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with id " + id));

//        User user = getCurrentUser();
//        if(!user.equals(hotel.getOwner()))
//        {
//            throw new UnauthorizedException("This user doesn't own this hotel with id "+id);
//        }

        for(Room room: hotel.getRooms()){
            inventoryService.deleteAllInventories(room);
            roomRepository.deleteById(room.getId());
        }
        pricingUpdateService.deleteMinPriceInventories(hotel);
        hotelRepository.deleteById(id);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')") //only admin is allowed to activate the hotel
    public void activateHotel(Long id) {
        log.info("Activating the hotel with ID: {}",id);
        Hotel hotel = hotelRepository
                .findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with id " + id));

//        User user = getCurrentUser();
//        if(!user.equals(hotel.getOwner()))
//        {
//            throw new UnauthorizedException("This user doesn't own this hotel with id "+id);
//        }

        if(hotel.getActive()){
            throw new ResourceNotFoundException("Hotel is already active");
        }

        hotel.setActive(true);

        //assuming only do it once when hotel is first time activated.
        for(Room room: hotel.getRooms()){
            inventoryService.initializeRoomForAYear(room);
        }
    }

    @Override
    public HotelInfoDto getHotelInfoById(Long hotelId, HotelInfoRequestDto hotelInfoRequestDto) {
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with id " + hotelId));

        if(hotelInfoRequestDto.getStartDate() == null)
            hotelInfoRequestDto.setStartDate(LocalDate.now());
        if(hotelInfoRequestDto.getEndDate() == null)
            hotelInfoRequestDto.setEndDate(LocalDate.now());
        if(hotelInfoRequestDto.getRoomsCount() == null )
            hotelInfoRequestDto.setRoomsCount(1L);

        long daysCount = ChronoUnit.DAYS.between(hotelInfoRequestDto.getStartDate(), hotelInfoRequestDto.getEndDate())+1;

        List<RoomPriceDto> roomPriceDtoList = inventoryRepository.findRoomAveragePrice(hotelId,
                hotelInfoRequestDto.getStartDate(), hotelInfoRequestDto.getEndDate(),
                hotelInfoRequestDto.getRoomsCount(), daysCount);


        List<RoomPriceResponseDto> rooms = roomPriceDtoList.stream()
                .map(roomPriceDto -> {
                    RoomPriceResponseDto roomPriceResponseDto = modelMapper.map(roomPriceDto.getRoom(),
                            RoomPriceResponseDto.class);
                    roomPriceResponseDto.setPrice(roomPriceDto.getPrice());
                    return roomPriceResponseDto;
                })
                .collect(Collectors.toList());

        return new HotelInfoDto(modelMapper.map(hotel, HotelDto.class), rooms);
    }

    @Override
    public List<HotelDto> getAllHotels() {
        User user = getCurrentUser();
        log.info("fetching all the hotels for user {}",user);
        List<Hotel> hotels = hotelRepository.findByOwner(user);
        return hotels.stream().map((element)->modelMapper.map(element,HotelDto.class))
                .collect(Collectors.toList());
    }
}
