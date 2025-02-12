package com.codingshuttle.projects.airBnbApp.Service;

import com.codingshuttle.projects.airBnbApp.Entity.User;
import com.codingshuttle.projects.airBnbApp.Repository.UserRepository;
import com.codingshuttle.projects.airBnbApp.Service.interfaces.UserService;
import com.codingshuttle.projects.airBnbApp.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceClass implements UserService, UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username).orElse(null);
    }
}
