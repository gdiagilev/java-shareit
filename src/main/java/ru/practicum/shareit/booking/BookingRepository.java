package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
           SELECT b
           FROM Booking b
           WHERE b.booker.id = :bookerId
           ORDER BY b.start DESC
           """)
    List<Booking> findByBookerIdOrderByStartDesc(@Param("bookerId") Long bookerId);


    @Query("""
           SELECT b
           FROM Booking b
           WHERE b.booker.id = :bookerId
           AND b.start < :now
           AND b.end > :now
           ORDER BY b.start DESC
           """)
    List<Booking> findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
            @Param("bookerId") Long bookerId,
            @Param("now") LocalDateTime now1,
            @Param("now") LocalDateTime now2
    );


    @Query("""
           SELECT b
           FROM Booking b
           WHERE b.booker.id = :bookerId
           AND b.end < :now
           ORDER BY b.start DESC
           """)
    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(
            @Param("bookerId") Long bookerId,
            @Param("now") LocalDateTime now
    );


    @Query("""
           SELECT b
           FROM Booking b
           WHERE b.booker.id = :bookerId
           AND b.start > :now
           ORDER BY b.start DESC
           """)
    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(
            @Param("bookerId") Long bookerId,
            @Param("now") LocalDateTime now
    );


    @Query("""
           SELECT b
           FROM Booking b
           WHERE b.booker.id = :bookerId
           AND b.status = :status
           ORDER BY b.start DESC
           """)
    List<Booking> findByBookerIdAndStatusOrderByStartDesc(
            @Param("bookerId") Long bookerId,
            @Param("status") BookingStatus status
    );


    @Query("""
           SELECT b
           FROM Booking b
           WHERE b.item.owner.id = :ownerId
           ORDER BY b.start DESC
           """)
    List<Booking> findByItemOwnerIdOrderByStartDesc(@Param("ownerId") Long ownerId);
}