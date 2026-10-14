package org.projects.event.DAO;

import io.swagger.v3.oas.annotations.media.Schema;

public class Event {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    String id;
    @Schema(description = "senderEmail", example = "pranay.ranjan@gmail.com", requiredMode = Schema.RequiredMode.REQUIRED)
    String senderEmail;
    @Schema(description = "message", example = "Hi there!", requiredMode = Schema.RequiredMode.REQUIRED)
    String message;
    @Schema(description = "receiverEmail", example = "someone@gmail.com", requiredMode = Schema.RequiredMode.REQUIRED)
    String receiverEmail;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReceiverEmail() {
        return receiverEmail;
    }

    public void setReceiverEmail(String receiverEmail) {
        this.receiverEmail = receiverEmail;
    }

}
