package co.edu.uco.messageservice.catalog;

/**
 * Evento emitido cuando un mensaje se crea, actualiza o elimina.
 */
public record MessageChange(CatalogEventType type, Message payload) {
}
