package com.codingshuttle.projects.airBnbApp.Controller;

import com.codingshuttle.projects.airBnbApp.DTO.HotelDto;
import com.codingshuttle.projects.airBnbApp.DTO.HotelInfoDto;
import com.codingshuttle.projects.airBnbApp.DTO.HotelPriceDto;
import com.codingshuttle.projects.airBnbApp.DTO.HotelSearchRequest;
import com.codingshuttle.projects.airBnbApp.Service.HotelService;
import com.codingshuttle.projects.airBnbApp.Service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
public class HotelBrowseController {
    private final InventoryService inventoryService;
    private final HotelService hotelService;

    /*
    * The Search result will have average min booking price of a hotel room in the search filter.
    * Let say a hotel is having 3 different rooms but the lowest price among all those rooms will be displayed for this hotel.
    * These prices again will differ on the basis of different strategy like urgency(booked within 7days of the present date),holiday,surge factor,occupancy,etc.
    * */
    @GetMapping("/search")
    public ResponseEntity<Page<HotelPriceDto>> searchHotels(@RequestBody HotelSearchRequest hotelSearchRequest){
       var page = inventoryService.searchHotels(hotelSearchRequest);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{hotelId}/info")
    public ResponseEntity<HotelInfoDto> getHotelInfo(@PathVariable Long hotelId){
        return ResponseEntity.ok(hotelService.getHotelInfoById(hotelId));
    }
}
