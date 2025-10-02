package com.quickbite.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickbite.config.SecurityConfig;
import com.quickbite.dto.UserDTO;
import com.quickbite.model.User;
import com.quickbite.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf; // <-- ADD THIS IMPORT
import org.springframework.context.annotation.Import;
@WebMvcTest(UserController.class)
@Import(SecurityConfig.class) // <--- ADD THIS LINE
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("1");
        testUser.setUsername("testuser");
        testUser.setPassword("password@123");
        testUser.setRole("STUDENT");

        testUserDTO = new UserDTO("1", "testuser", "STUDENT");
    }

    @Test
    void register_ShouldReturnCreatedUser() throws Exception {
        when(userService.register(any(User.class))).thenReturn(testUserDTO);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    void login_ShouldReturnUser_WhenCredentialsValid() throws Exception {
        when(userService.login("testuser", "password@123")).thenReturn(testUser);

        String loginPayload = "{\"username\":\"testuser\",\"password\":\"password@123\"}";

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void changeRole_ShouldReturnUpdatedUser() throws Exception {
        testUser.setRole("ADMIN");
        when(userService.changeUserRole("1", "ADMIN")).thenReturn(testUser);

        mockMvc.perform(put("/auth/changeRole/1")
                        .param("role", "ADMIN")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUsers_ShouldReturnListOfUsers() throws Exception {
        List<UserDTO> users = List.of(testUserDTO);
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/auth"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("testuser"));
    }
}