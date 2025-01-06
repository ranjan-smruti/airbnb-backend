package com.codingshuttle.projects.airBnbApp.Service;

import com.codingshuttle.projects.airBnbApp.DTO.HotelDto;
import com.codingshuttle.projects.airBnbApp.Entity.Hotel;
import com.codingshuttle.projects.airBnbApp.Repository.HotelRepository;
import com.codingshuttle.projects.airBnbApp.exception.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@Slf4j
@RequiredArgsConstructor
public class HotelServiceClass implements HotelService{
    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;

    @Autowired
    ObjectMapper objectMapper;

    @Override
    public HotelDto createNewHotel(HotelDto hotelDto) throws JsonProcessingException {
        log.info("Creating new hotel with name: {}",hotelDto.getName());
        Hotel hotel = modelMapper.map(hotelDto,Hotel.class);
        hotel.setActive(false);
        //get the list of images and amenities.
        hotel.setImages(objectMapper.writeValueAsString(hotelDto.getImages()));
        hotel.setAmenities(objectMapper.writeValueAsString(hotelDto.getAmenities()));

        hotel = hotelRepository.save(hotel);
        log.info("Created a new hotel with id: {} name: {}",hotel.getId(), hotel.getName());

        HotelDto responseDto = modelMapper.map(hotel,HotelDto.class);
        //Reading images and amenities
        responseDto.setImages(Arrays.asList(objectMapper.readValue(hotel.getImages(), String[].class)));
        responseDto.setAmenities(Arrays.asList(objectMapper.readValue(hotel.getAmenities(), String[].class)));
        return responseDto;
    }

    @Override
    public HotelDto getHotelById(Long id) throws JsonProcessingException {
        log.info("Getting hotel with id: {}",id);
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Hotel no found with id: "+id));

        HotelDto responseDto = modelMapper.map(hotel,HotelDto.class);
        //Reading images and amenities
        responseDto.setImages(Arrays.asList(objectMapper.readValue(hotel.getImages(), String[].class)));
        responseDto.setAmenities(Arrays.asList(objectMapper.readValue(hotel.getAmenities(), String[].class)));

        return responseDto;
    }

    @Override
    public HotelDto updateHotelById(Long id, HotelDto hotelDto) throws JsonProcessingException {
        log.info("Updating the hotel with ID: {}",id);
        Hotel hotel = hotelRepository
                .findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with id " + id));
        modelMapper.map(hotelDto,hotel);    //mapping the dto to hotel entity
        hotel.setId(id);
        //get the list of images and amenities.
        hotel.setImages(objectMapper.writeValueAsString(hotelDto.getImages()));
        hotel.setAmenities(objectMapper.writeValueAsString(hotelDto.getAmenities()));
        hotel = hotelRepository.save(hotel);

        HotelDto responseDto = modelMapper.map(hotel,HotelDto.class);
        //Reading images and amenities
        responseDto.setImages(Arrays.asList(objectMapper.readValue(hotel.getImages(), String[].class)));
        responseDto.setAmenities(Arrays.asList(objectMapper.readValue(hotel.getAmenities(), String[].class)));
        return responseDto;
    }

    @Override
    public void deleteHotelById(Long id) {
        Boolean exists = hotelRepository.existsById(id);
        if(!exists) throw new ResourceNotFoundException("Hotel not found with id " + id);

        hotelRepository.deleteById(id);
        //delete the future inventories for this hotel
    }

    @Override
    public void activateHotel(Long id) {
        log.info("Activating the hotel with ID: {}",id);
        Hotel hotel = hotelRepository
                .findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with id " + id));

        hotel.setActive(true);
        //TODO: Create inventory for all the rooms for this hotel.
    }
}
