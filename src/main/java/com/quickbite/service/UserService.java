package com.quickbite.service;

import com.quickbite.dto.UserDTO;
import com.quickbite.exception.InvalidCredentialsException;
import com.quickbite.exception.UserAlreadyExistsException;
import com.quickbite.exception.UserNotFoundException;
import com.quickbite.model.User;
import com.quickbite.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDTO register(User user) {
        log.info("Attempting to register user: {}", user.getUsername());

        if (userRepository.findByUsername(user.getUsername()) != null) {
            throw new UserAlreadyExistsException(user.getUsername());
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("STUDENT");

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getUsername());

        return new UserDTO(savedUser);
    }

    public User login(String username, String password) {
        log.info("Attempting login for user: {}", username);

        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new InvalidCredentialsException();
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        log.info("User logged in successfully: {}", username);
        return user;
    }

    public User changeUserRole(String userId, String role) {
        log.info("Changing role for user {} to {}", userId, role);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId, "id"));

        user.setRole(role);
        User savedUser = userRepository.save(user);

        log.info("Role changed successfully for user: {}", savedUser.getUsername());
        return savedUser;
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserDTO(user.getId(), user.getUsername(), user.getRole()))
                .toList();
    }

    public User findById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id, "id"));
    }

    public User findByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UserNotFoundException(username, "username");
        }
        return user;
    }
}