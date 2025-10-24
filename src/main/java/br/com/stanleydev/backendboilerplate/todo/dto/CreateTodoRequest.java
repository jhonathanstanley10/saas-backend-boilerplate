package br.com.stanleydev.backendboilerplate.todo.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class CreateTodoRequest {
    @NotEmpty
    private String task;
}