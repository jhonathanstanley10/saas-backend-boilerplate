import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment.development';
import type { Todo } from '../../shared/models/todo.model';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class TodoService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/todos`;

  getTodos(): Observable<Todo[]> {
    return this.http.get<Todo[]>(this.apiUrl);
  }

  addTodo(task: string): Observable<Todo> {
    return this.http.post<Todo>(this.apiUrl, { task });
  }

  // --- CORRECTED METHOD ---
  updateTodo(id: string, completed: boolean): Observable<Todo> {
    // The backend PATCH endpoint expects an object like { "completed": true/false }
    // We need to wrap the boolean in an object here.
    const payload = { completed: completed }; 
    return this.http.patch<Todo>(`${this.apiUrl}/${id}`, payload); 
  }

  deleteTodo(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
