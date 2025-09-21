package com.codingshuttle.projects.airBnbApp.Service;

import com.codingshuttle.projects.airBnbApp.Entity.User;
import com.codingshuttle.projects.airBnbApp.Entity.enums.Roles;
import com.codingshuttle.projects.airBnbApp.ExceptionHandler.ResourceNotFoundException;
import com.codingshuttle.projects.airBnbApp.Repository.UserRepository;
import com.codingshuttle.projects.airBnbApp.Service.interfaces.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceClass implements AdminService {
    private final UserRepository userRepository;
    @Override
    public void approveHotelManager(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!user.getRoles().contains(Roles.PENDING_HOTEL_MANAGER)) {
            throw new IllegalStateException("User has no pending approval for hotel manager");
        }

        user.getRoles().remove(Roles.PENDING_HOTEL_MANAGER);
        user.getRoles().add(Roles.HOTEL_MANAGER);
        userRepository.save(user);
    }
}
