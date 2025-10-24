package br.com.stanleydev.backendboilerplate.billing.controller;

import br.com.stanleydev.backendboilerplate.billing.dto.CreatePortalSessionRequest;
import br.com.stanleydev.backendboilerplate.billing.service.StripeService;
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

import java.util.Map;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private static final Logger log = LoggerFactory.getLogger(BillingController.class);
    private final StripeService stripeService;


    public record CreateCheckoutSessionRequest(String successUrl, String cancelUrl) {}


    @PostMapping("/create-checkout-session")
    public ResponseEntity<?> createCheckoutSession(
            @AuthenticationPrincipal User user,
            @RequestBody CreateCheckoutSessionRequest request
    ) {
        try {
            Session session = stripeService.createCheckoutSession(
                    user,
                    request.successUrl(),
                    request.cancelUrl()
            );

            return ResponseEntity.ok(Map.of("url", session.getUrl()));

        } catch (StripeException e) {

            return ResponseEntity.status(500).body(Map.of("error", "Error creating payment session."));
        }
    }

    @PostMapping("/create-portal-session")
    public ResponseEntity<?> createPortalSession(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreatePortalSessionRequest request
    ) {
        try {
            com.stripe.model.billingportal.Session portalSession = stripeService.createPortalSession(
                    user,
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
}