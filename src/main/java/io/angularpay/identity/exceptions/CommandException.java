package io.angularpay.identity.exceptions;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Getter
@Builder
public class CommandException extends ResponseStatusException {

    private final HttpStatus status;
    private final ErrorCode errorCode;
    private final String message;
    private final Throwable cause;

    public CommandException(
            HttpStatus status,
            ErrorCode errorCode,
            String message,
            Throwable cause) {
        super(status, errorCode.getDefaultMessage());
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
        this.cause = cause;
    }
}
