package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class DtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> bookingDtoTester;

    @Autowired
    private JacksonTester<CommentDto> commentDtoTester;

    @Autowired
    private JacksonTester<ItemDto> itemDtoTester;

    @Autowired
    private JacksonTester<ItemRequestDto> itemRequestDtoTester;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Test
    void bookingDto_serialization_shouldFormatDatesCorrectly() throws Exception {
        LocalDateTime start = LocalDateTime.of(2025, 6, 1, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 6, 2, 10, 0, 0);

        BookingDto dto = BookingDto.builder()
                .id(1L)
                .itemId(2L)
                .start(start)
                .end(end)
                .status(BookingStatus.WAITING)
                .build();

        var json = bookingDtoTester.write(dto);

        assertThat(json).extractingJsonPathStringValue("$.start")
                .isEqualTo("2025-06-01T10:00:00");
        assertThat(json).extractingJsonPathStringValue("$.end")
                .isEqualTo("2025-06-02T10:00:00");
        assertThat(json).extractingJsonPathStringValue("$.status")
                .isEqualTo("WAITING");
    }

    @Test
    void bookingDto_deserialization_shouldParseDatesCorrectly() throws Exception {
        String json = """
                {
                    "id": 1,
                    "itemId": 2,
                    "start": "2025-06-01T10:00:00",
                    "end": "2025-06-02T10:00:00",
                    "status": "APPROVED"
                }
                """;

        BookingDto dto = bookingDtoTester.parseObject(json);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getItemId()).isEqualTo(2L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2025, 6, 1, 10, 0, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2025, 6, 2, 10, 0, 0));
        assertThat(dto.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void bookingDto_withNullDates_shouldSerializeAsNull() throws Exception {
        BookingDto dto = BookingDto.builder()
                .id(1L)
                .itemId(2L)
                .build();

        var json = bookingDtoTester.write(dto);

        assertThat(json).extractingJsonPathStringValue("$.start").isNull();
        assertThat(json).extractingJsonPathStringValue("$.end").isNull();
    }


    @Test
    void commentDto_serialization_shouldIncludeAllFields() throws Exception {
        LocalDateTime created = LocalDateTime.of(2025, 5, 1, 12, 0, 0);

        CommentDto dto = CommentDto.builder()
                .id(1L)
                .text("Great item!")
                .authorName("Alice")
                .created(created)
                .build();

        var json = commentDtoTester.write(dto);

        assertThat(json).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(json).extractingJsonPathStringValue("$.text").isEqualTo("Great item!");
        assertThat(json).extractingJsonPathStringValue("$.authorName").isEqualTo("Alice");
        assertThat(json).extractingJsonPathStringValue("$.created")
                .isEqualTo("2025-05-01T12:00:00");
    }

    @Test
    void commentDto_deserialization_shouldParseCreatedDate() throws Exception {
        String json = """
                {
                    "id": 5,
                    "text": "Excellent!",
                    "authorName": "Bob",
                    "created": "2025-05-15T09:30:00"
                }
                """;

        CommentDto dto = commentDtoTester.parseObject(json);

        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto.getText()).isEqualTo("Excellent!");
        assertThat(dto.getAuthorName()).isEqualTo("Bob");
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2025, 5, 15, 9, 30, 0));
    }


    @Test
    void itemRequestDto_serialization_shouldIncludeCreatedAndItems() throws Exception {
        LocalDateTime created = LocalDateTime.of(2025, 4, 10, 8, 0, 0);

        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(1L);
        dto.setDescription("Need a drill");
        dto.setCreated(created);

        var json = itemRequestDtoTester.write(dto);

        assertThat(json).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(json).extractingJsonPathStringValue("$.description")
                .isEqualTo("Need a drill");
        assertThat(json).extractingJsonPathStringValue("$.created")
                .isEqualTo("2025-04-10T08:00:00");
        assertThat(json).extractingJsonPathArrayValue("$.items").isEmpty();
    }

    @Test
    void itemRequestDto_deserialization_shouldParseDescription() throws Exception {
        String json = """
                {
                    "description": "Looking for a ladder",
                    "created": "2025-04-20T14:00:00"
                }
                """;

        ItemRequestDto dto = itemRequestDtoTester.parseObject(json);

        assertThat(dto.getDescription()).isEqualTo("Looking for a ladder");
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2025, 4, 20, 14, 0, 0));
    }


    @Test
    void itemDto_serialization_shouldIncludeAllFields() throws Exception {
        ItemDto dto = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description("A powerful drill")
                .available(true)
                .requestId(5L)
                .build();

        var json = itemDtoTester.write(dto);

        assertThat(json).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(json).extractingJsonPathStringValue("$.name").isEqualTo("Drill");
        assertThat(json).extractingJsonPathStringValue("$.description")
                .isEqualTo("A powerful drill");
        assertThat(json).extractingJsonPathBooleanValue("$.available").isTrue();
        assertThat(json).extractingJsonPathNumberValue("$.requestId").isEqualTo(5);
    }

    @Test
    void itemDto_deserialization_shouldParseRequestId() throws Exception {
        String json = """
                {
                    "name": "Hammer",
                    "description": "Heavy hammer",
                    "available": false,
                    "requestId": 3
                }
                """;

        ItemDto dto = itemDtoTester.parseObject(json);

        assertThat(dto.getName()).isEqualTo("Hammer");
        assertThat(dto.getAvailable()).isFalse();
        assertThat(dto.getRequestId()).isEqualTo(3L);
    }

    @Test
    void itemDto_withNullRequestId_shouldSerializeAsNull() throws Exception {
        ItemDto dto = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description("A drill")
                .available(true)
                .build();

        var json = itemDtoTester.write(dto);

        assertThat(json).extractingJsonPathStringValue("$.requestId").isNull();
    }
}
