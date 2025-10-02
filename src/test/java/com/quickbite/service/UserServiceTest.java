package com.quickbite.service;

import com.quickbite.dto.UserDTO;
import com.quickbite.exception.InvalidCredentialsException;
import com.quickbite.exception.UserAlreadyExistsException;
import com.quickbite.exception.UserNotFoundException;
import com.quickbite.model.User;
import com.quickbite.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("1");
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setRole("STUDENT");
    }

    @Test
    void register_ShouldRegisterNewUser_WhenUserDoesNotExist() {
        // Given
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("password123");

        when(userRepository.findByUsername("newuser")).thenReturn(null);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserDTO result = userService.register(newUser);

        // Then
        assertNotNull(result);
        assertEquals("STUDENT", newUser.getRole());
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(newUser);
    }

    @Test
    void register_ShouldThrowException_WhenUserAlreadyExists() {
        // Given
        User existingUser = new User();
        existingUser.setUsername("existinguser");

        when(userRepository.findByUsername("existinguser")).thenReturn(testUser);

        // When & Then
        assertThrows(UserAlreadyExistsException.class, () -> userService.register(existingUser));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_ShouldReturnUser_WhenCredentialsAreValid() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(testUser);
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);

        // When
        User result = userService.login("testuser", "password123");

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void login_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(null);

        // When & Then
        assertThrows(InvalidCredentialsException.class,
                () -> userService.login("nonexistent", "password"));
    }

    @Test
    void login_ShouldThrowException_WhenPasswordIsIncorrect() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(testUser);
        when(passwordEncoder.matches("wrongpassword", testUser.getPassword())).thenReturn(false);

        // When & Then
        assertThrows(InvalidCredentialsException.class,
                () -> userService.login("testuser", "wrongpassword"));
    }

    @Test
    void changeUserRole_ShouldUpdateRole_WhenUserExists() {
        // Given
        when(userRepository.findById("1")).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);

        // When
        User result = userService.changeUserRole("1", "ADMIN");

        // Then
        assertEquals("ADMIN", result.getRole());
        verify(userRepository).save(testUser);
    }

    @Test
    void changeUserRole_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.findById("999")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class,
                () -> userService.changeUserRole("999", "ADMIN"));
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Given
        List<User> users = List.of(testUser);
        when(userRepository.findAll()).thenReturn(users);

        // When
        List<UserDTO> result = userService.getAllUsers();

        // Then
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
    }

    @Test
    void findById_ShouldReturnUser_WhenUserExists() {
        // Given
        when(userRepository.findById("1")).thenReturn(Optional.of(testUser));

        // When
        User result = userService.findById("1");

        // Then
        assertEquals(testUser, result);
    }

    @Test
    void findById_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.findById("999")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> userService.findById("999"));
    }
}