package com.chinesereads.academy.classroom;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/** Publicación de eventos de asiento (envoltorio fino para poder verificarla en tests). */
@Component
public class SeatEvents {

  private final ApplicationEventPublisher publisher;

  public SeatEvents(ApplicationEventPublisher publisher) {
    this.publisher = publisher;
  }

  public void publish(Object event) {
    publisher.publishEvent(event);
  }
}
