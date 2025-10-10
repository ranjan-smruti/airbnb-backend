package com.codingshuttle.projects.airBnbApp.Service;

import com.codingshuttle.projects.airBnbApp.DTO.GuestDto;
import com.codingshuttle.projects.airBnbApp.Entity.Guest;
import com.codingshuttle.projects.airBnbApp.Entity.User;
import com.codingshuttle.projects.airBnbApp.ExceptionHandler.DuplicateGuestException;
import com.codingshuttle.projects.airBnbApp.Repository.GuestRepository;
import com.codingshuttle.projects.airBnbApp.Service.interfaces.GuestService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import static com.codingshuttle.projects.airBnbApp.Util.AppUtils.getCurrentUser;


@Service
@RequiredArgsConstructor
@Slf4j
public class GuestServiceClass implements GuestService {

    private final GuestRepository guestRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<GuestDto> getAllGuests() {
        User user = getCurrentUser();
        log.info("Fetching all guests of user with id: {}", user.getId());
        List<Guest> guests = guestRepository.findByUser(user);
        return guests.stream()
                .map(guest -> modelMapper.map(guest, GuestDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<GuestDto> addNewGuest(List<GuestDto> guestDto) {
        log.info("Adding new guest: {}", guestDto);
        User user = getCurrentUser();

        List<Guest> guestsToSave = new ArrayList<>();

        for(GuestDto ele : guestDto)
        {
            //check if the guest exists with same name
            if(guestRepository.existsByNameIgnoreCaseAndUser(ele.getName(), user))
            {
                log.error("Duplicate guest found: {} for user: {}",ele.getName(),user.getId());
                throw new DuplicateGuestException("Guest with name " + ele.getName() + " already exists.");
            }
            Guest guest = modelMapper.map(ele, Guest.class);
            guest.setUser(user);
            guestsToSave.add(guest);
        }

        guestRepository.saveAll(guestsToSave);

        return guestsToSave.stream()
                .map(guest -> modelMapper.map(guest, GuestDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public void updateGuest(Long guestId, GuestDto guestDto) {
        log.info("Updating guest with ID: {}", guestId);
        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new EntityNotFoundException("Guest not found"));

        User user = getCurrentUser();
        if(!user.equals(guest.getUser())) throw new AccessDeniedException("You are not the owner of this guest");

        modelMapper.map(guestDto, guest);
        guest.setUser(user);
        guest.setId(guestId);

        guestRepository.save(guest);
        log.info("Guest with ID: {} updated successfully", guestId);
    }

    @Override
    public void deleteGuest(Long guestId) {
        log.info("Deleting guest with ID: {}", guestId);
        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new EntityNotFoundException("Guest not found"));

        User user = getCurrentUser();
        if(!user.equals(guest.getUser())) throw new AccessDeniedException("You are not the owner of this guest");

        guestRepository.deleteById(guestId);
        log.info("Guest with ID: {} deleted successfully", guestId);
    }
}
