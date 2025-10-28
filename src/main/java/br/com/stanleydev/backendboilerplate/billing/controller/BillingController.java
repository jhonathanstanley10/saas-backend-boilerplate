package br.com.stanleydev.backendboilerplate.billing.controller;

import br.com.stanleydev.backendboilerplate.billing.dto.CreatePortalSessionRequest;
import br.com.stanleydev.backendboilerplate.billing.service.StripeService;
import br.com.stanleydev.backendboilerplate.organization.model.Membership;
import br.com.stanleydev.backendboilerplate.organization.model.Organization;
import br.com.stanleydev.backendboilerplate.organization.repository.MembershipRepository;
import br.com.stanleydev.backendboilerplate.organization.repository.OrganizationRepository;
import br.com.stanleydev.backendboilerplate.user.model.User;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private static final Logger log = LoggerFactory.getLogger(BillingController.class);
    private final StripeService stripeService;
    private final MembershipRepository membershipRepository;
    private final OrganizationRepository organizationRepository;


    public record CreateCheckoutSessionRequest(String successUrl, String cancelUrl) {}


    @PostMapping("/create-checkout-session")
    public ResponseEntity<?> createCheckoutSession(
            @AuthenticationPrincipal User user,
            @RequestBody CreateCheckoutSessionRequest request
    ) {
        try {
            Organization organization = findUserOrganization(user);
            Session session = stripeService.createCheckoutSession(
                    organization,
                    request.successUrl(),
                    request.cancelUrl()
            );

            return ResponseEntity.ok(Map.of("url", session.getUrl()));

        } catch (StripeException e) {
            log.error("Stripe error creating checkout session for user {}: {}", user.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Error creating payment session."));
        } catch (IllegalStateException e) {
            log.error("State error creating checkout session for user {}: {}", user.getEmail(), e.getMessage());
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/create-portal-session")
    public ResponseEntity<?> createPortalSession(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreatePortalSessionRequest request
    ) {
        try {
            Organization organization = findUserOrganization(user);

            com.stripe.model.billingportal.Session portalSession = stripeService.createPortalSession(
                    organization,
                    request.returnUrl()
            );

            return ResponseEntity.ok(Map.of("url", portalSession.getUrl()));

        } catch (StripeException e) {
            log.error("Stripe error creating portal session: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Error creating customer portal session."));
        } catch (IllegalStateException e) {
            log.warn("User {} tried to access portal without Stripe customer ID", user.getEmail());
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    private Organization findUserOrganization(User user) {
        List<Membership> memberships = membershipRepository.findByUserId(user.getId());
        if (memberships.isEmpty()) {
            throw new IllegalStateException("User is not part of any organization.");
        }

        UUID organizationId = memberships.get(0).getOrganizationId();
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new IllegalStateException("Organization not found for user's membership."));
    }
}