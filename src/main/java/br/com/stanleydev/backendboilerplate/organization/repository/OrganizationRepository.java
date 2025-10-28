package br.com.stanleydev.backendboilerplate.organization.repository;

import br.com.stanleydev.backendboilerplate.organization.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    Optional<Organization> findByTenantId(String tenantId);
    Optional<Organization> findByStripeCustomerId(String stripeCustomerId);
}