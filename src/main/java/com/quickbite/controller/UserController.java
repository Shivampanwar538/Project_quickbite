package com.quickbite.controller;

import com.quickbite.model.User;
import com.quickbite.repository.UserRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.quickbite.dto.UserDTO;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class UserController {
    private final UserRepository userRepo;

    public UserController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @PostMapping("/register")
    public UserDTO register(@RequestBody User user) {
        user.setRole("STUDENT");
        return new UserDTO(userRepo.save(user));
    }

    @PostMapping("/login")
    public User login(@RequestBody User u) {
        User dbUser = userRepo.findByUsername(u.getUsername());
        if (dbUser != null && dbUser.getPassword().equals(u.getPassword())) {
            return dbUser;
        }
        throw new RuntimeException("Invalid credentials");
    }

    @PutMapping("/changeRole/{id}")
    public User changeRole(@PathVariable String id, @RequestParam String role) {
        User u = userRepo.findById(id).orElseThrow();
        u.setRole(role);
        return userRepo.save(u);
    }

    @GetMapping
    public List<UserDTO> getUsers() {
        return userRepo.findAll().stream()
                .map(u -> new UserDTO(u.getId(), u.getUsername(), u.getRole()))
                .toList();
    }
}