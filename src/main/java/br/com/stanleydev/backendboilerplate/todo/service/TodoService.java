package br.com.stanleydev.backendboilerplate.todo.service;

import br.com.stanleydev.backendboilerplate.exception.ResourceNotFoundException;
import br.com.stanleydev.backendboilerplate.todo.dto.CreateTodoRequest;
import br.com.stanleydev.backendboilerplate.todo.dto.TodoResponse;
import br.com.stanleydev.backendboilerplate.todo.dto.UpdateTodoRequest;
import br.com.stanleydev.backendboilerplate.todo.model.Todo;
import br.com.stanleydev.backendboilerplate.todo.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID; // ADD THIS
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;

    @Transactional(readOnly = true)
    public List<TodoResponse> getAllTodos() {
        return todoRepository.findAll().stream()
                .map(TodoResponse::fromEntity)
                .collect(Collectors.toList());
    }


    @Transactional
    public TodoResponse createTodo(CreateTodoRequest request) {
        Todo newTodo = new Todo();
        newTodo.setTask(request.getTask());
        newTodo.setCompleted(false);

        Todo savedTodo = todoRepository.save(newTodo);
        return TodoResponse.fromEntity(savedTodo);
    }


    @Transactional
    public TodoResponse updateTodo(UUID id, UpdateTodoRequest request) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id: " + id));

        if (request.getCompleted() != null) {
            todo.setCompleted(request.getCompleted());
        }

        Todo savedTodo = todoRepository.save(todo);
        return TodoResponse.fromEntity(savedTodo);
    }


    @Transactional
    public void deleteTodo(UUID id) {
        if (!todoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Todo not found with id: " + id);
        }

        todoRepository.deleteById(id);
    }
}