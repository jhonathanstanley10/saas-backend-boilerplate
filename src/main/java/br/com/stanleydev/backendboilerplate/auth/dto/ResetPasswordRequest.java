package br.com.stanleydev.backendboilerplate.auth.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotEmpty
    private String token;

    @NotEmpty
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String newPassword;
}