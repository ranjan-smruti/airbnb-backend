package com.codingshuttle.projects.airBnbApp.Repository;

import com.codingshuttle.projects.airBnbApp.Entity.Guest;
import com.codingshuttle.projects.airBnbApp.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuestRepository extends JpaRepository<Guest, Long> {
    List<Guest> findByUser(User user);
    boolean existsByNameIgnoreCaseAndUser(String guestName, User user);
}
