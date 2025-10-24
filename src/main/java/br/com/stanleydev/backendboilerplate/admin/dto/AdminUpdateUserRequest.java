package br.com.stanleydev.backendboilerplate.admin.dto;

import br.com.stanleydev.backendboilerplate.user.model.Role;
import br.com.stanleydev.backendboilerplate.user.model.SubscriptionStatus;
import lombok.Data;

@Data
public class AdminUpdateUserRequest {
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private SubscriptionStatus subscriptionStatus;
}