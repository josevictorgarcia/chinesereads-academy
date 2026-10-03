package com.chinesereads.academy.classroom;

import java.time.Instant;

/** Un alumno ha ocupado un asiento de un profesor: ChineseReads debe darle premium hasta {@code premiumUntil}. */
public record SeatActivated(long enrollmentId, long teacherId, long studentUserId, Instant premiumUntil) {}
