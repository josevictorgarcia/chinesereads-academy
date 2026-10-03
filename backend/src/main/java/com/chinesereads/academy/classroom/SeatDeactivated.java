package com.chinesereads.academy.classroom;

/** Un asiento se ha dado de baja: ChineseReads debe retirar el premium concedido por Academy. */
public record SeatDeactivated(long enrollmentId, long teacherId, long studentUserId) {}
