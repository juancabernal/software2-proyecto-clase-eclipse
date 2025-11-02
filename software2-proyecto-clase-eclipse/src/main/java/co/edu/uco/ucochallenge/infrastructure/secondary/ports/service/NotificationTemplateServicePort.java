package co.edu.uco.ucochallenge.infrastructure.secondary.ports.service;

import java.util.Optional;

import co.edu.uco.ucochallenge.application.notification.NotificationTemplate;

public interface NotificationTemplateServicePort {

    Optional<NotificationTemplate> findByKey(String key);
}