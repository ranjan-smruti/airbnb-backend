package com.codingshuttle.projects.airBnbApp.DTO;

import com.codingshuttle.projects.airBnbApp.Entity.Room;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class HotelInfoDto {
    private HotelDto hotel;
    private List<RoomDto> rooms;
}
