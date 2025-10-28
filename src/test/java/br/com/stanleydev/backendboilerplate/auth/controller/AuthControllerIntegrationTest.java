package br.com.stanleydev.backendboilerplate.auth.controller;

import br.com.stanleydev.backendboilerplate.auth.dto.RegisterRequest;
import br.com.stanleydev.backendboilerplate.organization.repository.MembershipRepository; // Added
import br.com.stanleydev.backendboilerplate.organization.repository.OrganizationRepository; // Added
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
import org.springframework.transaction.annotation.Transactional; // Added for tearDown

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
    @Autowired
    private OrganizationRepository organizationRepository; // Added
    @Autowired
    private MembershipRepository membershipRepository; // Added

    // Clean up the database after each test
    // Added @Transactional to ensure deletion happens within a transaction
    @AfterEach
    @Transactional
    void tearDown() {
        // --- IMPORTANT: Delete in the correct order to avoid constraint violations ---
        membershipRepository.deleteAll();
        organizationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void register_shouldSucceed_withValidData() throws Exception {
        // --- ARRANGE ---
        RegisterRequest registerRequest = RegisterRequest.builder()
                .firstName("New") // Added
                .lastName("User") // Added
                .email("new-user@example.com")
                .password("ValidPassword123")
                .build();

        String requestJson = objectMapper.writeValueAsString(registerRequest);

        // --- ACT & ASSERT ---
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated()) // Expect 201 Created now
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());

        // Verify in the database
        User savedUser = userRepository.findByEmail("new-user@example.com").orElseThrow();
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("new-user@example.com");
        assertThat(savedUser.getFirstName()).isEqualTo("New"); // Verify name

        // Verify organization and membership were created
        assertThat(organizationRepository.findAll()).hasSize(1);
        assertThat(membershipRepository.findByUserId(savedUser.getId())).hasSize(1);
    }

    @Test
    void register_shouldFail_withShortPassword() throws Exception {
        // --- ARRANGE ---
        // Provide valid names so ONLY password fails
        RegisterRequest registerRequest = RegisterRequest.builder()
                .firstName("Fail") // Added valid name
                .lastName("User") // Added valid name
                .email("fail-user@example.com")
                .password("short") // Invalid password
                .build();

        String requestJson = objectMapper.writeValueAsString(registerRequest);

        // --- ACT & ASSERT ---
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest()) // Check for 400 Bad Request
                .andExpect(jsonPath("$.message").value("Validation failed"))
                // Check specifically for the password error message within the errors array
                .andExpect(jsonPath("$.errors[?(@.field == 'password')].message")
                        .value("Password must be at least 8 characters long"));

        // Verify user was NOT created in the database
        assertThat(userRepository.findByEmail("fail-user@example.com")).isEmpty();
    }

    @Test
    void register_shouldFail_withMissingName() throws Exception {
        // --- ARRANGE ---
        // Missing firstName and lastName, which are now required
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("fail-user@example.com")
                .password("ValidPassword123")
                .build();

        String requestJson = objectMapper.writeValueAsString(registerRequest);

        // --- ACT & ASSERT ---
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest()) // Check for 400 Bad Request
                .andExpect(jsonPath("$.message").value("Validation failed"))
                // Check specifically that firstName error exists
                .andExpect(jsonPath("$.errors[?(@.field == 'firstName')]").exists())
                // Check specifically that lastName error exists
                .andExpect(jsonPath("$.errors[?(@.field == 'lastName')]").exists());

        // Verify user was NOT created in the database
        assertThat(userRepository.findByEmail("fail-user@example.com")).isEmpty();
    }

    // TODO: Add integration tests for login, refreshToken, forgotPassword, resetPassword
}
