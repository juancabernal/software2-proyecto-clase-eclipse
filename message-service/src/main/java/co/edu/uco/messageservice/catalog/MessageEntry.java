package co.edu.uco.messageservice.catalog;

import java.util.Objects;

import jakarta.validation.constraints.NotBlank;

public class MessageEntry {

    @NotBlank
    private String code;
    @NotBlank
    private String userMessage;
    @NotBlank
    private String technicalMessage;
    private String generalMessage;

    public MessageEntry() {
    }

    public MessageEntry(String code, String userMessage, String technicalMessage) {
        this(code, userMessage, technicalMessage, null);
    }

    public MessageEntry(String code, String userMessage, String technicalMessage, String generalMessage) {
        this.code = Objects.requireNonNull(code, "code");
        this.userMessage = Objects.requireNonNull(userMessage, "userMessage");
        this.technicalMessage = Objects.requireNonNull(technicalMessage, "technicalMessage");
        this.generalMessage = generalMessage;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getTechnicalMessage() {
        return technicalMessage;
    }

    public void setTechnicalMessage(String technicalMessage) {
        this.technicalMessage = technicalMessage;
    }

    public String getGeneralMessage() {
        return generalMessage;
    }

    public void setGeneralMessage(String generalMessage) {
        this.generalMessage = generalMessage;
    }
}
