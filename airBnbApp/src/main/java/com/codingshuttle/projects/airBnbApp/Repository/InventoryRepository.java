package com.codingshuttle.projects.airBnbApp.Repository;

import com.codingshuttle.projects.airBnbApp.Entity.Inventory;
import com.codingshuttle.projects.airBnbApp.Entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory,Long> {
    void deleteByDateAfterAndRoom(LocalDate date, Room room);
}
