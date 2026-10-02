package com.chinesereads.academy.identity;

import java.util.Set;

/**
 * SPI: un módulo que sabe algo del usuario (p. ej. classroom sabe si tiene un asiento activo) aporta roles
 * sin que identity dependa de él. Se consulta en cada petición autenticada; debe ser barato o cacheado.
 */
public interface RoleContributor {

  Set<AcademyRole> rolesFor(long userId);
}
