
package io.angularpay.identity.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SendNotificationRequestApiModel {

    @JsonProperty("client_reference")
    private String clientReference;
    private NotificationChannel channel;
    private NotificationType type;
    private String subject;
    private String from;
    private String to;
    private String message;
    @JsonProperty("send_at")
    private String sendAt;
}
