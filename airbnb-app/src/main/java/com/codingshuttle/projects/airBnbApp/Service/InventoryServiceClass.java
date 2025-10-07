package com.codingshuttle.projects.airBnbApp.Service;

import com.codingshuttle.projects.airBnbApp.DTO.*;
import com.codingshuttle.projects.airBnbApp.Entity.Inventory;
import com.codingshuttle.projects.airBnbApp.Entity.Room;
import com.codingshuttle.projects.airBnbApp.Entity.User;
import com.codingshuttle.projects.airBnbApp.ExceptionHandler.ResourceNotFoundException;
import com.codingshuttle.projects.airBnbApp.Repository.HotelMinPriceRepository;
import com.codingshuttle.projects.airBnbApp.Repository.InventoryRepository;
import com.codingshuttle.projects.airBnbApp.Repository.RoomRepository;
import com.codingshuttle.projects.airBnbApp.Service.interfaces.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.codingshuttle.projects.airBnbApp.Util.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceClass implements InventoryService {
    private final InventoryRepository inventoryRepository;
    private final ModelMapper modelMapper;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    private final RoomRepository roomRepository;

    private final Integer SEARCH_DAYS_LIMIT = 90;

    @Override
    public void initializeRoomForAYear(Room room){
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusYears(1);
        for(; !today.isAfter(endDate); today=today.plusDays(1)){
            Inventory inventory=Inventory.builder()
                    .hotel(room.getHotel())
                    .room(room)
                    .bookedCount(0)
                    .reservedCount(0)
                    .city(room.getHotel().getCity())
                    .date(today)
                    .price(room.getBasePrice())
                    .surgeFactor(BigDecimal.ONE)
                    .totalCount(room.getTotalCount())
                    .closed(false)
                    .build();
            inventoryRepository.save(inventory);
        }
    }

    @Override
    public void deleteAllInventories(Room room) {
        inventoryRepository.deleteByRoom(room);
    }

    /*
    * For browsing hotels, we need to return all the hotels whose inventory is active.
    At least one room type is available between the start and end date of that city.

    Criteria for inventory:
    1. startDate <= date <= endDate
    2. city
    3. availability:(totalCount - bookedCount) >= roomsCount
    4. Closed = false (for inventory)

    Group the response by room and get the response by unique hotels.
    * */
    @Override
    public Page<HotelPriceResponseDto> searchHotels(HotelSearchRequest hotelSearchRequest) {

        List<Integer> ratings = hotelSearchRequest.getStar();
        if (ratings == null || ratings.isEmpty()) {
            hotelSearchRequest.setStar(null); // no filter
        }
        else
        {
            boolean invalid = ratings.stream().anyMatch(r -> r < 1 || r > 5);
            if(invalid)
            {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Property ratings must be between 1 and 5.");
            }
        }

        Pageable pageable = PageRequest.of(hotelSearchRequest.getPage(), hotelSearchRequest.getSize());

        long dateCount = ChronoUnit.DAYS.between(hotelSearchRequest.getStartDate(), hotelSearchRequest.getEndDate())+1;
        long daysBetween = ChronoUnit.DAYS.between(LocalDate.now(),hotelSearchRequest.getStartDate());

        if(daysBetween < SEARCH_DAYS_LIMIT)
        {
            Page<HotelPriceDto> hotelPage = hotelMinPriceRepository.findHotelsWithAvailableInventory(hotelSearchRequest.getCity(),
                    hotelSearchRequest.getStartDate(), hotelSearchRequest.getEndDate(), hotelSearchRequest.getRoomsCount(),
                    dateCount,hotelSearchRequest.getStar(), pageable);

            return hotelPage.map(hotelPriceDto -> {
                HotelPriceResponseDto hotelPriceResponseDto = modelMapper.map(hotelPriceDto.getHotel(), HotelPriceResponseDto.class);
                hotelPriceResponseDto.setPrice(hotelPriceDto.getPrice());
                return hotelPriceResponseDto;
            });
        }

        //logic for date exceeding beyond SEARCH_DAYS_LIMIT days.
        Page<HotelPriceDto> hotelPage = inventoryRepository.findHotelsWithAvailableInventory(hotelSearchRequest.getCity(),hotelSearchRequest.getStartDate(),
                hotelSearchRequest.getEndDate(),hotelSearchRequest.getRoomsCount(),
                dateCount,hotelSearchRequest.getStar(),pageable);

        return hotelPage.map(hotelPriceDto -> {
            HotelPriceResponseDto hotelPriceResponseDto = modelMapper.map(hotelPriceDto.getHotel(), HotelPriceResponseDto.class);
            hotelPriceResponseDto.setPrice(hotelPriceDto.getPrice());
            return hotelPriceResponseDto;
        });
    }

    @Override
    public List<InventoryDTO> getAllInventoryByRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(()->new ResourceNotFoundException("Room not found with id "+roomId));

        User user = getCurrentUser();

        if(!user.equals(room.getHotel().getOwner())) throw new AccessDeniedException("No access to the resources for hotel "+room.getHotel().getId());

        return inventoryRepository.findByRoomOrderByDate(room)
                .stream()
                .map((element)->modelMapper.map(element, InventoryDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateInventory(Long roomId, UpdateInventoryRequestDTO updateInventoryRequestDTO) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(()->new ResourceNotFoundException("Room not found with id "+roomId));

        User user = getCurrentUser();

        if(!user.equals(room.getHotel().getOwner())) throw new AccessDeniedException("No access to the resources for hotel "+room.getHotel().getId());

        inventoryRepository.getInventoryAndLockBeforeUpdate(roomId,updateInventoryRequestDTO.getStartDate(),updateInventoryRequestDTO.getEndDate());

        inventoryRepository.updateInventory(roomId,updateInventoryRequestDTO.getStartDate(),
                updateInventoryRequestDTO.getEndDate(), updateInventoryRequestDTO.getClosed(),
                updateInventoryRequestDTO.getSurgeFactor());

    }

    @Override
    public void updatePriceByRoom(Room room) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusYears(1);

        inventoryRepository.getInventoryAndLockBeforeUpdate(room.getId(),today,endDate);
        inventoryRepository.updatePriceByRoom(room.getId(),room.getBasePrice());
    }

    @Override
    public void updateRoomCountByRoom(Room room) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusYears(1);

        List<Inventory> inv = inventoryRepository.getInventoryAndLockBeforeUpdate(room.getId(), today,endDate);

        List<LocalDate> validDates = new ArrayList<>();
        for (Inventory inventory : inv) {
            int bookedPlusReserved = inventory.getBookedCount() + inventory.getReservedCount();
            if (room.getTotalCount() >= bookedPlusReserved) {
                validDates.add(inventory.getDate());
            }
        }

        inventoryRepository.updateCountByRoom(room.getId(), room.getTotalCount(), validDates);
    }
}
