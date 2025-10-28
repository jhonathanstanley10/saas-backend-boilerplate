import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TodoService } from './todo.service';
import type { Todo } from '../../shared/models/todo.model';
import { AppStateService } from '../../core/services/app-state.service'; // <-- IMPORT AppStateService
import { SpinnerComponent } from '../../shared/components/spinner/spinner.component';
import { finalize } from 'rxjs/operators';
import { FormsModule } from '@angular/forms'; // Import FormsModule

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, SpinnerComponent, FormsModule], // Add FormsModule
  template: `
    <h1 class="mb-6 text-3xl font-bold text-gray-800">
      Welcome, {{ appState.currentUser()?.firstName }}!
    </h1>
    <div class="max-w-2xl">
      <div class="rounded-xl bg-white p-6 shadow-lg">
        <h2 class="mb-4 text-xl font-semibold text-gray-700">Your Todos</h2>

        <!-- Add Todo Form -->
        <form (ngSubmit)="addTodo()" class="mb-4 flex gap-3">
          <input
            #taskInput
            [(ngModel)]="newTask"
            name="task"
            type="text"
            placeholder="What needs to be done?"
            class="flex-1 rounded-lg border border-gray-300 px-4 py-2 shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500"
            (keyup.enter)="addTodo()"
          />
          <button
            type="submit"
            [disabled]="isLoading() || newTask.trim() === ''"
            class="flex w-24 items-center justify-center rounded-lg bg-indigo-600 px-5 py-2 font-medium text-white shadow-md transition hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 disabled:opacity-50"
          >
            @if(isLoading() && currentAction() === 'add') {
            <app-spinner />
            } @else {
            Add
            }
          </button>
        </form>

        <!-- Todo List -->
        <ul class="space-y-3">
          @for(todo of todos(); track todo.id) {
          <li
            class="flex items-center gap-3 rounded-lg p-2 transition-all"
            [class.opacity-50]="isLoading() && currentActionId() === todo.id"
          >
            <!-- Checkbox -->
            <input
              type="checkbox"
              [checked]="todo.completed"
              [disabled]="isLoading() && currentActionId() === todo.id"
              (change)="toggleTodo(todo.id, !todo.completed)"
              class="h-5 w-5 rounded border-gray-300 text-indigo-600 focus:ring-indigo-500 disabled:cursor-not-allowed"
            />
            <span
              [class.line-through]="todo.completed"
              [class.text-gray-500]="todo.completed"
              class="flex-1 text-gray-800"
            >
              {{ todo.task }}
            </span>
            <!-- Delete Button -->
            <button
              (click)="deleteTodo(todo.id)"
              [disabled]="isLoading() && currentActionId() === todo.id"
              class="text-gray-400 hover:text-red-500 disabled:cursor-not-allowed disabled:opacity-50"
            >
              <!-- Trash Icon -->
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <path d="M3 6h18" />
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6" />
                <path d="M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
              </svg>
            </button>
          </li>
          } @empty { @if (!isLoading() || currentAction() !== 'load') {
          <p class="py-4 text-center text-gray-500">You're all caught up!</p>
          } }
        </ul>
        @if(isLoading() && currentAction() === 'load') {
        <div class="flex w-full justify-center py-4">
          <app-spinner />
        </div>
        }
      </div>
    </div>
  `,
})
export class DashboardComponent implements OnInit {
  appState = inject(AppStateService);
  private todoService = inject(TodoService);

  todos = signal<Todo[]>([]);
  isLoading = signal(false);
  currentAction = signal<'load' | 'add' | 'update' | 'delete' | null>(null);
  currentActionId = signal<string | null>(null); // Track which todo is being acted upon

  newTask: string = '';

  ngOnInit() {
    this.fetchTodos();
  }

  fetchTodos() {
    this.isLoading.set(true);
    this.currentAction.set('load');
    this.currentActionId.set(null);
    this.todoService
      .getTodos()
      .pipe(
        finalize(() => {
          this.isLoading.set(false);
          this.currentAction.set(null);
        })
      )
      .subscribe((data) => {
        this.todos.set(data);
      });
  }

  addTodo() {
    const task = this.newTask.trim();
    if (!task) return;

    this.isLoading.set(true);
    this.currentAction.set('add');
    this.currentActionId.set(null);
    this.todoService
      .addTodo(task)
      .pipe(
        finalize(() => {
          this.isLoading.set(false);
          this.currentAction.set(null);
        })
      )
      .subscribe((newTodo) => {
        this.todos.update((current) => [...current, newTodo]);
        this.newTask = '';
      });
  }

  // --- Toggle Method with Debugging ---
  toggleTodo(id: string, completed: boolean) {
    console.log(`[toggleTodo] Called for ID: ${id}, New status: ${completed}`); // <-- DEBUG LOG 1

    this.isLoading.set(true);
    this.currentAction.set('update');
    this.currentActionId.set(id);

    this.todoService
      .updateTodo(id, completed) // Call service with new status
      .pipe(
        finalize(() => {
          console.log(`[toggleTodo] Finalizing for ID: ${id}`); // <-- DEBUG LOG 2
          this.isLoading.set(false);
          this.currentAction.set(null);
          this.currentActionId.set(null);
        })
      )
      .subscribe({
          next: (updatedTodo) => {
             console.log(`[toggleTodo] Success for ID: ${id}. Received:`, updatedTodo); // <-- DEBUG LOG 3
            // Update the local state signal
            this.todos.update((currentTodos) =>
              currentTodos.map((t) => (t.id === id ? updatedTodo : t))
            );
             console.log('[toggleTodo] Local state updated.'); // <-- DEBUG LOG 4
          },
          error: (err) => {
             console.error(`[toggleTodo] Error for ID: ${id}:`, err); // <-- DEBUG LOG 5
          }
      });
  }

  deleteTodo(id: string) {
    this.isLoading.set(true);
    this.currentAction.set('delete');
    this.currentActionId.set(id);

    this.todoService
      .deleteTodo(id)
      .pipe(
        finalize(() => {
          this.isLoading.set(false);
          this.currentAction.set(null);
          this.currentActionId.set(null);
        })
      )
      .subscribe({
          next: () => {
            this.todos.update((current) => current.filter((t) => t.id !== id));
          },
          error: (err) => {
            console.error("Failed to delete todo:", err);
          }
      });
  }
}
