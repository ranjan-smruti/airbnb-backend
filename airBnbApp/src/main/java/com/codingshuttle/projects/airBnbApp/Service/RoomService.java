package com.codingshuttle.projects.airBnbApp.Service;

import com.codingshuttle.projects.airBnbApp.DTO.RoomDto;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

public interface RoomService {
    RoomDto createNewRoom(Long hotelId, RoomDto roomDto) throws JsonProcessingException;
    List<RoomDto> getAllRoomsInHotel(Long id);
    RoomDto getRoomById(Long id) throws JsonProcessingException;
    void deleteRoomById(Long id);
}
