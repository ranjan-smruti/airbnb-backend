package com.codingshuttle.projects.airBnbApp.DTO;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelSearchRequest {
    private String city;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer roomsCount;

    private Integer page=0;
    private Integer size=10;

    private BigDecimal lowPrice;
    private BigDecimal highPrice;
    private List<Integer> star = new ArrayList<>();
    private List<BigDecimal> ratings = new ArrayList<>();

    public HotelSearchRequest(String city, LocalDate startDate, LocalDate endDate, Integer roomsCount, Integer page, Integer size) {
        this.city=city;
        this.startDate=startDate;
        this.endDate=endDate;
        this.roomsCount=roomsCount;
        this.page=page;
        this.size=size;
    }
}
