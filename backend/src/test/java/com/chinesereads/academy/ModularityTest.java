package com.chinesereads.academy;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Verifica las fronteras entre módulos (sin ciclos, sin acceso a paquetes internal ajenos) y genera la
 * documentación de módulos en target/spring-modulith-docs, que se copia a docs/arquitectura/.
 */
class ModularityTest {

  static final ApplicationModules modules = ApplicationModules.of(AcademyApplication.class);

  @Test
  void modulesRespectTheirBoundaries() {
    modules.verify();
  }

  @Test
  void writeDocumentation() {
    new Documenter(modules).writeDocumentation();
  }
}
