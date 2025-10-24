package br.com.stanleydev.backendboilerplate.user.dto;

import br.com.stanleydev.backendboilerplate.user.model.SubscriptionStatus;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserResponse {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private SubscriptionStatus subscriptionStatus;
}