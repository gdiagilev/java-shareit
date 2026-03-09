package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long bookerId,
            LocalDateTime now1,
            LocalDateTime now2
    );

    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(
            Long bookerId,
            LocalDateTime now
    );

    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(
            Long bookerId,
            LocalDateTime now
    );

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(
            Long bookerId,
            BookingStatus status
    );

    List<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId);
}