package br.com.stanleydev.backendboilerplate.organization.dto;

import br.com.stanleydev.backendboilerplate.organization.model.Organization;
import br.com.stanleydev.backendboilerplate.user.model.SubscriptionStatus;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class OrganizationResponse {
    private UUID id;
    private String name;
    private String stripeCustomerId;
    private SubscriptionStatus subscriptionStatus;
    private UUID ownerUserId;

    public static OrganizationResponse fromEntity(Organization organization) {
        return OrganizationResponse.builder()
                .id(organization.getId())
                .name(organization.getName())
                .stripeCustomerId(organization.getStripeCustomerId())
                .subscriptionStatus(organization.getSubscriptionStatus())
                .ownerUserId(organization.getOwnerUserId())
                .build();
    }
}
