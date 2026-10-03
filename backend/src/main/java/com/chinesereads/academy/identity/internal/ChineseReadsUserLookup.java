package com.chinesereads.academy.identity.internal;

import java.util.Optional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lectura cacheada (60 s, ver application.yml) del usuario matriz: evita una consulta a la base de datos
 * compartida en cada petición, manteniendo el bloqueo de cuentas efectivo en menos de un minuto.
 */
@Component
public class ChineseReadsUserLookup {

  public static final String CACHE = "chinesereads-users";

  private final ChineseReadsUserRepository repository;

  public ChineseReadsUserLookup(ChineseReadsUserRepository repository) {
    this.repository = repository;
  }

  @Cacheable(cacheNames = CACHE, key = "#email")
  @Transactional(readOnly = true)
  public Optional<UserSnapshot> byEmail(String email) {
    return repository.findByEmail(email).map(UserSnapshot::of);
  }

  /** Copia inmutable y serializable para la caché (nunca se cachea la entidad JPA). */
  public record UserSnapshot(long id, String email, String name, String language, boolean blocked) {
    static UserSnapshot of(ChineseReadsUser u) {
      return new UserSnapshot(u.getId(), u.getEmail(), u.getName(), u.getLanguage(), u.isBlocked());
    }
  }
}
