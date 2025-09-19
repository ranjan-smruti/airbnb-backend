package com.codingshuttle.projects.airBnbApp.Security;

import com.codingshuttle.projects.airBnbApp.DTO.LoginDTO;
import com.codingshuttle.projects.airBnbApp.DTO.SignUpRequestDTO;
import com.codingshuttle.projects.airBnbApp.DTO.UserDTO;
import com.codingshuttle.projects.airBnbApp.Entity.User;
import com.codingshuttle.projects.airBnbApp.Entity.enums.Roles;
import com.codingshuttle.projects.airBnbApp.ExceptionHandler.ResourceNotFoundException;
import com.codingshuttle.projects.airBnbApp.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    public UserDTO signUp(SignUpRequestDTO signUpRequestDTO){
        User user = userRepository.findByEmail(signUpRequestDTO.getEmail()).orElse(null);
        if(user != null){
            throw new RuntimeException("User is already present with email id " + signUpRequestDTO.getEmail());
        }

        User newUser = modelMapper.map(signUpRequestDTO, User.class);
        newUser.setRoles(Set.of(Roles.USER));
        newUser.setPassword(passwordEncoder.encode(signUpRequestDTO.getPassword()));
        newUser = userRepository.save(newUser);

        return modelMapper.map(newUser, UserDTO.class);
    }

    public String[] login(LoginDTO loginDTO){
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginDTO.getEmail(), loginDTO.getPassword()
        ));
        User user = (User) authentication.getPrincipal();

        String[] token = new String[2];
        token[0] = jwtService.generateAccessToken(user);
        token[1] = jwtService.generateRefreshToken(user);

        return token;
    }

    public String refreshToken(String refreshToken) {
        Long id = jwtService.getUserIdFromToken(refreshToken);

        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with id: "+id));
        return jwtService.generateAccessToken(user);
    }
}
