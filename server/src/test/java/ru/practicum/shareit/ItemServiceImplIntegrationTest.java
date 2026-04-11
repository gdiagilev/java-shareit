package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.CommentRepository;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
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
class ItemServiceImplIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommentRepository commentRepository;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner@test.com")
                .build());

        booker = userRepository.save(User.builder()
                .name("Booker")
                .email("booker@test.com")
                .build());

        item = itemRepository.save(Item.builder()
                .name("Drill")
                .description("A powerful drill")
                .available(true)
                .owner(owner)
                .build());
    }

    @Test
    void createItem_shouldSaveAndReturnDto() {
        ItemDto dto = ItemDto.builder()
                .name("Hammer")
                .description("Heavy hammer")
                .available(true)
                .build();

        ItemDto result = itemService.createItem(owner.getId(), dto);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Hammer");
        assertThat(result.getDescription()).isEqualTo("Heavy hammer");
        assertThat(result.getAvailable()).isTrue();
    }

    @Test
    void createItem_withUnknownUser_shouldThrowNotFoundException() {
        ItemDto dto = ItemDto.builder()
                .name("Hammer")
                .description("Heavy hammer")
                .available(true)
                .build();

        assertThatThrownBy(() -> itemService.createItem(999L, dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getUserItems_shouldReturnAllOwnerItems() {
        itemRepository.save(Item.builder()
                .name("Saw")
                .description("A sharp saw")
                .available(true)
                .owner(owner)
                .build());

        List<ItemDto> items = itemService.getUserItems(owner.getId());

        assertThat(items).hasSize(2);
        assertThat(items).extracting("name").containsExactlyInAnyOrder("Drill", "Saw");
    }

    @Test
    void getUserItems_shouldIncludeLastAndNextBookings() {
        LocalDateTime now = LocalDateTime.now();

        bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .start(now.minusDays(2))
                .end(now.minusDays(1))
                .status(BookingStatus.APPROVED)
                .build());

        bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .status(BookingStatus.APPROVED)
                .build());

        List<ItemDto> items = itemService.getUserItems(owner.getId());

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getLastBooking()).isNotNull();
        assertThat(items.get(0).getNextBooking()).isNotNull();
    }

    @Test
    void getItemById_asOwner_shouldIncludeBookings() {
        LocalDateTime now = LocalDateTime.now();

        bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .start(now.minusDays(2))
                .end(now.minusDays(1))
                .status(BookingStatus.APPROVED)
                .build());

        ItemDto result = itemService.getItemById(owner.getId(), item.getId());

        assertThat(result).isNotNull();
        assertThat(result.getLastBooking()).isNotNull();
        assertThat(result.getNextBooking()).isNull();
    }

    @Test
    void getItemById_asBooker_shouldNotIncludeBookings() {
        ItemDto result = itemService.getItemById(booker.getId(), item.getId());

        assertThat(result.getLastBooking()).isNull();
        assertThat(result.getNextBooking()).isNull();
    }

    @Test
    void getItemById_withUnknownId_shouldThrowNotFoundException() {
        assertThatThrownBy(() -> itemService.getItemById(owner.getId(), 999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateItem_shouldUpdateFields() {
        ItemDto update = ItemDto.builder()
                .name("Updated Drill")
                .description("Updated description")
                .available(false)
                .build();

        ItemDto result = itemService.updateItem(owner.getId(), item.getId(), update);

        assertThat(result.getName()).isEqualTo("Updated Drill");
        assertThat(result.getDescription()).isEqualTo("Updated description");
        assertThat(result.getAvailable()).isFalse();
    }

    @Test
    void updateItem_byNonOwner_shouldThrowNotFoundException() {
        ItemDto update = ItemDto.builder().name("Hacked").build();

        assertThatThrownBy(() -> itemService.updateItem(booker.getId(), item.getId(), update))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void searchItems_shouldReturnMatchingAvailableItems() {
        List<ItemDto> results = itemService.searchItems("drill");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Drill");
    }

    @Test
    void searchItems_withBlankText_shouldReturnEmptyList() {
        List<ItemDto> results = itemService.searchItems("  ");

        assertThat(results).isEmpty();
    }

    @Test
    void addComment_afterCompletedBooking_shouldSaveComment() {
        LocalDateTime now = LocalDateTime.now();

        bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .start(now.minusDays(3))
                .end(now.minusDays(1))
                .status(BookingStatus.APPROVED)
                .build());

        CommentDto commentDto = CommentDto.builder().text("Great drill!").build();
        CommentDto result = itemService.addComment(booker.getId(), item.getId(), commentDto);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getText()).isEqualTo("Great drill!");
        assertThat(result.getAuthorName()).isEqualTo("Booker");
        assertThat(result.getCreated()).isNotNull();
    }

    @Test
    void addComment_withoutCompletedBooking_shouldThrowValidationException() {
        CommentDto commentDto = CommentDto.builder().text("No booking").build();

        assertThatThrownBy(() -> itemService.addComment(booker.getId(), item.getId(), commentDto))
                .isInstanceOf(jakarta.validation.ValidationException.class);
    }
}
