package br.com.stanleydev.backendboilerplate.organization.controller;

import br.com.stanleydev.backendboilerplate.organization.dto.OrganizationResponse;
import br.com.stanleydev.backendboilerplate.organization.service.OrganizationService;
import br.com.stanleydev.backendboilerplate.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @GetMapping("/my-organization")
    public ResponseEntity<OrganizationResponse> getMyOrganization(
            @AuthenticationPrincipal User user
    ) {
        OrganizationResponse response = organizationService.getMyOrganization(user);
        return ResponseEntity.ok(response);
    }
}
