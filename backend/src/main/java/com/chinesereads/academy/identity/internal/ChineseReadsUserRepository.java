package com.chinesereads.academy.identity.internal;

import java.util.Optional;
import org.springframework.data.repository.Repository;

/** Solo consultas: deliberadamente no extiende CrudRepository para que no exista ningún método de escritura. */
public interface ChineseReadsUserRepository extends Repository<ChineseReadsUser, Long> {

  Optional<ChineseReadsUser> findByEmail(String email);

  Optional<ChineseReadsUser> findById(Long id);
}
