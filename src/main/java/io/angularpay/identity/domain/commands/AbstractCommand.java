package io.angularpay.identity.domain.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.identity.domain.Role;
import io.angularpay.identity.exceptions.CommandException;
import io.angularpay.identity.exceptions.ErrorObject;
import io.angularpay.identity.exceptions.ValidationException;
import io.angularpay.identity.models.AccessControl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.Executors;

import static io.angularpay.identity.exceptions.ErrorCode.*;
import static io.angularpay.identity.helpers.Helper.*;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractCommand<T extends AccessControl, R> {

    protected final String name;
    protected final ObjectMapper mapper;

    public R execute(T request) {
        try {
            if (this instanceof SensitiveDataCommand) {
                T commandMask = (T) ((SensitiveDataCommand) this).mask(request);
                log.info("received {} request {}", this.name, maskAuthenticatedUser(mapper, commandMask));
            } else {
                log.info("received {} request {}", this.name, maskAuthenticatedUser(mapper, request));
            }
            log.info("validating {} request...", this.name);

            if (this instanceof BruteForceGuardCommand) {
                if (!StringUtils.hasText(request.getAuthenticatedUser().getClientIp())) {
                    log.info("No IP found in request. Exiting with 403");
                    throw CommandException.builder()
                            .status(HttpStatus.FORBIDDEN)
                            .errorCode(MISSING_IP_ERROR)
                            .message(MISSING_IP_ERROR.getDefaultMessage())
                            .build();
                }

                boolean blocked = ((BruteForceGuardCommand) this).isBlocked(request.getAuthenticatedUser().getClientIp());
                if (blocked) {
                    log.info("Blocked! Login failure quota exceeded for IP: {}", request.getAuthenticatedUser().getClientIp());
                    throw CommandException.builder()
                            .status(HttpStatus.FORBIDDEN)
                            .errorCode(BLOCKED_IP_ERROR)
                            .message(BLOCKED_IP_ERROR.getDefaultMessage())
                            .build();
                }
            }

            List<ErrorObject> validationErrors = this.validate(request);

            if (!CollectionUtils.isEmpty(validationErrors)) {
                log.info("{} request validation failed!", this.name);
                log.info("validation errors: {}", writeAsStringOrDefault(mapper, validationErrors));
                ValidationException exception = new ValidationException(validationErrors);
                throw CommandException.builder()
                        .status(resolveStatus(validationErrors))
                        .errorCode(VALIDATION_ERROR)
                        .cause(exception)
                        .message(String.format("Validation failed for %s request", this.name))
                        .build();
            }

            boolean hasPermittedRole = hasPermittedRole(this.permittedRoles(), request.getAuthenticatedUser().getRoles());

            boolean isResourceOwner = false;
            String resourceOwner = this.getResourceOwner(request);
            if (!hasPermittedRole && StringUtils.hasText(resourceOwner) && StringUtils.hasText(request.getAuthenticatedUser().getDeviceId())) {
                isResourceOwner = request.getAuthenticatedUser().getUserReference().equalsIgnoreCase(resourceOwner);
            }

            if (!hasPermittedRole && !isResourceOwner) {
                throw CommandException.builder()
                        .status(HttpStatus.FORBIDDEN)
                        .errorCode(AUTHORIZATION_ERROR)
                        .message(String.format("Authorization failed for %s request", this.name))
                        .build();
            }

            R response = this.handle(request);
            log.info("{} request successfully processed", this.name);
            log.info("returning {} response {}", this.name, writeAsStringOrDefault(mapper, response));

            if (this instanceof AuthActivityPublisherCommand) {
                Executors.newSingleThreadExecutor().submit(() -> {
                    ((AuthActivityPublisherCommand) this).publishAuthActivity(response);
                });
            }

            if (this instanceof BruteForceGuardCommand) {
                log.info("Invalidating brute force cache on login success for IP: {}", request.getAuthenticatedUser().getClientIp());
                Executors.newSingleThreadExecutor().submit(() -> {
                    ((BruteForceGuardCommand) this).onLoginSuccess(request.getAuthenticatedUser().getClientIp());
                });
            }

            if (this instanceof ResourceReferenceCommand) {
                return ((ResourceReferenceCommand<R, R>) this).map(response);
            } else {
                return response;
            }
        } catch (Exception exception) {
            log.error("An error occurred while processing {} request", this.name, exception);
            if (exception instanceof CommandException) {
                if (((CommandException) exception).getStatus() == HttpStatus.UNAUTHORIZED
                        || ((CommandException) exception).getStatus() == HttpStatus.FORBIDDEN) {
                    CommandException commandException = ((CommandException) exception);
                    if (this instanceof BruteForceGuardCommand &&
                            commandException.getErrorCode() != MISSING_IP_ERROR &&
                            commandException.getErrorCode() != BLOCKED_IP_ERROR
                    ) {
                        log.info("Recording login failure from IP: {}", request.getAuthenticatedUser().getClientIp());
                        Executors.newSingleThreadExecutor().submit(() -> {
                            ((BruteForceGuardCommand) this).onLoginFailure(request.getAuthenticatedUser().getClientIp());
                        });
                    }
                }
                throw exception;
            } else {
                throw CommandException.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .errorCode(GENERIC_ERROR)
                        .cause(exception)
                        .message(String.format("An error occurred while processing %s request", this.name))
                        .build();
            }
        }
    }

    protected abstract String getResourceOwner(T request);

    protected abstract R handle(T request);

    protected abstract List<ErrorObject> validate(T request);

    protected abstract List<Role> permittedRoles();
}
