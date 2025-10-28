package br.com.stanleydev.backendboilerplate.organization.repository;

import br.com.stanleydev.backendboilerplate.organization.model.Membership;
import br.com.stanleydev.backendboilerplate.organization.model.OrganizationRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, UUID> {
    List<Membership> findByUserId(UUID userId);
    Optional<Membership> findByUserIdAndOrganizationId(UUID userId, UUID organizationId);
    // Find memberships for an org with a specific role (e.g., OWNER)
    Optional<Membership> findByOrganizationIdAndRole(UUID organizationId, OrganizationRole role);
    // You could also use List<Membership> if multiple owners were possible
}