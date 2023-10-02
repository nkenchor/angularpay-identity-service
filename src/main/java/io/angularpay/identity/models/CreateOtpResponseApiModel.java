
package io.angularpay.identity.models;

import lombok.Data;

@Data
public class CreateOtpResponseApiModel {

    private String reference;
    private String code;
}
