/** Espejo de los records Java del backend (módulo identity). Mantener en sincronía con sus DTOs. */
export type AcademyRole = 'TEACHER' | 'STUDENT';

export interface TeacherSummary {
  id: number;
  displayName: string;
}

export interface MeResponse {
  userId: number;
  email: string;
  name: string;
  language: string;
  roles: AcademyRole[];
  teacher: TeacherSummary | null;
}

export interface RegisterTeacherRequest {
  displayName: string;
}

export interface TeacherResponse {
  id: number;
  displayName: string;
  language: string;
}

/** RFC 9457 + extensiones de Academy. */
export interface ProblemDetail {
  type?: string;
  title?: string;
  status: number;
  detail?: string;
  code?: string;
  errorId?: string;
  correlationId?: string;
  errors?: { field: string; message: string }[];
}

// ---- classroom (espejo de ClassroomViews.java) ----
export interface GroupSummary {
  id: number;
  name: string;
  level: string | null;
  activeMembers: number;
  createdAt: string;
}

export interface Member {
  enrollmentId: number;
  studentUserId: number;
  name: string;
  email: string | null;
  joinedAt: string;
}

export interface InviteView {
  code: string;
  expiresAt: string;
  maxUses: number | null;
  uses: number;
}

export interface SeatUsage {
  used: number;
  total: number;
  periodEnd: string | null;
  subscriptionActive: boolean;
}

export interface GroupDetail {
  id: number;
  name: string;
  level: string | null;
  description: string | null;
  members: Member[];
  activeInvite: InviteView | null;
  seats: SeatUsage;
}

export interface CreateGroupRequest {
  name: string;
  level?: string;
  description?: string;
}

export interface StudentGroup {
  groupId: number;
  name: string;
  level: string | null;
  teacherName: string;
  joinedAt: string;
}

export interface JoinResult {
  groupId: number;
  groupName: string;
  alreadyMember: boolean;
}
