
package io.angularpay.identity.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Document("user_identities")
public class UserIdentity {

    @Id
    private String id;
    @Version
    private int version;
    @JsonProperty("user_reference")
    private String userReference;
    private String username;
    private String password;
    private String email;
    private String firstname;
    private String lastname;
    private boolean enabled;
    @JsonProperty("is_deleted")
    private boolean deleted;
    private Set<Role> roles;
    private Set<String> devices;
    @JsonProperty("access_control")
    private LoginActivity loginActivity;
    @JsonProperty("created_on")
    private String createdOn;
    @JsonProperty("last_modified")
    private String lastModified;
    private PasswordReset passwordReset;
}
