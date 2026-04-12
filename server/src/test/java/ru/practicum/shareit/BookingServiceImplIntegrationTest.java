package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = ru.practicum.shareit.ShareItApp.class)
@ActiveProfiles("test")
@Transactional
class BookingServiceImplIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner@booking-test.com")
                .build());

        booker = userRepository.save(User.builder()
                .name("Booker")
                .email("booker@booking-test.com")
                .build());

        item = itemRepository.save(Item.builder()
                .name("Drill")
                .description("A powerful drill")
                .available(true)
                .owner(owner)
                .build());
    }

    @Test
    void createBooking_shouldSaveAndReturnDto() {
        BookingDto dto = BookingDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        BookingDto result = bookingService.createBooking(booker.getId(), dto);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(result.getItem().getId()).isEqualTo(item.getId());
        assertThat(result.getBooker().getId()).isEqualTo(booker.getId());
    }

    @Test
    void createBooking_byOwner_shouldThrowNotFoundException() {
        BookingDto dto = BookingDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        assertThatThrownBy(() -> bookingService.createBooking(owner.getId(), dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void createBooking_unavailableItem_shouldThrowValidationException() {
        item.setAvailable(false);
        itemRepository.save(item);

        BookingDto dto = BookingDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        assertThatThrownBy(() -> bookingService.createBooking(booker.getId(), dto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void approveBooking_byOwner_shouldChangeStatusToApproved() {
        Booking booking = bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .build());

        BookingDto result = bookingService.approveBooking(owner.getId(), booking.getId(), true);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void approveBooking_withRejected_shouldChangeStatusToRejected() {
        Booking booking = bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .build());

        BookingDto result = bookingService.approveBooking(owner.getId(), booking.getId(), false);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void approveBooking_byNonOwner_shouldThrowForbiddenException() {
        Booking booking = bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .build());

        assertThatThrownBy(() -> bookingService.approveBooking(booker.getId(), booking.getId(), true))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void approveBooking_alreadyApproved_shouldThrowIllegalArgumentException() {
        Booking booking = bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.APPROVED)
                .build());

        assertThatThrownBy(() -> bookingService.approveBooking(owner.getId(), booking.getId(), true))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getBookingById_byBooker_shouldReturnBooking() {
        Booking booking = bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .build());

        BookingDto result = bookingService.getBookingById(booker.getId(), booking.getId());

        assertThat(result.getId()).isEqualTo(booking.getId());
    }

    @Test
    void getBookingById_byOwner_shouldReturnBooking() {
        Booking booking = bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .build());

        BookingDto result = bookingService.getBookingById(owner.getId(), booking.getId());

        assertThat(result.getId()).isEqualTo(booking.getId());
    }

    @Test
    void getBookingById_byStranger_shouldThrowNotFoundException() {
        User stranger = userRepository.save(User.builder()
                .name("Stranger")
                .email("stranger@test.com")
                .build());

        Booking booking = bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .build());

        assertThatThrownBy(() -> bookingService.getBookingById(stranger.getId(), booking.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getUserBookings_withStateAll_shouldReturnAllBookings() {
        bookingRepository.save(Booking.builder()
                .item(item).booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING).build());

        List<BookingDto> results = bookingService.getUserBookings(booker.getId(), BookingState.ALL);

        assertThat(results).hasSize(1);
    }

    @Test
    void getOwnerBookings_withStateAll_shouldReturnAllBookings() {
        bookingRepository.save(Booking.builder()
                .item(item).booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING).build());

        List<BookingDto> results = bookingService.getOwnerBookings(owner.getId(), BookingState.ALL);

        assertThat(results).hasSize(1);
    }

    @Test
    void getUserBookings_withStatePast_shouldReturnPastBookings() {
        bookingRepository.save(Booking.builder()
                .item(item).booker(booker)
                .start(LocalDateTime.now().minusDays(3))
                .end(LocalDateTime.now().minusDays(1))
                .status(BookingStatus.APPROVED).build());

        List<BookingDto> results = bookingService.getUserBookings(booker.getId(), BookingState.PAST);

        assertThat(results).hasSize(1);
    }

    @Test
    void getUserBookings_withUnknownUser_shouldThrowNotFoundException() {
        assertThatThrownBy(() -> bookingService.getUserBookings(999L, BookingState.ALL))
                .isInstanceOf(NotFoundException.class);
    }
}
