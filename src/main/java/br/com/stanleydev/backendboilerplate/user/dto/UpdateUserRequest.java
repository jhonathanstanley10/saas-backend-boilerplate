package br.com.stanleydev.backendboilerplate.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @Size(min = 1)
    private String firstName;

    @Size(min = 1)
    private String lastName;
}