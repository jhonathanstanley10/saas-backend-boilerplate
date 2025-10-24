package br.com.stanleydev.backendboilerplate.auth.controller;

import br.com.stanleydev.backendboilerplate.auth.dto.RegisterRequest;
import br.com.stanleydev.backendboilerplate.user.model.User;
import br.com.stanleydev.backendboilerplate.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest // Loads the entire application context
@AutoConfigureMockMvc // Gives us a MockMvc instance to make fake HTTP requests
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc; // For making HTTP requests

    @Autowired
    private ObjectMapper objectMapper; // For converting objects to JSON

    @Autowired
    private UserRepository userRepository; // To check the database directly

    // Clean up the database after each test
    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void register_shouldSucceed_withValidData() throws Exception {
        // --- ARRANGE ---
        RegisterRequest registerRequest = RegisterRequest.builder()
                .firstName("New")
                .lastName("User")
                .email("new-user@example.com")
                .password("ValidPassword123")
                .build();

        String requestJson = objectMapper.writeValueAsString(registerRequest);

        // --- ACT & ASSERT ---
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk()) // Check for 200 OK
                .andExpect(jsonPath("$.token").isNotEmpty()) // Check if 'token' exists
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());

        // Verify in the database
        User savedUser = userRepository.findByEmail("new-user@example.com").orElseThrow();
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("new-user@example.com");
    }

    @Test
    void register_shouldFail_withShortPassword() throws Exception {
        // --- ARRANGE ---
        // This request has a password that violates the @Size(min=8) validation
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("fail-user@example.com")
                .password("short")
                .build();

        String requestJson = objectMapper.writeValueAsString(registerRequest);

        // --- ACT & ASSERT ---
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest()) // Check for 400 Bad Request
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors[0].field").value("password"));

        // Verify user was NOT created in the database
        assertThat(userRepository.findByEmail("fail-user@example.com")).isEmpty();
    }
}