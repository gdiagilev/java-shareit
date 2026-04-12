package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BookingDto createBooking(Long userId, BookingDto dto) {

        var booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        var item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new NotFoundException("Item not found"));

        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Owner cannot book own item");
        }

        if (!item.getAvailable()) {
            throw new IllegalArgumentException("Item not available");
        }

        if (dto.getStart() == null || dto.getEnd() == null ||
                !dto.getEnd().isAfter(dto.getStart())) {
            throw new IllegalArgumentException("Invalid booking time");
        }

        // DO NOT check if start/end is in the past - gateway handles that

        var booking = Booking.builder()
                .start(dto.getStart())
                .end(dto.getEnd())
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        return BookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDto approveBooking(Long ownerId, Long bookingId, boolean approved) {
        var booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Only owner can approve booking");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new IllegalArgumentException("Booking already processed");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        return BookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto getBookingById(Long userId, Long bookingId) {

        var booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Access denied");
        }

        return BookingMapper.toDto(booking);
    }

    @Override
    public List<BookingDto> getUserBookings(Long userId, BookingState state) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        LocalDateTime now = LocalDateTime.now();

        return switch (state) {
            case CURRENT -> bookingRepository
                    .findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now)
                    .stream().map(BookingMapper::toDto).toList();

            case PAST -> bookingRepository
                    .findByBookerIdAndEndBeforeOrderByStartDesc(userId, now)
                    .stream().map(BookingMapper::toDto).toList();

            case FUTURE -> bookingRepository
                    .findByBookerIdAndStartAfterOrderByStartDesc(userId, now)
                    .stream().map(BookingMapper::toDto).toList();

            case WAITING -> bookingRepository
                    .findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING)
                    .stream().map(BookingMapper::toDto).toList();

            case REJECTED -> bookingRepository
                    .findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED)
                    .stream().map(BookingMapper::toDto).toList();

            case ALL -> bookingRepository
                    .findByBookerIdOrderByStartDesc(userId)
                    .stream().map(BookingMapper::toDto).toList();
        };
    }

    @Override
    public List<BookingDto> getOwnerBookings(Long ownerId, BookingState state) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        LocalDateTime now = LocalDateTime.now();

        return switch (state) {

            case CURRENT -> bookingRepository
                    .findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(ownerId, now, now)
                    .stream()
                    .map(BookingMapper::toDto)
                    .toList();

            case PAST -> bookingRepository
                    .findByItemOwnerIdAndEndBeforeOrderByStartDesc(ownerId, now)
                    .stream()
                    .map(BookingMapper::toDto)
                    .toList();

            case FUTURE -> bookingRepository
                    .findByItemOwnerIdAndStartAfterOrderByStartDesc(ownerId, now)
                    .stream()
                    .map(BookingMapper::toDto)
                    .toList();

            case WAITING -> bookingRepository
                    .findByItemOwnerIdAndStatusOrderByStartDesc(ownerId, BookingStatus.WAITING)
                    .stream()
                    .map(BookingMapper::toDto)
                    .toList();

            case REJECTED -> bookingRepository
                    .findByItemOwnerIdAndStatusOrderByStartDesc(ownerId, BookingStatus.REJECTED)
                    .stream()
                    .map(BookingMapper::toDto)
                    .toList();

            case ALL -> bookingRepository
                    .findByItemOwnerIdOrderByStartDesc(ownerId)
                    .stream()
                    .map(BookingMapper::toDto)
                    .toList();
        };
    }
}