package com.chinesereads.academy.billing.internal;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherSubscriptionRepository extends JpaRepository<TeacherSubscription, Long> {

  Optional<TeacherSubscription> findByTeacherId(long teacherId);
}
