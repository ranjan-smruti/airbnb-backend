package com.codingshuttle.projects.airBnbApp.Repository;

import com.codingshuttle.projects.airBnbApp.Entity.Hotel;
import com.codingshuttle.projects.airBnbApp.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel,Long> {
    List<Hotel> findByOwner(User user);
}
