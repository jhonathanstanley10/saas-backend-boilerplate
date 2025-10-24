package br.com.stanleydev.backendboilerplate.user.controller;

import br.com.stanleydev.backendboilerplate.user.dto.UpdateUserRequest;
import br.com.stanleydev.backendboilerplate.user.dto.UserResponse;
import br.com.stanleydev.backendboilerplate.user.model.User;
import br.com.stanleydev.backendboilerplate.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @GetMapping("/my-profile")
    public ResponseEntity<UserResponse> getMyProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.mapToUserResponse(user));
    }

    @PutMapping("/my-profile")
    public ResponseEntity<UserResponse> updateMyProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        UserResponse updatedUser = userService.updateUserProfile(user, request);
        return ResponseEntity.ok(updatedUser);
    }
}