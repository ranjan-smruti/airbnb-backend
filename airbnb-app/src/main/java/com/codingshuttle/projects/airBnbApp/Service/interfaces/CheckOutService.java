package com.codingshuttle.projects.airBnbApp.Service.interfaces;

import com.codingshuttle.projects.airBnbApp.Entity.Booking;

public interface CheckOutService {
    String getCheckoutSession(Booking booking, String successUrl, String failureUrl);
}
