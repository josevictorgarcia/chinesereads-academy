import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { MeResponse, RegisterTeacherRequest, TeacherResponse } from './models';

/** Cliente tipado del módulo identity del backend. Credenciales y cabeceras las pone el interceptor. */
@Injectable({ providedIn: 'root' })
export class IdentityApiService {
  private readonly http = inject(HttpClient);

  me(): Observable<MeResponse> {
    return this.http.get<MeResponse>('/api/me');
  }

  registerTeacher(request: RegisterTeacherRequest): Observable<TeacherResponse> {
    return this.http.post<TeacherResponse>('/api/teachers/me', request);
  }
}
