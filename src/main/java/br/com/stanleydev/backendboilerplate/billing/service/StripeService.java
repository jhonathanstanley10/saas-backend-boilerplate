package br.com.stanleydev.backendboilerplate.billing.service;

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

@Service
@RequiredArgsConstructor
public class StripeService {

    private static final Logger log = LoggerFactory.getLogger(StripeService.class);

    private final UserRepository userRepository;

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
    public Session createCheckoutSession(User user, String successUrl, String cancelUrl) throws StripeException {

        String stripeCustomerId = user.getStripeCustomerId();
        if (stripeCustomerId == null) {


            CustomerCreateParams customerParams =
                    CustomerCreateParams.builder()
                            .setEmail(user.getEmail())
                            .setName(user.getEmail())
                            .build();

            Customer customer = Customer.create(customerParams);
            stripeCustomerId = customer.getId();
            user.setStripeCustomerId(stripeCustomerId);
            userRepository.save(user);
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
                updateSubscriptionStatus(stripeCustomerId, status);
                break;
            }
            case "customer.subscription.updated": {
                log.info("Subscription updated event received.");
                Subscription subscription = (Subscription) stripeObject;
                String stripeCustomerId = subscription.getCustomer();
                String status = subscription.getStatus();

                updateSubscriptionStatus(stripeCustomerId, status);
                break;
            }
            case "customer.subscription.deleted": {
                log.info("Subscription deleted event received.");
                Subscription subscription = (Subscription) stripeObject;
                String stripeCustomerId = subscription.getCustomer();

                updateSubscriptionStatus(stripeCustomerId, "canceled");
                break;
            }
            default:
                log.warn("Unhandled Stripe event type: {}", event.getType());
        }
    }

    private void updateSubscriptionStatus(String stripeCustomerId, String stripeStatus) {
        User user = userRepository.findByStripeCustomerId(stripeCustomerId)
                .orElseThrow(() -> new RuntimeException("User with Stripe Customer ID " + stripeCustomerId + " not found."));

        log.info("Status:" + stripeStatus);
        if (Objects.equals(stripeStatus, "active")) {
            user.setSubscriptionStatus(SubscriptionStatus.PREMIUM);
            log.info("User {} set to PREMIUM", user.getEmail());
        } else {
            user.setSubscriptionStatus(SubscriptionStatus.FREE);
            log.info("User {} set to FREE", user.getEmail());
        }
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public com.stripe.model.billingportal.Session createPortalSession(User user, String returnUrl) throws StripeException {

        String stripeCustomerId = user.getStripeCustomerId();
        if (stripeCustomerId == null) {
            throw new IllegalStateException("User has no Stripe customer ID.");
        }

        com.stripe.param.billingportal.SessionCreateParams params = com.stripe.param.billingportal.SessionCreateParams.builder()
                .setCustomer(stripeCustomerId)
                .setReturnUrl(returnUrl)
                .build();

        return com.stripe.model.billingportal.Session.create(params);
    }
}