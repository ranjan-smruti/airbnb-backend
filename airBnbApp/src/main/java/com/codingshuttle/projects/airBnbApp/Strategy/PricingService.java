package com.codingshuttle.projects.airBnbApp.Strategy;

import com.codingshuttle.projects.airBnbApp.Entity.Inventory;
import com.codingshuttle.projects.airBnbApp.Service.HolidayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PricingService {
    private final HolidayService holidayService;
    @Autowired
    public PricingService(HolidayService holidayService) {
        this.holidayService = holidayService;
    }

    public BigDecimal calculateDynamicPricing(Inventory inventory){
        PricingStrategy pricingStrategy = new BasePricingStrategy();

        //apply the addition strategies
        pricingStrategy = new SurgePricingStrategy(pricingStrategy);
        pricingStrategy = new OccupancyPricingStrategy(pricingStrategy);
        pricingStrategy = new UrgencyPricingStrategy(pricingStrategy);
        pricingStrategy = new HolidayPricingStrategy(pricingStrategy,holidayService);

        return pricingStrategy.calculatePrice(inventory);
    }
}
