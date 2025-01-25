package com.codingshuttle.projects.airBnbApp.Strategy;

import com.codingshuttle.projects.airBnbApp.Entity.Inventory;
import com.codingshuttle.projects.airBnbApp.Service.HolidayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class HolidayPricingStrategy implements PricingStrategy{
    private final PricingStrategy wrapped;
    private final HolidayService holidayService;

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        BigDecimal price = wrapped.calculatePrice(inventory);

       //call an API to check if today is holiday or not else create a constant array to store the list of the holiday.

        //calendarific API code
        //boolean isTodayHoliday = holidayService.isTodayHoliday("IN");

        //constant ARRAY
        boolean isTodayHoliday = holidayService.isHoliday(inventory.getDate());

        if(isTodayHoliday){
            price = price.multiply(BigDecimal.valueOf(1.5));
        }
        return price;
    }
}
