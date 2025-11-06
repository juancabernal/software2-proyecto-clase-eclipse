package co.edu.uco.messageservice.domain.event;

/**
 * Identifies the type of catalog change triggered by the system when a message
 * is modified.
 */
public enum MessageCatalogEventType {
    CREATED,
    UPDATED,
    DELETED
}
