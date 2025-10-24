package br.com.stanleydev.backendboilerplate.todo.model;

import br.com.stanleydev.backendboilerplate.tenant.TenantAwareBaseEntity;
import br.com.stanleydev.backendboilerplate.tenant.TenantListener;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "todos")
@EntityListeners(TenantListener.class)
public class Todo extends TenantAwareBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String task;

    private boolean completed = false;
}