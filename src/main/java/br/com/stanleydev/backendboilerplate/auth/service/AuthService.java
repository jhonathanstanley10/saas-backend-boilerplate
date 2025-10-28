package br.com.stanleydev.backendboilerplate.auth.service;

import br.com.stanleydev.backendboilerplate.auth.dto.*;
import br.com.stanleydev.backendboilerplate.exception.EmailAlreadyExistsException;
import br.com.stanleydev.backendboilerplate.exception.ResourceNotFoundException;
import br.com.stanleydev.backendboilerplate.organization.model.Membership;
import br.com.stanleydev.backendboilerplate.organization.model.Organization;
import br.com.stanleydev.backendboilerplate.organization.model.OrganizationRole;
import br.com.stanleydev.backendboilerplate.organization.repository.MembershipRepository;
import br.com.stanleydev.backendboilerplate.organization.repository.OrganizationRepository;
import br.com.stanleydev.backendboilerplate.security.JwtService;
import br.com.stanleydev.backendboilerplate.tenant.TenantContext;
import br.com.stanleydev.backendboilerplate.user.model.Role;
import br.com.stanleydev.backendboilerplate.user.model.SubscriptionStatus;
import br.com.stanleydev.backendboilerplate.user.model.User;
import br.com.stanleydev.backendboilerplate.user.repository.UserRepository;
import br.com.stanleydev.backendboilerplate.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final OrganizationRepository organizationRepository;
    private final MembershipRepository membershipRepository;

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already taken");
        }



        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER) // Default role
                .build();
        User savedUser = userRepository.save(user);

        String newTenantId = UUID.randomUUID().toString();

        Organization organization = Organization.builder()
                .tenantId(newTenantId)
                .name( (request.getFirstName() != null ? request.getFirstName() + "'s" : "My") + " Workspace") // Default name
                .ownerUserId(savedUser.getId())
                .subscriptionStatus(SubscriptionStatus.FREE)
                .build();
        Organization savedOrganization = organizationRepository.save(organization);

        Membership membership = Membership.builder()
                .userId(savedUser.getId())
                .organizationId(savedOrganization.getId())
                .role(OrganizationRole.OWNER) // First user is the owner
                .build();
        membershipRepository.save(membership);

        String accessToken = jwtService.generateAccessToken(savedUser, newTenantId);
        String refreshToken = jwtService.generateRefreshToken(savedUser);

        savedUser.setRefreshToken(refreshToken);
        savedUser.setRefreshTokenExpiry(jwtService.getRefreshTokenExpiry());
        userRepository.save(savedUser);


        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found after authentication"));

        // CRITICAL CHANGE: Find the Organization(s) the user belongs to
        List<Membership> memberships = membershipRepository.findByUserId(user.getId());
        if (memberships.isEmpty()) {
            // Should not happen if registration is correct, but handle it
            log.error("User {} authenticated but has no organization memberships.", user.getEmail());
            throw new IllegalStateException("User has no organization membership.");
        }

        // *** Simplification for now: Use the *first* organization found ***
        // In a real multi-org app, you'd need a way for the user to select which org to log into.
        Membership firstMembership = memberships.get(0);
        Organization organization = organizationRepository.findById(firstMembership.getOrganizationId())
                .orElseThrow(() -> new IllegalStateException("Organization not found for membership."));

        // Generate Tokens using the Organization's Tenant ID
        String accessToken = jwtService.generateAccessToken(user, organization.getTenantId()); // Use Org's tenantId
        String refreshToken = jwtService.generateRefreshToken(user);

        // Save refresh token to user (as before)
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(jwtService.getRefreshTokenExpiry());
        userRepository.save(user);

        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // --- Refresh Token Logic ---
    // Needs modification if user can belong to multiple orgs.
    // For now, it assumes the *new* access token should be for the *same* tenantId
    // as the *old* one (which is implicitly stored in TenantContext if the old token was valid).
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        User user = userRepository.findByRefreshToken(requestRefreshToken)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if (user.getRefreshTokenExpiry().isBefore(Instant.now())) {
            user.setRefreshToken(null); // Clear expired token
            user.setRefreshTokenExpiry(null);
            userRepository.save(user);
            throw new BadCredentialsException("Refresh token has expired");
        }

        // *** Need the tenantId to generate the new access token ***
        // Get it from the current context (set by the *old* access token's filter run)
        String currentTenantId = TenantContext.getCurrentTenant();
        if (currentTenantId == null) {
            // Fallback: If context is missing, maybe fetch the user's first org again? Risky.
            // Better to enforce that refresh must happen while an old token is still somewhat valid.
            log.warn("Cannot refresh token for user {} without tenantId in context.", user.getEmail());
            throw new BadCredentialsException("Cannot determine tenant context for refresh.");
        }

        // Generate new access token for the SAME tenant
        String newAccessToken = jwtService.generateAccessToken(user, currentTenantId);

        // Rotate the refresh token
        String newRefreshToken = jwtService.generateRefreshToken(user);
        user.setRefreshToken(newRefreshToken);
        user.setRefreshTokenExpiry(jwtService.getRefreshTokenExpiry());
        userRepository.save(user);

        return AuthResponse.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    @Transactional
    public void handleForgotPassword(ForgotPasswordRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String token = UUID.randomUUID().toString();

            user.setPasswordResetToken(token);
            user.setPasswordResetTokenExpiry(Instant.now().plus(1, ChronoUnit.HOURS));
            userRepository.save(user);

            log.info("Password reset token for {}: {}", user.getEmail(), token);
            emailService.sendPasswordResetEmail(user.getEmail(), token);
        }
    }

    @Transactional
    public void handleResetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByPasswordResetToken(request.getToken())
                .orElseThrow(() -> new BadCredentialsException("Invalid password reset token"));

        if (user.getPasswordResetTokenExpiry().isBefore(Instant.now())) {
            throw new BadCredentialsException("Password reset token has expired");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);

        user.setRefreshToken(null);
        user.setRefreshTokenExpiry(null);

        userRepository.save(user);
    }

}