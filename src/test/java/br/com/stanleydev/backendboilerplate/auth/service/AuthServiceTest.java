package br.com.stanleydev.backendboilerplate.auth.service;

import br.com.stanleydev.backendboilerplate.auth.dto.AuthResponse;
import br.com.stanleydev.backendboilerplate.auth.dto.RegisterRequest;
import br.com.stanleydev.backendboilerplate.exception.EmailAlreadyExistsException;
import br.com.stanleydev.backendboilerplate.security.JwtService;
import br.com.stanleydev.backendboilerplate.user.model.Role;
import br.com.stanleydev.backendboilerplate.user.model.User;
import br.com.stanleydev.backendboilerplate.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Initializes mocks
class AuthServiceTest {

    // These are the dependencies of AuthService, so we mock them
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    // This tells Mockito to inject the mocks above into our AuthService instance
    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        // Create a re-usable request object for our tests
        registerRequest = RegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();
    }

    @Test
    void register_shouldSucceed_whenEmailIsNotTaken() {
        // --- ARRANGE ---
        // 1. Mock the "email not found" check
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // 2. Mock the password encoding
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");

        // 3. Mock the token generation
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("mock-access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("mock-refresh-token");
        when(jwtService.getRefreshTokenExpiry()).thenReturn(Instant.now());

        // 4. Create an ArgumentCaptor to capture the User object sent to save()
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        // --- ACT ---
        AuthResponse response = authService.register(registerRequest);

        // --- ASSERT ---
        // 1. Check the response is correct
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mock-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("mock-refresh-token");

        // 2. Verify that userRepository.save() was called exactly once
        verify(userRepository, times(1)).save(userCaptor.capture());

        // 3. Check that the User object we "captured" has the correct details
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getPasswordHash()).isEqualTo("hashed-password");
        assertThat(savedUser.getRole()).isEqualTo(Role.ROLE_USER);
        assertThat(savedUser.getTenantId()).isNotNull();
    }

    @Test
    void register_shouldFail_whenEmailIsTaken() {
        // --- ARRANGE ---
        // 1. Mock the "email found" check
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(new User()));

        // --- ACT & ASSERT ---
        // 1. Assert that the specific exception is thrown
        assertThrows(EmailAlreadyExistsException.class, () -> {
            authService.register(registerRequest);
        });

        // 2. Verify that save() was NEVER called
        verify(userRepository, never()).save(any(User.class));
    }

    // You would add more tests here for login(), refreshToken(), etc.
}