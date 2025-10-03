package com.codingshuttle.projects.airBnbApp.Controller;

import com.codingshuttle.projects.airBnbApp.DTO.BookingDto;
import com.codingshuttle.projects.airBnbApp.DTO.GuestDto;
import com.codingshuttle.projects.airBnbApp.DTO.ProfileUpdateRequestDTO;
import com.codingshuttle.projects.airBnbApp.DTO.UserDTO;
import com.codingshuttle.projects.airBnbApp.ExceptionHandler.ApiResponse;
import com.codingshuttle.projects.airBnbApp.GlobalAPIResponseHandler.APIResponse;
import com.codingshuttle.projects.airBnbApp.Service.interfaces.BookingService;
import com.codingshuttle.projects.airBnbApp.Service.interfaces.GuestService;
import com.codingshuttle.projects.airBnbApp.Service.interfaces.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final BookingService bookingService;
    private final GuestService guestService;

    @PatchMapping("/profile")
    public ResponseEntity<APIResponse<?>> updateProfile(@RequestBody ProfileUpdateRequestDTO profileUpdateRequestDTO){
        userService.updateProfile(profileUpdateRequestDTO);
        ApiResponse apiResponse = ApiResponse.builder()
                .status(HttpStatus.OK)
                .msg("User profile updated successfully!!")
                .build();
        return buildResponseEntity(apiResponse);
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<BookingDto>> getBookings(){
        return ResponseEntity.ok(bookingService.getBookings());
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getProfile(){
        return ResponseEntity.ok(userService.getProfile());
    }

    @GetMapping("/guests")
    public ResponseEntity<List<GuestDto>> getAllGuests() {
        return ResponseEntity.ok(guestService.getAllGuests());
    }

    @PostMapping("/guests")
    public ResponseEntity<GuestDto> addNewGuest(@RequestBody GuestDto guestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(guestService.addNewGuest(guestDto));
    }

    @PutMapping("/guests/{guestId}")
    public ResponseEntity<APIResponse<?>> updateGuest(@PathVariable Long guestId, @RequestBody GuestDto guestDto) {
        guestService.updateGuest(guestId, guestDto);

        ApiResponse apiResponse = ApiResponse.builder()
                .status(HttpStatus.OK)
                .msg("Guest list updated successfully!!")
                .build();
        return buildResponseEntity(apiResponse);
    }

    @DeleteMapping("/guests/{guestId}")
    public ResponseEntity<APIResponse<?>> deleteGuest(@PathVariable Long guestId) {
        guestService.deleteGuest(guestId);

        ApiResponse apiResponse = ApiResponse.builder()
                .status(HttpStatus.OK)
                .msg("Guest list deleted successfully!!")
                .build();
        return buildResponseEntity(apiResponse);
    }

    private ResponseEntity<APIResponse<?>> buildResponseEntity(ApiResponse apiResponse) {
        return new ResponseEntity<>(new APIResponse<>(apiResponse),apiResponse.getStatus());
    }
}
