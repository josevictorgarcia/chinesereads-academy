import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  CreateGroupRequest,
  GroupDetail,
  GroupSummary,
  InviteView,
  JoinResult,
  SeatUsage,
  StudentGroup,
} from './models';

/** Cliente tipado del módulo classroom del backend. */
@Injectable({ providedIn: 'root' })
export class ClassroomApiService {
  private readonly http = inject(HttpClient);

  // --- profesor ---
  listGroups(): Observable<GroupSummary[]> {
    return this.http.get<GroupSummary[]>('/api/teacher/groups');
  }

  createGroup(request: CreateGroupRequest): Observable<GroupSummary> {
    return this.http.post<GroupSummary>('/api/teacher/groups', request);
  }

  groupDetail(groupId: number): Observable<GroupDetail> {
    return this.http.get<GroupDetail>(`/api/teacher/groups/${groupId}`);
  }

  createInvite(groupId: number): Observable<InviteView> {
    return this.http.post<InviteView>(`/api/teacher/groups/${groupId}/invite`, {});
  }

  revokeInvite(groupId: number): Observable<void> {
    return this.http.delete<void>(`/api/teacher/groups/${groupId}/invite`);
  }

  deactivateEnrollment(enrollmentId: number): Observable<void> {
    return this.http.delete<void>(`/api/teacher/enrollments/${enrollmentId}`);
  }

  seats(): Observable<SeatUsage> {
    return this.http.get<SeatUsage>('/api/teacher/seats');
  }

  // --- alumno ---
  join(code: string): Observable<JoinResult> {
    return this.http.post<JoinResult>('/api/student/join', { code });
  }

  myGroups(): Observable<StudentGroup[]> {
    return this.http.get<StudentGroup[]>('/api/student/groups');
  }
}
