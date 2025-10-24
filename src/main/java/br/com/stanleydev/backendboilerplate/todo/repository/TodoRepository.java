package br.com.stanleydev.backendboilerplate.todo.repository;

import br.com.stanleydev.backendboilerplate.todo.model.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TodoRepository extends JpaRepository<Todo, UUID> {
}
