package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.user.dto.UserShortDto;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private BookingDto makeBookingDto(Long id, BookingStatus status) {
        return BookingDto.builder()
                .id(id)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(status)
                .itemId(1L)
                .booker(UserShortDto.builder().id(2L).build())
                .item(ItemShortDto.builder().id(1L).name("Drill").build())
                .build();
    }

    @Test
    void createBooking_shouldReturn200WithBody() throws Exception {
        BookingDto input = BookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        BookingDto response = makeBookingDto(1L, BookingStatus.WAITING);
        when(bookingService.createBooking(anyLong(), any())).thenReturn(response);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.booker.id").value(2L))
                .andExpect(jsonPath("$.item.name").value("Drill"));
    }

    @Test
    void createBooking_withUnknownItem_shouldReturn404() throws Exception {
        BookingDto input = BookingDto.builder()
                .itemId(999L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        when(bookingService.createBooking(anyLong(), any()))
                .thenThrow(new NotFoundException("Item not found"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Item not found"));
    }

    @Test
    void approveBooking_shouldReturn200WithApprovedStatus() throws Exception {
        BookingDto response = makeBookingDto(1L, BookingStatus.APPROVED);
        when(bookingService.approveBooking(anyLong(), anyLong(), anyBoolean())).thenReturn(response);

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void approveBooking_byWrongUser_shouldReturn403() throws Exception {
        when(bookingService.approveBooking(anyLong(), anyLong(), anyBoolean()))
                .thenThrow(new ForbiddenException("Only owner can approve booking"));

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 99L)
                        .param("approved", "true"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void getBookingById_shouldReturn200WithBooking() throws Exception {
        BookingDto response = makeBookingDto(1L, BookingStatus.WAITING);
        when(bookingService.getBookingById(anyLong(), anyLong())).thenReturn(response);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getBookingById_notFound_shouldReturn404() throws Exception {
        when(bookingService.getBookingById(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Booking not found"));

        mockMvc.perform(get("/bookings/999")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserBookings_shouldReturn200WithList() throws Exception {
        when(bookingService.getUserBookings(anyLong(), any()))
                .thenReturn(List.of(makeBookingDto(1L, BookingStatus.WAITING)));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getUserBookings_withInvalidState_shouldReturn400() throws Exception {
        when(bookingService.getUserBookings(anyLong(), any()))
                .thenThrow(new IllegalArgumentException("Unknown state: INVALID"));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .param("state", "INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Unknown state: INVALID"));
    }

    @Test
    void getOwnerBookings_shouldReturn200WithList() throws Exception {
        when(bookingService.getOwnerBookings(anyLong(), any()))
                .thenReturn(List.of(makeBookingDto(1L, BookingStatus.WAITING)));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}