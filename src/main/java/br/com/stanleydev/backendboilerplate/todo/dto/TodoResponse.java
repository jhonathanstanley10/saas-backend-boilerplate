package br.com.stanleydev.backendboilerplate.todo.dto;

import br.com.stanleydev.backendboilerplate.todo.model.Todo;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class TodoResponse {
    private UUID id;
    private String task;
    private boolean completed;
    
    public static TodoResponse fromEntity(Todo todo) {
        return TodoResponse.builder()
                .id(todo.getId())
                .task(todo.getTask())
                .completed(todo.isCompleted())
                .build();
    }
}