package com.codingshuttle.projects.airBnbApp.DTO;

import com.codingshuttle.projects.airBnbApp.Entity.Hotel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelPriceDto {
    private Hotel hotel;
    private double price;
}
