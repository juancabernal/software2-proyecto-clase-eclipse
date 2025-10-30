package co.edu.uco.notificationservice.catalog;

public class Notification {

    private String key;
    private String channel;
    private String subject;
    private String body;

    public Notification() {
    }

    public Notification(String key, String channel, String subject, String body) {
        setKey(key);
        setChannel(channel);
        setSubject(subject);
        setBody(body);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
