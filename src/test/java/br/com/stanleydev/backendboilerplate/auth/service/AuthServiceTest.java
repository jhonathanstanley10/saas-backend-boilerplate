package br.com.stanleydev.backendboilerplate.auth.service;

import br.com.stanleydev.backendboilerplate.auth.dto.AuthResponse;
import br.com.stanleydev.backendboilerplate.auth.dto.LoginRequest;
import br.com.stanleydev.backendboilerplate.auth.dto.RefreshTokenRequest;
import br.com.stanleydev.backendboilerplate.auth.dto.RegisterRequest;
import br.com.stanleydev.backendboilerplate.exception.EmailAlreadyExistsException;
import br.com.stanleydev.backendboilerplate.exception.ResourceNotFoundException;
import br.com.stanleydev.backendboilerplate.organization.model.Membership;
import br.com.stanleydev.backendboilerplate.organization.model.Organization;
import br.com.stanleydev.backendboilerplate.organization.model.OrganizationRole;
import br.com.stanleydev.backendboilerplate.organization.repository.MembershipRepository;
import br.com.stanleydev.backendboilerplate.organization.repository.OrganizationRepository;
import br.com.stanleydev.backendboilerplate.security.JwtService;
import br.com.stanleydev.backendboilerplate.tenant.TenantContext; // Needed for refresh token test
import br.com.stanleydev.backendboilerplate.user.model.Role;
import br.com.stanleydev.backendboilerplate.user.model.User;
import br.com.stanleydev.backendboilerplate.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic; // Needed for TenantContext
import org.mockito.Mockito; // Needed for TenantContext
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    // --- Mocks for Dependencies ---
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private OrganizationRepository organizationRepository; // Added
    @Mock
    private MembershipRepository membershipRepository; // Added

    // Inject mocks into the service under test
    @InjectMocks
    private AuthService authService;

    // --- Test Data ---
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;
    private Organization testOrganization;
    private Membership testMembership;
    private RefreshTokenRequest refreshTokenRequest;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .password("password123")
                .build();

        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .passwordHash("hashed-password")
                .role(Role.ROLE_USER)
                .build();

        testOrganization = Organization.builder()
                .id(UUID.randomUUID())
                .tenantId(UUID.randomUUID().toString())
                .name("Test Workspace")
                .build();

        testMembership = Membership.builder()
                .id(UUID.randomUUID())
                .userId(testUser.getId())
                .organizationId(testOrganization.getId())
                .role(OrganizationRole.OWNER)
                .build();

        refreshTokenRequest = RefreshTokenRequest.builder()
                .refreshToken("valid-refresh-token")
                .build();

        // Clear TenantContext before each test (important for static mock)
        TenantContext.clear();
    }

    // Clear TenantContext after each test
    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }


    // --- Registration Tests ---

    @Test
    void register_shouldSucceed_whenEmailIsNotTaken() {
        // Arrange
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashed-password");

        // Mock saving User
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            userToSave.setId(UUID.randomUUID()); // Simulate ID generation
            return userToSave;
        });

        // Mock saving Organization
        ArgumentCaptor<Organization> orgCaptor = ArgumentCaptor.forClass(Organization.class);
        when(organizationRepository.save(orgCaptor.capture())).thenAnswer(invocation -> {
            Organization orgToSave = invocation.getArgument(0);
            orgToSave.setId(UUID.randomUUID()); // Simulate ID generation
            return orgToSave;
        });

        // Mock saving Membership
        ArgumentCaptor<Membership> membershipCaptor = ArgumentCaptor.forClass(Membership.class);
        when(membershipRepository.save(membershipCaptor.capture())).thenAnswer(invocation -> {
            Membership membershipToSave = invocation.getArgument(0);
            membershipToSave.setId(UUID.randomUUID());
            return membershipToSave;
        });


        // Mock token generation (now requires tenantId)
        when(jwtService.generateAccessToken(any(User.class), anyString())).thenReturn("mock-access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("mock-refresh-token");
        when(jwtService.getRefreshTokenExpiry()).thenReturn(Instant.now().plus(7, ChronoUnit.DAYS));


        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mock-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("mock-refresh-token");

        // Verify saves
        verify(userRepository, times(2)).save(any(User.class)); // Saved twice: once initially, once with refresh token
        verify(organizationRepository, times(1)).save(any(Organization.class));
        verify(membershipRepository, times(1)).save(any(Membership.class));

        // Assert captured entities
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getEmail()).isEqualTo(registerRequest.getEmail());
        assertThat(capturedUser.getFirstName()).isEqualTo(registerRequest.getFirstName());
        assertThat(capturedUser.getPasswordHash()).isEqualTo("hashed-password");
        assertThat(capturedUser.getRefreshToken()).isEqualTo("mock-refresh-token");

        Organization capturedOrg = orgCaptor.getValue();
        assertThat(capturedOrg.getName()).isEqualTo("Test's Workspace");
        assertThat(capturedOrg.getTenantId()).isNotNull();

        Membership capturedMembership = membershipCaptor.getValue();
        assertThat(capturedMembership.getUserId()).isEqualTo(capturedUser.getId());
        assertThat(capturedMembership.getOrganizationId()).isEqualTo(capturedOrg.getId());
        assertThat(capturedMembership.getRole()).isEqualTo(OrganizationRole.OWNER);

        // Verify token generation was called with captured tenantId
        verify(jwtService).generateAccessToken(any(User.class), eq(capturedOrg.getTenantId()));
    }

    @Test
    void register_shouldFail_whenEmailIsTaken() {
        // Arrange
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(new User()));

        // Act & Assert
        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(registerRequest));

        // Verify no saves occurred
        verify(userRepository, never()).save(any(User.class));
        verify(organizationRepository, never()).save(any(Organization.class));
        verify(membershipRepository, never()).save(any(Membership.class));
    }

    // --- Login Tests ---

    @Test
    void login_shouldSucceed_withValidCredentialsAndMembership() {
        // Arrange
        // 1. Mock AuthenticationManager to succeed
        doNothing().when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        // 2. Mock finding the user
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));

        // 3. Mock finding the membership
        when(membershipRepository.findByUserId(testUser.getId())).thenReturn(List.of(testMembership));

        // 4. Mock finding the organization
        when(organizationRepository.findById(testMembership.getOrganizationId())).thenReturn(Optional.of(testOrganization));

        // 5. Mock token generation
        when(jwtService.generateAccessToken(testUser, testOrganization.getTenantId())).thenReturn("mock-access-token");
        when(jwtService.generateRefreshToken(testUser)).thenReturn("mock-refresh-token");
        when(jwtService.getRefreshTokenExpiry()).thenReturn(Instant.now().plus(7, ChronoUnit.DAYS));

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mock-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("mock-refresh-token");

        // Verify user was saved (to update refresh token)
        verify(userRepository, times(1)).save(testUser);
        assertThat(testUser.getRefreshToken()).isEqualTo("mock-refresh-token");
    }

    @Test
    void login_shouldFail_whenUserHasNoMembership() {
        // Arrange
        doNothing().when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(membershipRepository.findByUserId(testUser.getId())).thenReturn(Collections.emptyList()); // No memberships

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> authService.login(loginRequest));

        // Verify user was NOT saved
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_shouldFail_withInvalidCredentials() {
        // Arrange
        // Mock AuthenticationManager to throw BadCredentialsException
        doThrow(new BadCredentialsException("Bad credentials")).when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));

        // Verify nothing else was called
        verify(userRepository, never()).findByEmail(anyString());
        verify(membershipRepository, never()).findByUserId(any(UUID.class));
        verify(organizationRepository, never()).findById(any(UUID.class));
        verify(userRepository, never()).save(any(User.class));
    }


    // --- Refresh Token Tests ---

    @Test
    void refreshToken_shouldSucceed_withValidTokenAndTenantContext() {
        // Arrange
        testUser.setRefreshToken("valid-refresh-token");
        testUser.setRefreshTokenExpiry(Instant.now().plus(1, ChronoUnit.DAYS));
        String existingTenantId = UUID.randomUUID().toString();

        when(userRepository.findByRefreshToken("valid-refresh-token")).thenReturn(Optional.of(testUser));
        when(jwtService.generateAccessToken(testUser, existingTenantId)).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(testUser)).thenReturn("new-refresh-token");
        when(jwtService.getRefreshTokenExpiry()).thenReturn(Instant.now().plus(7, ChronoUnit.DAYS));

        // --- Mock TenantContext ---
        // Since TenantContext uses static methods, we need Mockito's static mocking
        // This requires the mockito-inline dependency (usually included with spring-boot-starter-test)
        try (MockedStatic<TenantContext> mockedTenantContext = Mockito.mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenant).thenReturn(existingTenantId);

            // Act
            AuthResponse response = authService.refreshToken(refreshTokenRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo("new-access-token");
            assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");

            // Verify user was saved with new tokens
            verify(userRepository, times(1)).save(testUser);
            assertThat(testUser.getRefreshToken()).isEqualTo("new-refresh-token");
        } // Static mock is automatically closed here
    }

    @Test
    void refreshToken_shouldFail_whenTenantContextIsNull() {
        // Arrange
        testUser.setRefreshToken("valid-refresh-token");
        testUser.setRefreshTokenExpiry(Instant.now().plus(1, ChronoUnit.DAYS));
        when(userRepository.findByRefreshToken("valid-refresh-token")).thenReturn(Optional.of(testUser));

        // Mock TenantContext to return null
        try (MockedStatic<TenantContext> mockedTenantContext = Mockito.mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenant).thenReturn(null); // No tenant in context

            // Act & Assert
            assertThrows(BadCredentialsException.class, () -> authService.refreshToken(refreshTokenRequest));
        }

        // Verify user was NOT saved
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void refreshToken_shouldFail_whenTokenIsExpired() {
        // Arrange
        testUser.setRefreshToken("expired-refresh-token");
        testUser.setRefreshTokenExpiry(Instant.now().minus(1, ChronoUnit.DAYS)); // Expired yesterday
        refreshTokenRequest.setRefreshToken("expired-refresh-token");

        when(userRepository.findByRefreshToken("expired-refresh-token")).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> authService.refreshToken(refreshTokenRequest));

        // Verify user was saved (to clear the expired token)
        verify(userRepository, times(1)).save(testUser);
        assertThat(testUser.getRefreshToken()).isNull();
        assertThat(testUser.getRefreshTokenExpiry()).isNull();
    }

    @Test
    void refreshToken_shouldFail_whenTokenIsInvalid() {
        // Arrange
        refreshTokenRequest.setRefreshToken("invalid-token");
        when(userRepository.findByRefreshToken("invalid-token")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> authService.refreshToken(refreshTokenRequest));

        // Verify user was NOT saved
        verify(userRepository, never()).save(any(User.class));
    }

}