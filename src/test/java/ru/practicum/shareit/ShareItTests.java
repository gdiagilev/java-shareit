package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.ItemRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ShareItTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User user;
    private Item item;

    @BeforeEach
    void setup() {
        user = User.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .build();
        user = userRepository.save(user);

        item = Item.builder()
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .owner(user)
                .build();
        item = itemRepository.save(item);
    }

    @Test
    void testUserAndItemCreated() {
        assertEquals(1, userRepository.count());
        assertEquals(1, itemRepository.count());
        assertEquals(user.getId(), item.getOwner().getId());
    }
}