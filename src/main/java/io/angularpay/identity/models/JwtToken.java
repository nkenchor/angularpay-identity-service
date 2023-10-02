package io.angularpay.identity.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JwtToken {

    private String header;
    private String payload;
    private String signature;
}
