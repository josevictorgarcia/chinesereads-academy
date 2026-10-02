package com.chinesereads.academy.support;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.MountableFile;

/**
 * Un MySQL 8 real por JVM de tests con los dos esquemas: {@code academy} (lo migra Flyway) y una copia
 * mínima de {@code chinesereads} creada desde el contrato de lectura en docs/integracion-chinesereads.
 */
@Testcontainers
public abstract class MySqlTestSupport {

  private static final Path CONTRACT_SQL = Paths.get("..", "docs", "integracion-chinesereads",
      "chinesereads-user.contract.sql").toAbsolutePath().normalize();

  @Container
  @ServiceConnection
  static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.0")
      .withDatabaseName("academy")
      .withUsername("root")
      .withPassword("test")
      .withCopyFileToContainer(MountableFile.forHostPath(CONTRACT_SQL), "/docker-entrypoint-initdb.d/01-chinesereads-contract.sql")
      .withCopyFileToContainer(MountableFile.forClasspathResource("db/02-seed-chinesereads-users.sql"), "/docker-entrypoint-initdb.d/02-seed.sql");
}
