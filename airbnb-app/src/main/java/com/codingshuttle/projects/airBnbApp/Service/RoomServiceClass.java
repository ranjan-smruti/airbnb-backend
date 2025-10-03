package com.codingshuttle.projects.airBnbApp.Service;

import com.codingshuttle.projects.airBnbApp.DTO.HotelDto;
import com.codingshuttle.projects.airBnbApp.DTO.RoomDto;
import com.codingshuttle.projects.airBnbApp.Entity.Hotel;
import com.codingshuttle.projects.airBnbApp.Entity.Room;
import com.codingshuttle.projects.airBnbApp.Entity.User;
import com.codingshuttle.projects.airBnbApp.ExceptionHandler.UnauthorizedException;
import com.codingshuttle.projects.airBnbApp.Repository.HotelRepository;
import com.codingshuttle.projects.airBnbApp.Repository.RoomRepository;
import com.codingshuttle.projects.airBnbApp.Service.interfaces.InventoryService;
import com.codingshuttle.projects.airBnbApp.Service.interfaces.RoomService;
import com.codingshuttle.projects.airBnbApp.ExceptionHandler.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static com.codingshuttle.projects.airBnbApp.Util.AppUtils.getCurrentUser;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoomServiceClass implements RoomService {
    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;

    @Autowired
    ObjectMapper objectMapper;

    @Override
    @PreAuthorize("hasRole('HOTEL_MANAGER')") //only manager is allowed to create room
    public RoomDto createNewRoom(Long hotelId, RoomDto roomDto){
        log.info("Creating a new room in hotel with ID: {}", hotelId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: "+hotelId));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner()))
        {
            throw new UnauthorizedException("This user doesn't own this hotel with id "+hotelId);
        }

        Room room = modelMapper.map(roomDto, Room.class);
        room.setHotel(hotel);
        room = roomRepository.save(room);

        if (hotel.getActive()) {
            inventoryService.initializeRoomForAYear(room);
        }

        return modelMapper.map(room, RoomDto.class);
    }

    @Override
    public List<RoomDto> getAllRoomsInHotel(Long hotelId) {
        log.info("Getting all rooms in hotel with ID: {}", hotelId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: "+hotelId));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner()))
        {
            throw new UnauthorizedException("This user doesn't own this hotel with id "+hotelId);
        }

        return hotel.getRooms()
                .stream()
                .map((element) -> modelMapper.map(element, RoomDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public RoomDto getRoomById(Long roomId) {
        log.info("Getting the room with ID: {}", roomId);
        Room room = roomRepository
                .findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: "+roomId));
        return modelMapper.map(room, RoomDto.class);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('HOTEL_MANAGER')") //only manager is allowed to update room
    public RoomDto updateRoomById(Long hotelId, Long roomId, RoomDto roomDto) {
        log.info("Updating the room with ID: {}",roomId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with id " + hotelId));

        User user = getCurrentUser();
        if(!user.equals(hotel.getOwner()))
        {
            throw new UnauthorizedException("This user doesn't own this hotel with id "+hotelId);
        }

        Room room = roomRepository.findById(roomId).orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: "+roomId));

        BigDecimal oldPrice = room.getBasePrice();
        Integer oldTotalCount = room.getTotalCount();

        modelMapper.map(roomDto,room);    //mapping the dto to hotel entity
        room.setId(roomId);
        room = roomRepository.save(room);

        if(!oldPrice.equals(room.getBasePrice()))
            inventoryService.updatePriceByRoom(room);

        if(!oldTotalCount.equals(room.getTotalCount()))
            inventoryService.updateRoomCountByRoom(room);

        return modelMapper.map(room, RoomDto.class);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')") //only admin is allowed to delete
    public void deleteRoomById(Long id) {
        log.info("Deleting room with id: {}", id);
        Room room = roomRepository
                .findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Room not found with id " + id));

//        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        if(!user.equals(room.getHotel().getOwner()))
//        {
//            throw new UnauthorizedException("This user doesn't own this hotel with id "+id);
//        }

        inventoryService.deleteAllInventories(room);
        roomRepository.deleteById(id);

    }
}
