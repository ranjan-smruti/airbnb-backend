package com.codingshuttle.projects.airBnbApp.Repository;

import com.codingshuttle.projects.airBnbApp.DTO.HotelPriceDto;
import com.codingshuttle.projects.airBnbApp.DTO.RoomPriceDto;
import com.codingshuttle.projects.airBnbApp.Entity.Hotel;
import com.codingshuttle.projects.airBnbApp.Entity.Inventory;
import com.codingshuttle.projects.airBnbApp.Entity.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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
             SELECT new com.codingshuttle.projects.airBnbApp.DTO.HotelPriceDto(i.hotel, AVG(i.price))
             FROM Inventory i
             WHERE i.city = :city
                 AND i.date BETWEEN :startDate AND :endDate
                 AND i.closed = false
                 AND (i.totalCount - i.bookedCount - i.reservedCount) >= :roomsCount
                 AND (:star IS NULL OR i.hotel.star IN :star)
                  AND (:ratings IS NULL OR i.hotel.rating IN :ratings)
                  AND (:lowPrice IS NULL OR i.price >= :lowPrice)
                  AND (:highPrice IS NULL OR i.price <= :highPrice)
                 GROUP BY i.hotel, i.room
                 HAVING COUNT(i.date) = :dateCount
           """)
    Page<HotelPriceDto> findHotelsWithAvailableInventory(
            @Param("city") String city,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Integer roomsCount,
            @Param("dateCount") Long dateCount,
            @Param("star") List<Integer> star,
            @Param("ratings") List<BigDecimal> ratings,
            @Param("lowPrice") BigDecimal lowPrice,
            @Param("highPrice") BigDecimal highPrice,
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

    @Query("""
            SELECT i
            FROM Inventory i
            WHERE i.room.id = :roomId
                AND i.date BETWEEN :startDate AND :endDate
                AND (i.totalCount - i.bookedCount) >= :numberOfRooms
                AND i.closed = false
            """)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Inventory> findAndLockReservedInventory(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("numberOfRooms") int numberOfRooms);

    @Modifying
    @Query("""
            UPDATE Inventory i
            SET i.reservedCount = i.reservedCount - :numberOfRooms,
                i.bookedCount = i.bookedCount + :numberOfRooms
            WHERE i.room.id = :roomId
                AND i.date BETWEEN :startDate AND :endDate
                AND (i.totalCount - i.bookedCount) >= :numberOfRooms
                AND i.reservedCount >= :numberOfRooms
                AND i.closed = false
            """)
    void confirmBooking(@Param("roomId") Long roomId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate,
                        @Param("numberOfRooms") int numberOfRooms);

    @Modifying
    @Query("""
                UPDATE Inventory i
                SET i.bookedCount = i.bookedCount - :numberOfRooms
                WHERE i.room.id = :roomId
                  AND i.date BETWEEN :startDate AND :endDate
                  AND (i.totalCount - i.bookedCount) >= :numberOfRooms
                  AND i.closed = false
            """)
    void cancelBooking(@Param("roomId") Long roomId,
                       @Param("startDate") LocalDate startDate,
                       @Param("endDate") LocalDate endDate,
                       @Param("numberOfRooms") int numberOfRooms);

    @Modifying
    @Query("""
                UPDATE Inventory i
                SET i.reservedCount = i.reservedCount + :numberOfRooms
                WHERE i.room.id = :roomId
                  AND i.date BETWEEN :startDate AND :endDate
                  AND (i.totalCount - i.bookedCount - i.reservedCount) >= :numberOfRooms
                  AND i.closed = false
            """)
    void initBooking(@Param("roomId") Long roomId,
                     @Param("startDate") LocalDate startDate,
                     @Param("endDate") LocalDate endDate,
                     @Param("numberOfRooms") int numberOfRooms);

    @Modifying
    @Query("""
            UPDATE Inventory i
            SET i.reservedCount = i.reservedCount - :numberOfRooms
            WHERE i.room.id = :roomId
              AND i.date BETWEEN :startDate AND :endDate
              AND i.reservedCount >= :numberOfRooms
              AND i.closed = false
        """)
    void releaseReservedInventory(@Param("roomId") Long roomId,
                     @Param("startDate") LocalDate startDate,
                     @Param("endDate") LocalDate endDate,
                     @Param("numberOfRooms") int numberOfRooms);

    @Query("""
                SELECT i
                FROM Inventory i
                WHERE i.room.id = :roomId
                  AND i.date BETWEEN :startDate AND :endDate
            """)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Inventory> getInventoryAndLockBeforeUpdate(@Param("roomId") Long roomId,
                                                 @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);

    @Modifying
    @Query("""
                UPDATE Inventory i
                SET i.surgeFactor = :surgeFactor,
                    i.closed = :closed
                WHERE i.room.id = :roomId
                AND i.date BETWEEN :startDate AND :endDate
            """)
    void updateInventory(@Param("roomId") Long roomId,
                       @Param("startDate") LocalDate startDate,
                       @Param("endDate") LocalDate endDate,
                       @Param("closed") boolean closed,
                       @Param("surgeFactor") BigDecimal surgeFactor);

    @Modifying
    @Query("""
            UPDATE Inventory i
            SET i.price = :newPrice
            WHERE i.room.id = :roomId
            """)
    void updatePriceByRoom(@Param("roomId") Long roomId,
                           @Param("newPrice") BigDecimal newPrice);

    @Modifying
    @Query("""
            UPDATE Inventory i
            SET i.totalCount = :newTotalCount
            WHERE i.room.id = :roomId
            AND i.date IN :dates
            """)
    void updateCountByRoom(@Param("roomId") Long roomId,
                           @Param("newTotalCount") Integer newTotalCount,
                           @Param("dates") List<LocalDate> dates);

    @Query("""
       SELECT new com.codingshuttle.projects.airBnbApp.DTO.RoomPriceDto(
            i.room,
            CASE
                WHEN COUNT(i) = :dateCount THEN AVG(i.price)
                ELSE NULL
            END
        )
       FROM Inventory i
       WHERE i.hotel.id = :hotelId
             AND i.date BETWEEN :startDate AND :endDate
             AND (i.totalCount - i.bookedCount) >= :roomsCount
             AND i.closed = false
       GROUP BY i.room
       """)
    List<RoomPriceDto> findRoomAveragePrice(
            @Param("hotelId") Long hotelId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Long roomsCount,
            @Param("dateCount") Long dateCount
    );

    List<Inventory> findByHotelAndDateBetween(Hotel hotel, LocalDate startDate, LocalDate endDate);

    List<Inventory> findByRoomOrderByDate(Room room);
}
