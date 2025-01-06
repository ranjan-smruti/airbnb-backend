package com.codingshuttle.projects.airBnbApp.DTO;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RoomDto {
    private Long id;
    private String type;
    private BigDecimal basePrice;
    private List<String> images;
    private List<String> amenities;
    private Integer totalCount;
    private Integer capacity;
}
