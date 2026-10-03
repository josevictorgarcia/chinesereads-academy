import { Routes } from '@angular/router';
import { authGuard, teacherGuard } from './core/guards';

/**
 * Rutas declaradas una vez y montadas dos veces: inglés en la raíz y español bajo `/es`
 * (mismo esquema que ChineseReads). Cada nivel tiene su propio comodín 404.
 */
export const appRoutes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    loadComponent: () => import('./features/public/landing').then((m) => m.Landing),
  },
  {
    path: 'para-profesores',
    loadComponent: () => import('./features/public/for-teachers').then((m) => m.ForTeachers),
  },
  {
    path: 'precios',
    loadComponent: () => import('./features/public/pricing').then((m) => m.Pricing),
  },
  {
    path: 'privacy-policy',
    loadComponent: () => import('./features/public/privacy-policy').then((m) => m.PrivacyPolicy),
  },
  {
    path: 'terms-of-use',
    loadComponent: () => import('./features/public/terms-of-use').then((m) => m.TermsOfUse),
  },
  {
    path: 'teacher',
    canActivate: [authGuard],
    loadComponent: () => import('./features/teacher/teacher-home').then((m) => m.TeacherHome),
  },
  {
    path: 'teacher/groups/:id',
    canActivate: [authGuard, teacherGuard],
    loadComponent: () => import('./features/teacher/group-detail').then((m) => m.GroupDetailPage),
  },
  {
    path: 'student',
    canActivate: [authGuard],
    loadComponent: () => import('./features/student/student-home').then((m) => m.StudentHome),
  },
  // /join?code=XXXXXXXX: misma página del alumno con el código precargado (enlace que comparte el profesor).
  {
    path: 'join',
    canActivate: [authGuard],
    loadComponent: () => import('./features/student/student-home').then((m) => m.StudentHome),
  },
];

const notFound = {
  path: '**',
  loadComponent: () => import('./features/public/not-found').then((m) => m.NotFound),
};

export const routes: Routes = [
  ...appRoutes,
  { path: 'es', children: [...appRoutes, notFound] },
  notFound,
];
