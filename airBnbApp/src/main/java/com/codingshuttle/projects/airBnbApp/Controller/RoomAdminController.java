package com.codingshuttle.projects.airBnbApp.Controller;

import com.codingshuttle.projects.airBnbApp.DTO.RoomDto;
import com.codingshuttle.projects.airBnbApp.ExceptionHandler.ApiResponse;
import com.codingshuttle.projects.airBnbApp.GlobalAPIResponseHandler.APIResponse;
import com.codingshuttle.projects.airBnbApp.Service.RoomService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/hotels/{hotelId}/rooms")
@RequiredArgsConstructor
public class RoomAdminController {
    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<RoomDto> createNewRoom(@PathVariable Long hotelId,
                                                     @RequestBody RoomDto roomDto) throws JsonProcessingException {

        RoomDto room = roomService.createNewRoom(hotelId,roomDto);
        return new ResponseEntity<>(room, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<RoomDto>> getAllRoomsInHotel(@PathVariable Long hotelId){
        return ResponseEntity.ok(roomService.getAllRoomsInHotel(hotelId));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomDto> getRoomById(@PathVariable Long hotelId,
                                               @PathVariable Long roomId) throws JsonProcessingException {
        return ResponseEntity.ok(roomService.getRoomById(roomId));
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<APIResponse<?>> deleteRoomById(@PathVariable Long hotelId,
                                                         @PathVariable Long roomId){
        roomService.deleteRoomById(roomId);
        ApiResponse apiResponse = ApiResponse.builder()
                .status(HttpStatus.OK)
                .msg("Room deleted successfully with id " + roomId)
                .build();
        return buildResponseEntity(apiResponse);
    }

    private ResponseEntity<APIResponse<?>> buildResponseEntity(ApiResponse apiResponse) {
        return new ResponseEntity<>(new APIResponse<>(apiResponse),apiResponse.getStatus());
    }
}
