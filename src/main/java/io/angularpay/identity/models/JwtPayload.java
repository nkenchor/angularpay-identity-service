package io.angularpay.identity.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JwtPayload {

    private String jti;
    private String iss;
    private String aud;
    private String azp;
    private String sub;
    private long iat;
    private long exp;
    private String email;
    private String firstName;
    private String lastName;
    private String userReference;
    private List<String> roles;
}
