package br.com.stanleydev.backendboilerplate.todo.controller;

import br.com.stanleydev.backendboilerplate.todo.dto.CreateTodoRequest;
import br.com.stanleydev.backendboilerplate.todo.dto.TodoResponse;
import br.com.stanleydev.backendboilerplate.todo.dto.UpdateTodoRequest;
import br.com.stanleydev.backendboilerplate.todo.service.TodoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;


    @GetMapping
    public ResponseEntity<List<TodoResponse>> getAllTodos() {
        return ResponseEntity.ok(todoService.getAllTodos());
    }


    @PostMapping

    public ResponseEntity<TodoResponse> createTodo(@Valid @RequestBody CreateTodoRequest request) {
        TodoResponse savedTodo = todoService.createTodo(request);
        return ResponseEntity.ok(savedTodo);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TodoResponse> updateTodo(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTodoRequest request
    ) {
        TodoResponse updatedTodo = todoService.updateTodo(id, request);
        return ResponseEntity.ok(updatedTodo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(@PathVariable UUID id) {
        todoService.deleteTodo(id);
        return ResponseEntity.noContent().build();
    }
}