/**
 * Aulas: grupos del profesor, altas de alumnos (asientos), pertenencia a grupos y códigos de invitación.
 * Publica SeatActivated/SeatDeactivated; aporta el rol STUDENT. Consulta la cuota a billing.
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = {"identity", "billing", "shared"})
package com.chinesereads.academy.classroom;
