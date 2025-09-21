package com.codingshuttle.projects.airBnbApp.Controller;

import com.codingshuttle.projects.airBnbApp.DTO.*;
import com.codingshuttle.projects.airBnbApp.Entity.User;
import com.codingshuttle.projects.airBnbApp.Security.AuthService;
import com.codingshuttle.projects.airBnbApp.Service.interfaces.BookingService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.codingshuttle.projects.airBnbApp.Util.AppUtils.getCurrentUser;

@RestController
@RequiredArgsConstructor
@RequestMapping("/guest")
public class GuestController {
    private final BookingService bookingService;
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> guestLogin(@RequestBody GuestLoginDTO guestLoginDTO, HttpServletRequest request, HttpServletResponse response){
        String[] token = authService.guestLogin(guestLoginDTO);

        Cookie cookie = new Cookie("refreshToken",token[1]);
        cookie.setHttpOnly(true);

        response.addCookie(cookie);
        return ResponseEntity.ok(new LoginResponseDTO(token[0]));
    }
}
