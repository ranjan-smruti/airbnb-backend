package com.codingshuttle.projects.airBnbApp.Service;

import com.codingshuttle.projects.airBnbApp.DTO.HotelDto;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface HotelService {
    HotelDto createNewHotel(HotelDto hotelDto) throws JsonProcessingException;
    HotelDto getHotelById(Long id) throws JsonProcessingException;
    HotelDto updateHotelById(Long id, HotelDto hotelDto) throws JsonProcessingException;
    void deleteHotelById(Long id);
    void activateHotel(Long id);
}
