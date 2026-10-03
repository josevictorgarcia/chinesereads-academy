package com.chinesereads.academy.identity;

/** Evento de dominio: un usuario de ChineseReads se ha dado de alta como profesor en Academy. */
public record TeacherRegistered(long teacherId, long userId) {}
