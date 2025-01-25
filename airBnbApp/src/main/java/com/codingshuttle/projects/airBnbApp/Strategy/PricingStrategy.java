package com.codingshuttle.projects.airBnbApp.Strategy;

import com.codingshuttle.projects.airBnbApp.Entity.Inventory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

public interface PricingStrategy {
    BigDecimal calculatePrice(Inventory inventory);
}
