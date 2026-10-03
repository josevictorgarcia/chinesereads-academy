package com.chinesereads.academy.identity.internal;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherProfileRepository extends JpaRepository<TeacherProfile, Long> {

  Optional<TeacherProfile> findByUserIdAndDeletedAtIsNull(long userId);

  boolean existsByUserIdAndDeletedAtIsNull(long userId);
}
