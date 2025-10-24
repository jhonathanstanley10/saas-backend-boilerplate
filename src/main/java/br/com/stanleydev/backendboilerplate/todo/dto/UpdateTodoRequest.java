package br.com.stanleydev.backendboilerplate.todo.dto;

import lombok.Data;

@Data
public class UpdateTodoRequest {

    private Boolean completed;
}