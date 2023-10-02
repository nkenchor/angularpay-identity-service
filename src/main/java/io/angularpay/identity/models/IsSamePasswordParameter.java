package io.angularpay.identity.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IsSamePasswordParameter {
    private String hashedPassword;
    private String plaintextPassword;
}
