package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingDto createBooking(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @Valid @RequestBody BookingDto bookingDto) {

        log.info("Получен запрос на создание бронирования от пользователя id={}", userId);
        BookingDto createdBooking = bookingService.createBooking(userId, bookingDto);
        log.info("Бронирование успешно создано: {}", createdBooking);
        return createdBooking;
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approveBooking(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long bookingId,
            @RequestParam boolean approved) {

        log.info("Пользователь id={} пытается {} бронирование id={}", userId,
                approved ? "одобрить" : "отклонить", bookingId);
        BookingDto updatedBooking = bookingService.approveBooking(userId, bookingId, approved);
        log.info("Бронирование id={} обновлено. Новый статус: {}", bookingId, updatedBooking);
        return updatedBooking;
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingById(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long bookingId) {

        log.info("Пользователь id={} запрашивает бронирование id={}", userId, bookingId);
        BookingDto booking = bookingService.getBookingById(userId, bookingId);
        log.info("Бронирование получено: {}", booking);
        return booking;
    }

    @GetMapping
    public List<BookingDto> getUserBookings(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "ALL") String state) {

        BookingState bookingState = BookingState.from(state);
        log.info("Запрос всех бронирований пользователя id={} со статусом {}", userId, bookingState);

        List<BookingDto> bookings = bookingService.getUserBookings(userId, bookingState);

        log.info("Найдено {} бронирований для пользователя id={}", bookings.size(), userId);
        return bookings;
    }

    @GetMapping("/owner")
    public List<BookingDto> getOwnerBookings(
            @RequestHeader("X-Sharer-User-Id") Long ownerId,
            @RequestParam(defaultValue = "ALL") String state) {

        BookingState bookingState = BookingState.from(state);

        log.info("Запрос всех бронирований владельца id={} со статусом {}", ownerId, bookingState);

        List<BookingDto> bookings = bookingService.getOwnerBookings(ownerId, bookingState);

        log.info("Найдено {} бронирований для владельца id={}", bookings.size(), ownerId);

        return bookings;
    }
}