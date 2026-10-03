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
