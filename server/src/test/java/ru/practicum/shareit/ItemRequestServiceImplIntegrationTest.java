package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = ru.practicum.shareit.ShareItApp.class)
@ActiveProfiles("test")
@Transactional
class ItemRequestServiceImplIntegrationTest {

    @Autowired
    private ItemRequestService requestService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemRequestRepository requestRepository;

    private User requester;
    private User otherUser;

    @BeforeEach
    void setUp() {
        requester = userRepository.save(User.builder()
                .name("Requester")
                .email("requester@test.com")
                .build());

        otherUser = userRepository.save(User.builder()
                .name("Other")
                .email("other@test.com")
                .build());
    }

    @Test
    void createRequest_shouldSaveAndReturnDto() {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Need a drill");

        ItemRequestDto result = requestService.createRequest(requester.getId(), dto);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getDescription()).isEqualTo("Need a drill");
        assertThat(result.getCreated()).isNotNull();
        assertThat(result.getItems()).isEmpty();
    }

    @Test
    void createRequest_withUnknownUser_shouldThrowNotFoundException() {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Need a drill");

        assertThatThrownBy(() -> requestService.createRequest(999L, dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getUserRequests_shouldReturnOwnRequests() {
        ItemRequestDto dto1 = new ItemRequestDto();
        dto1.setDescription("Need a drill");
        ItemRequestDto dto2 = new ItemRequestDto();
        dto2.setDescription("Need a saw");

        requestService.createRequest(requester.getId(), dto1);
        requestService.createRequest(requester.getId(), dto2);

        List<ItemRequestDto> results = requestService.getUserRequests(requester.getId());

        assertThat(results).hasSize(2);
    }

    @Test
    void getUserRequests_shouldNotReturnOtherUsersRequests() {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Need a drill");
        requestService.createRequest(otherUser.getId(), dto);

        List<ItemRequestDto> results = requestService.getUserRequests(requester.getId());

        assertThat(results).isEmpty();
    }

    @Test
    void getUserRequests_shouldIncludeItemResponses() {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Need a drill");
        ItemRequestDto savedRequest = requestService.createRequest(requester.getId(), dto);

        ItemRequest request = requestRepository.findById(savedRequest.getId()).orElseThrow();
        itemRepository.save(Item.builder()
                .name("Drill")
                .description("A drill")
                .available(true)
                .owner(otherUser)
                .request(request)
                .build());

        List<ItemRequestDto> results = requestService.getUserRequests(requester.getId());

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getItems()).hasSize(1);
        assertThat(results.get(0).getItems().get(0).getName()).isEqualTo("Drill");
    }

    @Test
    void getOtherUsersRequests_shouldNotReturnOwnRequests() {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Own request");
        requestService.createRequest(requester.getId(), dto);

        List<ItemRequestDto> results = requestService.getOtherUsersRequests(requester.getId());

        assertThat(results).isEmpty();
    }

    @Test
    void getOtherUsersRequests_shouldReturnOtherUsersRequests() {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Other user request");
        requestService.createRequest(otherUser.getId(), dto);

        List<ItemRequestDto> results = requestService.getOtherUsersRequests(requester.getId());

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getDescription()).isEqualTo("Other user request");
    }

    @Test
    void getRequestById_shouldReturnRequestWithItems() {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Need a drill");
        ItemRequestDto savedRequest = requestService.createRequest(requester.getId(), dto);

        ItemRequest request = requestRepository.findById(savedRequest.getId()).orElseThrow();
        itemRepository.save(Item.builder()
                .name("Drill")
                .description("A drill")
                .available(true)
                .owner(otherUser)
                .request(request)
                .build());

        ItemRequestDto result = requestService.getRequestById(otherUser.getId(), savedRequest.getId());

        assertThat(result.getId()).isEqualTo(savedRequest.getId());
        assertThat(result.getItems()).hasSize(1);
    }

    @Test
    void getRequestById_withUnknownId_shouldThrowNotFoundException() {
        assertThatThrownBy(() -> requestService.getRequestById(requester.getId(), 999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getUserRequests_shouldBeSortedNewestFirst() throws InterruptedException {
        ItemRequestDto dto1 = new ItemRequestDto();
        dto1.setDescription("First request");
        requestService.createRequest(requester.getId(), dto1);

        Thread.sleep(10);

        ItemRequestDto dto2 = new ItemRequestDto();
        dto2.setDescription("Second request");
        requestService.createRequest(requester.getId(), dto2);

        List<ItemRequestDto> results = requestService.getUserRequests(requester.getId());

        assertThat(results.get(0).getDescription()).isEqualTo("Second request");
        assertThat(results.get(1).getDescription()).isEqualTo("First request");
    }
}
