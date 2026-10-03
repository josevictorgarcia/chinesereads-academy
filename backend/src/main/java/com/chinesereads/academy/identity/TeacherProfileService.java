package com.chinesereads.academy.identity;

import com.chinesereads.academy.identity.internal.TeacherProfile;
import com.chinesereads.academy.identity.internal.TeacherProfileRepository;
import com.chinesereads.academy.shared.error.AcademyException;
import com.chinesereads.academy.shared.error.ErrorCode;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Alta y consulta del perfil de profesor. */
@Service
public class TeacherProfileService {

  private final TeacherProfileRepository repository;
  private final ApplicationEventPublisher events;
  private final Clock clock;

  public TeacherProfileService(TeacherProfileRepository repository, ApplicationEventPublisher events, Clock clock) {
    this.repository = repository;
    this.events = events;
    this.clock = clock;
  }

  @Transactional(readOnly = true)
  public Optional<TeacherProfileView> findById(long teacherId) {
    return repository.findById(teacherId).filter(p -> p.getDeletedAt() == null).map(TeacherProfileService::toView);
  }

  @Transactional(readOnly = true)
  public Optional<TeacherProfileView> findByUserId(long userId) {
    return repository.findByUserIdAndDeletedAtIsNull(userId).map(TeacherProfileService::toView);
  }

  @Transactional
  public TeacherProfileView register(CurrentUser user, String displayName) {
    if (repository.existsByUserIdAndDeletedAtIsNull(user.userId())) {
      throw new AcademyException(ErrorCode.IDENTITY_TEACHER_ALREADY_REGISTERED, "User already has a teacher profile");
    }
    Instant now = Instant.now(clock);
    String language = user.language() != null && !user.language().isBlank() ? user.language() : "es";
    TeacherProfile profile = repository.save(new TeacherProfile(user.userId(), displayName.trim(), language, now));
    events.publishEvent(new TeacherRegistered(profile.getId(), user.userId()));
    return toView(profile);
  }

  private static TeacherProfileView toView(TeacherProfile p) {
    return new TeacherProfileView(p.getId(), p.getUserId(), p.getDisplayName(), p.getLanguage());
  }
}
