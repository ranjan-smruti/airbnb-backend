package com.codingshuttle.projects.airBnbApp.Service;

import com.codingshuttle.projects.airBnbApp.Entity.Hotel;
import com.codingshuttle.projects.airBnbApp.Entity.HotelMinPrice;
import com.codingshuttle.projects.airBnbApp.Entity.Inventory;
import com.codingshuttle.projects.airBnbApp.Repository.HotelMinPriceRepository;
import com.codingshuttle.projects.airBnbApp.Repository.HotelRepository;
import com.codingshuttle.projects.airBnbApp.Repository.InventoryRepository;
import com.codingshuttle.projects.airBnbApp.Strategy.PricingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PricingUpdateService {
    //Scheduler to update the inventory and hotel min price table in every 1hr.
    private final HotelRepository hotelRepository;
    private final InventoryRepository inventoryRepository;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    private final PricingService pricingService;

//    @Scheduled(cron="*/5 * * * * *") //Every 5sec
    @Scheduled(cron="0 0 * * * *") //Every 1 hour
    public void updatePrices(){
        int page = 0;
        int batchSize = 100;

        while(true){
            Page<Hotel> hotelPage = hotelRepository.findAll(PageRequest.of(page,batchSize));
            if(hotelPage.isEmpty()){
                break;
            }

            hotelPage.getContent().forEach(this::updateHotelPrices);

            page++;
        }
    }

    private void updateHotelPrices(Hotel hotel){
        log.info("Updating hotel prices for hotel ID: {}",hotel.getId());
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusYears(1);

        List<Inventory> inventoryList = inventoryRepository.findByHotelAndDateBetween(hotel,startDate,endDate);

        //individual room prices for a day will be get updated in inventory table
        updateInventoryPrices(inventoryList);

        //avg minimum room price of a hotel of a particular day will get updated in the hotel_min_price table
        updateHotelMinPrices(hotel, inventoryList, startDate,endDate);
    }

    private void updateHotelMinPrices(Hotel hotel, List<Inventory> inventoryList, LocalDate startDate, LocalDate endDate) {
        //Compute minimum price per day for the hotel
        Map<LocalDate,BigDecimal> dailyPrice = inventoryList.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getDate,
                        Collectors.mapping(Inventory::getPrice, Collectors.minBy(Comparator.naturalOrder()))
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,e->e.getValue().orElse(BigDecimal.ZERO)));

        //Prepare HotelPrice entries in bluk
        List<HotelMinPrice> hotelPrices = new ArrayList<>();
        dailyPrice.forEach((date,price)->{
            HotelMinPrice hotelMinPrice = hotelMinPriceRepository.findByHotelAndDate(hotel,date).orElse(new HotelMinPrice(hotel,date));
            hotelMinPrice.setPrice(price);
            hotelPrices.add(hotelMinPrice);
        });

        //save all HotelPrice entries in bulk
        hotelMinPriceRepository.saveAll(hotelPrices);
    }

    private void updateInventoryPrices(List<Inventory> inventoryList){
       inventoryList.forEach(inventory -> {
           BigDecimal dynamicPrice = pricingService.calculateDynamicPricing(inventory);
           inventory.setPrice(dynamicPrice);
       });
       inventoryRepository.saveAll(inventoryList);
    }
}
