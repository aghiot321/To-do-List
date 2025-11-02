package br.com.aghiot.todolist.user;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class UserPersistenceTests {

    @Autowired
    private IUserRepository userRepository;

    private UserModel testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserModel();
        testUser.setUsername("testuser");
        testUser.setName("Test User");
        testUser.setPassword("hashedpassword123");
    }

    @Test
    void testCreateUser() {
        // Arrange & Act
        UserModel savedUser = userRepository.save(testUser);

        // Assert
        assertNotNull(savedUser);
        assertNotNull(savedUser.getId());
        assertEquals("testuser", savedUser.getUsername());
        assertEquals("Test User", savedUser.getName());
        assertEquals("hashedpassword123", savedUser.getPassword());
    }

    @Test
    void testFindUserById() {
        // Arrange
        UserModel savedUser = userRepository.save(testUser);
        UUID userId = savedUser.getId();

        // Act
        Optional<UserModel> foundUser = userRepository.findById(userId);

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals(userId, foundUser.get().getId());
        assertEquals("testuser", foundUser.get().getUsername());
    }

    @Test
    void testFindUserByUsername() {
        // Arrange
        userRepository.save(testUser);

        // Act
        UserModel foundUser = userRepository.findByUsername("testuser");

        // Assert
        assertNotNull(foundUser);
        assertEquals("testuser", foundUser.getUsername());
        assertEquals("Test User", foundUser.getName());
    }

    @Test
    void testUpdateUser() {
        // Arrange
        UserModel savedUser = userRepository.save(testUser);
        UUID userId = savedUser.getId();

        // Act
        savedUser.setName("Updated Name");
        savedUser.setPassword("newhash123");
        UserModel updatedUser = userRepository.save(savedUser);

        // Assert
        assertEquals(userId, updatedUser.getId());
        assertEquals("Updated Name", updatedUser.getName());
        assertEquals("newhash123", updatedUser.getPassword());
    }

    @Test
    void testDeleteUser() {
        // Arrange
        UserModel savedUser = userRepository.save(testUser);
        UUID userId = savedUser.getId();

        // Act
        userRepository.deleteById(userId);

        // Assert
        Optional<UserModel> deletedUser = userRepository.findById(userId);
        assertFalse(deletedUser.isPresent());
    }

    @Test
    void testFindAllUsers() {
        // Arrange
        UserModel user1 = new UserModel();
        user1.setUsername("user1");
        user1.setName("User One");
        user1.setPassword("hash1");

        UserModel user2 = new UserModel();
        user2.setUsername("user2");
        user2.setName("User Two");
        user2.setPassword("hash2");

        userRepository.save(user1);
        userRepository.save(user2);

        // Act
        var allUsers = userRepository.findAll();

        // Assert
        assertNotNull(allUsers);
        assertTrue(allUsers.size() >= 2);
    }

    @Test
    void testUserNotFoundByUsername() {
        // Act
        UserModel foundUser = userRepository.findByUsername("nonexistent");

        // Assert
        assertNull(foundUser);
    }

    @Test
    void testDuplicateUsernameConstraint() {
        // Arrange
        userRepository.save(testUser);

        UserModel duplicateUser = new UserModel();
        duplicateUser.setUsername("testuser");
        duplicateUser.setName("Another User");
        duplicateUser.setPassword("anotherhash");

        // Act & Assert
        assertThrows(Exception.class, () -> {
            userRepository.save(duplicateUser);
            userRepository.flush();
        });
    }
}
