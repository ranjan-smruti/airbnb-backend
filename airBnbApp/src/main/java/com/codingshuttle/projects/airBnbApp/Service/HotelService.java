package com.codingshuttle.projects.airBnbApp.Service;

import com.codingshuttle.projects.airBnbApp.DTO.HotelDto;
import com.codingshuttle.projects.airBnbApp.DTO.HotelInfoDto;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface HotelService {
    HotelDto createNewHotel(HotelDto hotelDto);
    HotelDto getHotelById(Long id);
    HotelDto updateHotelById(Long id, HotelDto hotelDto);
    void deleteHotelById(Long id);
    void activateHotel(Long id);

    HotelInfoDto getHotelInfoById(Long hotelId);
}
