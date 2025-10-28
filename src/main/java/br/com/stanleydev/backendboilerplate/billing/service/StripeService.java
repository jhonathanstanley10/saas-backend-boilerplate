package br.com.stanleydev.backendboilerplate.billing.service;

import br.com.stanleydev.backendboilerplate.organization.model.Membership;
import br.com.stanleydev.backendboilerplate.organization.model.Organization;
import br.com.stanleydev.backendboilerplate.organization.model.OrganizationRole;
import br.com.stanleydev.backendboilerplate.organization.repository.MembershipRepository;
import br.com.stanleydev.backendboilerplate.organization.repository.OrganizationRepository;
import br.com.stanleydev.backendboilerplate.user.model.SubscriptionStatus;
import br.com.stanleydev.backendboilerplate.user.model.User;
import br.com.stanleydev.backendboilerplate.user.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.CustomerCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StripeService {

    private static final Logger log = LoggerFactory.getLogger(StripeService.class);

    private final UserRepository userRepository;

    private final OrganizationRepository organizationRepository;

    private final MembershipRepository membershipRepository;


    @Value("${stripe.api.secret-key}")
    private String stripeSecretKey;

    @Value("${stripe.api.price-id}")
    private String stripePriceId;

    @Value("${stripe.api.webhook-secret}")
    private String webhookSecret;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @Transactional
    public Session createCheckoutSession(Organization organization, String successUrl, String cancelUrl) throws StripeException {

        String stripeCustomerId = organization.getStripeCustomerId();
        if (stripeCustomerId == null) {

            String ownerEmail = findOwnerEmail(organization.getId());

            CustomerCreateParams.Builder customerParamsBuilder = CustomerCreateParams.builder()
                    .setName(organization.getName());
            if (ownerEmail != null) {
                customerParamsBuilder.setEmail(ownerEmail);
            }

            Customer customer = Customer.create(customerParamsBuilder.build());
            stripeCustomerId = customer.getId();
            organization.setStripeCustomerId(stripeCustomerId);
            organizationRepository.save(organization);
        }


        com.stripe.param.checkout.SessionCreateParams params = com.stripe.param.checkout.SessionCreateParams.builder()
                .setMode(com.stripe.param.checkout.SessionCreateParams.Mode.SUBSCRIPTION)
                .setCustomer(stripeCustomerId)
                .addLineItem(
                        com.stripe.param.checkout.SessionCreateParams.LineItem.builder()
                                .setPrice(stripePriceId)
                                .setQuantity(1L)
                                .build()
                )
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .build();

        return Session.create(params);
    }

    @Transactional
    public void handleWebhook(String payload, String sigHeader) throws SignatureVerificationException {
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Stripe webhook signature verification failed.");
            throw e;
        }

        StripeObject stripeObject = event.getData().getObject();

        switch (event.getType()) {
            case "checkout.session.completed": {
                log.info("Checkout session completed event received.");
                break;
            }
            case "customer.subscription.created": {
                log.info("Subscription created event received.");
                Subscription subscription = (Subscription) stripeObject;
                String stripeCustomerId = subscription.getCustomer();
                String status = subscription.getStatus();
                updateOrganizationSubscriptionStatus(stripeCustomerId, status);
                break;
            }
            case "customer.subscription.updated": {
                log.info("Subscription updated event received.");
                Subscription subscription = (Subscription) stripeObject;
                String stripeCustomerId = subscription.getCustomer();
                String status = subscription.getStatus();

                updateOrganizationSubscriptionStatus(stripeCustomerId, status);
                break;
            }
            case "customer.subscription.deleted": {
                log.info("Subscription deleted event received.");
                Subscription subscription = (Subscription) stripeObject;
                String stripeCustomerId = subscription.getCustomer();

                updateOrganizationSubscriptionStatus(stripeCustomerId, "canceled");
                break;
            }
            default:
                log.warn("Unhandled Stripe event type: {}", event.getType());
        }
    }

    private void updateOrganizationSubscriptionStatus(String stripeCustomerId, String stripeStatus) {
        Organization organization = organizationRepository.findByStripeCustomerId(stripeCustomerId)
                .orElseThrow(() -> new RuntimeException("Organization with Stripe Customer ID " + stripeCustomerId + " not found."));

        log.info("Stripe status for Org {}: {}", organization.getId(), stripeStatus);
        if ("active".equals(stripeStatus) || "trialing".equals(stripeStatus)) {
            organization.setSubscriptionStatus(SubscriptionStatus.PREMIUM);
            log.info("Organization {} set to PREMIUM", organization.getId());
        } else {
            // "canceled", "incomplete_expired", "unpaid", "past_due", etc.
            organization.setSubscriptionStatus(SubscriptionStatus.FREE);
            log.info("Organization {} set to FREE", organization.getId());
        }
        organizationRepository.save(organization);
    }

    @Transactional(readOnly = true)
    public com.stripe.model.billingportal.Session createPortalSession(Organization organization, String returnUrl) throws StripeException {
        String stripeCustomerId = organization.getStripeCustomerId();
        if (stripeCustomerId == null) {
            throw new IllegalStateException("Organization has no Stripe customer ID.");
        }
        com.stripe.param.billingportal.SessionCreateParams params =
                com.stripe.param.billingportal.SessionCreateParams.builder()
                        .setCustomer(stripeCustomerId)
                        .setReturnUrl(returnUrl)
                        .build();
        return com.stripe.model.billingportal.Session.create(params);
    }

    private String findOwnerEmail(UUID organizationId) {
        // Find the membership record for the owner
        Optional<Membership> ownerMembershipOpt = membershipRepository.findByOrganizationIdAndRole(organizationId, OrganizationRole.OWNER);

        if (ownerMembershipOpt.isEmpty()) {
            log.error("Could not find OWNER membership for organization ID: {}", organizationId);
            return null; // Or throw an exception if this is critical
        }

        UUID ownerUserId = ownerMembershipOpt.get().getUserId();

        // Find the user associated with that membership
        Optional<User> ownerUserOpt = userRepository.findById(ownerUserId);

        if (ownerUserOpt.isEmpty()) {
            log.error("Could not find User with ID {} who is OWNER of org {}", ownerUserId, organizationId);
            return null; // Or throw an exception
        }

        return ownerUserOpt.get().getEmail();
    }
}