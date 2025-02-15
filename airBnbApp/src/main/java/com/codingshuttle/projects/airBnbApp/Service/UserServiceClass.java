package com.codingshuttle.projects.airBnbApp.Service;

import com.codingshuttle.projects.airBnbApp.Controller.ProfileUpdateRequestDTO;
import com.codingshuttle.projects.airBnbApp.DTO.UserDTO;
import com.codingshuttle.projects.airBnbApp.Entity.User;
import com.codingshuttle.projects.airBnbApp.Repository.UserRepository;
import com.codingshuttle.projects.airBnbApp.Service.interfaces.UserService;
import com.codingshuttle.projects.airBnbApp.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static com.codingshuttle.projects.airBnbApp.Util.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
public class UserServiceClass implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
    }

    @Override
    public UserDTO getProfile() {
       return modelMapper.map(getCurrentUser(), UserDTO.class);
    }

    @Override
    public void updateProfile(ProfileUpdateRequestDTO profileUpdateRequestDTO) {
        User user = getCurrentUser();
        if(profileUpdateRequestDTO.getDateOfBirth() != null) user.setDateOfBirth(profileUpdateRequestDTO.getDateOfBirth());
        if(profileUpdateRequestDTO.getGender() != null) user.setGender(profileUpdateRequestDTO.getGender());
        if(profileUpdateRequestDTO.getName() != null) user.setName(profileUpdateRequestDTO.getName());
        //also check for the role.

        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username).orElse(null);
    }
}
