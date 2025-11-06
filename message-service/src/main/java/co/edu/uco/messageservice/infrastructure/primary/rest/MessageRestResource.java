package co.edu.uco.messageservice.infrastructure.primary.rest;

/**
 * REST resource that represents the payload exchanged with clients.
 */
public class MessageRestResource {

    private String key;
    private String value;

    public MessageRestResource() {
    }

    public MessageRestResource(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
