package co.edu.uco.notificationservice.catalog;

/**
 * Evento emitido cuando una notificación se crea, actualiza o elimina.
 */
public record NotificationChange(CatalogEventType type, Notification payload) {
}
