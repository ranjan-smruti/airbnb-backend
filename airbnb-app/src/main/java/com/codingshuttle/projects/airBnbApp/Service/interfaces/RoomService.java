package com.codingshuttle.projects.airBnbApp.Service.interfaces;

import com.codingshuttle.projects.airBnbApp.DTO.RoomDto;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

public interface RoomService {
    RoomDto createNewRoom(Long hotelId, RoomDto roomDto);
    List<RoomDto> getAllRoomsInHotel(Long id);
    RoomDto getRoomById(Long id) throws JsonProcessingException;
    void deleteRoomById(Long id);

    RoomDto updateRoomById(Long hotelId, Long roomId, RoomDto roomDto);
}
