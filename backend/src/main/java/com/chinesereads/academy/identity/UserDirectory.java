package com.chinesereads.academy.identity;

import com.chinesereads.academy.identity.internal.ChineseReadsUserRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lectura pública (para otros módulos) de datos básicos de usuarios de ChineseReads, por id. */
@Service
public class UserDirectory {

  private final ChineseReadsUserRepository repository;

  public UserDirectory(ChineseReadsUserRepository repository) {
    this.repository = repository;
  }

  @Transactional(readOnly = true)
  public Optional<UserSummary> byId(long userId) {
    return repository.findById(userId).map(u -> new UserSummary(u.getId(), u.getName(), u.getEmail(), u.getLanguage()));
  }

  public record UserSummary(long id, String name, String email, String language) {}
}
