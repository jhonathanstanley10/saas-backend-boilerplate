package br.com.stanleydev.backendboilerplate.organization.service;

import br.com.stanleydev.backendboilerplate.exception.ResourceNotFoundException;
import br.com.stanleydev.backendboilerplate.organization.dto.OrganizationResponse;
import br.com.stanleydev.backendboilerplate.organization.model.Membership;
import br.com.stanleydev.backendboilerplate.organization.model.Organization;
import br.com.stanleydev.backendboilerplate.organization.repository.MembershipRepository;
import br.com.stanleydev.backendboilerplate.organization.repository.OrganizationRepository;
import br.com.stanleydev.backendboilerplate.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final MembershipRepository membershipRepository;

    @Transactional(readOnly = true)
    public OrganizationResponse getMyOrganization(User user) {
        // Find the user's membership
        List<Membership> memberships = membershipRepository.findByUserId(user.getId());
        if (memberships.isEmpty()) {
            throw new ResourceNotFoundException("No membership found for user: " + user.getEmail());
        }

        // For this boilerplate, we assume the user's first membership is their primary one
        UUID organizationId = memberships.get(0).getOrganizationId();

        // Find the organization
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + organizationId));

        return OrganizationResponse.fromEntity(organization);
    }
}
