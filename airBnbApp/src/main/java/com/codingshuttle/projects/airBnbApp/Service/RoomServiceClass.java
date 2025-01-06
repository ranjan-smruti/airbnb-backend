package com.codingshuttle.projects.airBnbApp.Service;

import com.codingshuttle.projects.airBnbApp.DTO.HotelDto;
import com.codingshuttle.projects.airBnbApp.DTO.RoomDto;
import com.codingshuttle.projects.airBnbApp.Entity.Hotel;
import com.codingshuttle.projects.airBnbApp.Entity.Room;
import com.codingshuttle.projects.airBnbApp.Repository.HotelRepository;
import com.codingshuttle.projects.airBnbApp.Repository.RoomRepository;
import com.codingshuttle.projects.airBnbApp.exception.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoomServiceClass implements RoomService {
    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;

    @Autowired
    ObjectMapper objectMapper;

    @Override
    public RoomDto createNewRoom(Long hotelId, RoomDto roomDto) throws JsonProcessingException {
        log.info("Creating a new room with hotel id:{}", hotelId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with id " + hotelId));
        Room room = modelMapper.map(roomDto, Room.class);
        room.setHotel(hotel);
        //get the list of images and amenities.
        room.setImages(objectMapper.writeValueAsString(roomDto.getImages()));
        room.setAmenities(objectMapper.writeValueAsString(roomDto.getAmenities()));
        room = roomRepository.save(room);

        //TODO:create inventory as soon as room is created if hotel is active.

        //Reading images and amenities
        RoomDto responseDto = modelMapper.map(room,RoomDto.class);
        responseDto.setImages(Arrays.asList(objectMapper.readValue(room.getImages(), String[].class)));
        responseDto.setAmenities(Arrays.asList(objectMapper.readValue(room.getAmenities(), String[].class)));
        return responseDto;
    }

    @Override
    public List<RoomDto> getAllRoomsInHotel(Long id) {
        log.info("Fetching all the rooms with hotel id: {}", id);
        Hotel hotel = hotelRepository
                .findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with id " + id));
        return hotel.getRooms()
                .stream()
                .map((element)->modelMapper.map(element,RoomDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public RoomDto getRoomById(Long id) {
        log.info("Fetching room with id: {}", id);
        Room room = roomRepository
                .findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Room not found with id " + id));

        return modelMapper.map(room, RoomDto.class);
    }

    @Override
    public void deleteRoomById(Long id) {
        log.info("Deleting room with id: {}", id);
       boolean isExists = roomRepository.existsById(id);
       if(!isExists) throw new ResourceNotFoundException("Room not found with id : "+id);
       roomRepository.deleteById(id);
       //TODO:delete all future inventories for this room.
    }
}
