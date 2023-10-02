
package io.angularpay.identity.exceptions;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ErrorResponse {

    @JsonProperty("error_reference")
    private String errorReference;
    private List<ErrorObject> errors;
    private String timestamp;
}
