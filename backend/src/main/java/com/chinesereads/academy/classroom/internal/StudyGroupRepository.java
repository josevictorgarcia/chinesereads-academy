package com.chinesereads.academy.classroom.internal;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {

  List<StudyGroup> findByTeacherIdAndDeletedAtIsNullOrderByCreatedAtDesc(long teacherId);

  Optional<StudyGroup> findByIdAndTeacherIdAndDeletedAtIsNull(long id, long teacherId);
}
