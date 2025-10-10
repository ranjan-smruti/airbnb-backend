package com.codingshuttle.projects.airBnbApp.Service.interfaces;

import com.codingshuttle.projects.airBnbApp.DTO.GuestDto;

import java.util.List;

public interface GuestService {

    List<GuestDto> getAllGuests();

    void updateGuest(Long guestId, GuestDto guestDto);

    void deleteGuest(Long guestId);

    List<GuestDto> addNewGuest(List<GuestDto> guestDto);
}
