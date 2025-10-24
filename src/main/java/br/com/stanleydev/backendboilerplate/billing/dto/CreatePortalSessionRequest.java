package br.com.stanleydev.backendboilerplate.billing.dto;

import jakarta.validation.constraints.NotEmpty;

public record CreatePortalSessionRequest(
        @NotEmpty
        String returnUrl
) {}