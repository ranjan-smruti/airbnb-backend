package com.codingshuttle.projects.airBnbApp.Repository;

import com.codingshuttle.projects.airBnbApp.Entity.Hotel;
import com.codingshuttle.projects.airBnbApp.Entity.Inventory;
import com.codingshuttle.projects.airBnbApp.Entity.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory,Long> {
    void deleteByRoom(Room room);

    //HAVING COUNT(i.date) = :dateCount ==> it may happen where
    //that multiple hotels have desired number of rooms available, but for a particular date
    //range there maybe only 1 available , so it will not get fetch in the search
    //only date with two rooms available will be shown .
    @Query("""
             SELECT DISTINCT i.hotel
             FROM Inventory i
             WHERE i.city = :city
                 AND i.date BETWEEN :startDate AND :endDate
                 AND i.closed = false
                 AND (i.totalCount - i.bookedCount - i.reservedCount) >= :roomsCount
                 GROUP BY i.hotel, i.room
                 HAVING COUNT(i.date) = :dateCount
           """)
    Page<Hotel> findHotelsWithAvailableInventory(
            @Param("city") String city,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Integer roomsCount,
            @Param("dateCount") Long dateCount,
            Pageable pageable
    );

    @Query("""
            SELECT i
            FROM Inventory i
            WHERE i.room.id = :roomId
                AND i.date BETWEEN :startDate AND :endDate
                 AND i.closed = false
                 AND (i.totalCount - i.bookedCount - i.reservedCount) >= :roomsCount
            """)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Inventory> findAndLockAvailableInventory(
          @Param("roomId") Long roomId,
          @Param("startDate") LocalDate startDate,
          @Param("endDate") LocalDate endDate,
          @Param("roomsCount") Integer roomsCount
    );

}
