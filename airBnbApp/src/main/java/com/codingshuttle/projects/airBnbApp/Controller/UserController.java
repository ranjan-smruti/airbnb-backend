package com.codingshuttle.projects.airBnbApp.Controller;

import com.codingshuttle.projects.airBnbApp.DTO.BookingDto;
import com.codingshuttle.projects.airBnbApp.DTO.UserDTO;
import com.codingshuttle.projects.airBnbApp.Service.interfaces.BookingService;
import com.codingshuttle.projects.airBnbApp.Service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final BookingService bookingService;

    @PatchMapping("/profile")
    public ResponseEntity<Void> updateProfile(@RequestBody ProfileUpdateRequestDTO profileUpdateRequestDTO){
        userService.updateProfile(profileUpdateRequestDTO);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<BookingDto>> getBookings(){
        return ResponseEntity.ok(bookingService.getBookings());
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getProfile(){
        return ResponseEntity.ok(userService.getProfile());
    }
}
