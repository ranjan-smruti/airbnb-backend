package com.codingshuttle.projects.airBnbApp.Controller;

import com.codingshuttle.projects.airBnbApp.DTO.HotelInfoDto;
import com.codingshuttle.projects.airBnbApp.DTO.HotelInfoRequestDto;
import com.codingshuttle.projects.airBnbApp.DTO.HotelPriceResponseDto;
import com.codingshuttle.projects.airBnbApp.DTO.HotelSearchRequest;
import com.codingshuttle.projects.airBnbApp.Service.interfaces.HotelService;
import com.codingshuttle.projects.airBnbApp.Service.interfaces.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
    public ResponseEntity<Page<HotelPriceResponseDto>> searchHotels(
            @RequestParam String city,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam Integer roomsCount,
            @RequestParam(required = false) String nflt,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size)
    {
        HotelSearchRequest hotelSearchRequest = new HotelSearchRequest(city,startDate,endDate,roomsCount,page,size);
        var pageResult = inventoryService.searchHotels(hotelSearchRequest,nflt);
        return ResponseEntity.ok(pageResult);
    }

    @GetMapping("/{hotelId}/info")
    public ResponseEntity<HotelInfoDto> getHotelInfo(
            @PathVariable Long hotelId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) Long roomsCount) {

        HotelInfoRequestDto hotelInfoRequestDto = new HotelInfoRequestDto(startDate, endDate, roomsCount);
        return ResponseEntity.ok(hotelService.getHotelInfoById(hotelId, hotelInfoRequestDto));
    }
}
