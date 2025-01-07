package com.codingshuttle.projects.airBnbApp.Controller;

import com.codingshuttle.projects.airBnbApp.DTO.HotelDto;
import com.codingshuttle.projects.airBnbApp.ExceptionHandler.ApiResponse;
import com.codingshuttle.projects.airBnbApp.GlobalAPIResponseHandler.APIResponse;
import com.codingshuttle.projects.airBnbApp.Service.HotelService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sound.midi.VoiceStatus;

@RestController
@RequestMapping(path = "/admin/hotels")
@RequiredArgsConstructor
@Slf4j
public class HotelController {
    private final HotelService hotelService;

    @PostMapping
    public ResponseEntity<HotelDto> createNewHotel(@RequestBody HotelDto hotelDto) throws JsonProcessingException {
        log.info("Attempting to create new hotel with name: {}",hotelDto.getName());
        HotelDto hotel = hotelService.createNewHotel(hotelDto);
        return new ResponseEntity<>(hotel, HttpStatus.CREATED);
    }

    @GetMapping("/{hotelId}")
    public ResponseEntity<HotelDto> getHotelById(@PathVariable Long hotelId) throws JsonProcessingException {
        HotelDto hotel = hotelService.getHotelById(hotelId);
        return ResponseEntity.ok(hotel);
    }

    @PutMapping(path="/{hotelId}")
    public ResponseEntity<HotelDto> updateHotelById(@PathVariable Long hotelId,
                                                    @RequestBody HotelDto hotelDto) throws JsonProcessingException {
        HotelDto updateHotel = hotelService.updateHotelById(hotelId,hotelDto);
        return ResponseEntity.ok(updateHotel);
    }

    @DeleteMapping(path="/{hotelId}")
    public ResponseEntity<APIResponse<?>> deleteHotelById(@PathVariable Long hotelId){
       hotelService.deleteHotelById(hotelId);
        ApiResponse apiResponse = ApiResponse.builder()
                .status(HttpStatus.OK)
                .msg("Hotel deleted successfully with id " + hotelId)
                .build();
        return buildResponseEntity(apiResponse);
    }

    @PatchMapping("/{hotelId}")
    public ResponseEntity<APIResponse<?>> activateHotel(@PathVariable Long hotelId){
        hotelService.activateHotel(hotelId);
        ApiResponse apiResponse = ApiResponse.builder()
                .status(HttpStatus.OK)
                .msg("Hotel with id " + hotelId + " is now active")
                .build();
        return buildResponseEntity(apiResponse);
    }

    private ResponseEntity<APIResponse<?>> buildResponseEntity(ApiResponse apiResponse) {
        return new ResponseEntity<>(new APIResponse<>(apiResponse),apiResponse.getStatus());
    }
}
