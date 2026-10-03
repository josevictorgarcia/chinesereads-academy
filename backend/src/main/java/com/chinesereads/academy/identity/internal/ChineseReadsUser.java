package com.chinesereads.academy.identity.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.Immutable;

/**
 * Vista de SOLO LECTURA de la tabla de usuarios de ChineseReads (base de datos ajena; en MySQL es un catálogo JDBC, de ahí {@code catalog}). Solo las columnas que Academy
 * necesita; el contrato está en docs/integracion-chinesereads/chinesereads-user.contract.sql. Nunca se persiste:
 * el usuario MySQL de Academy no tiene permisos de escritura sobre este esquema.
 */
@Entity
@Immutable
@Table(name = "user", catalog = "chinesereads")
public class ChineseReadsUser {

  @Id
  private Long id;

  @Column(name = "email")
  private String email;

  @Column(name = "name")
  private String name;

  @Column(name = "language")
  private String language;

  @Column(name = "blocked", nullable = false)
  private boolean blocked;

  @Column(name = "premium_until")
  private LocalDateTime premiumUntil;

  protected ChineseReadsUser() {}

  public Long getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public String getName() {
    return name;
  }

  public String getLanguage() {
    return language;
  }

  public boolean isBlocked() {
    return blocked;
  }

  public LocalDateTime getPremiumUntil() {
    return premiumUntil;
  }
}
