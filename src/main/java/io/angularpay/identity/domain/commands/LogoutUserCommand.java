package io.angularpay.identity.domain.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.identity.adapters.outbound.MongoAdapter;
import io.angularpay.identity.adapters.outbound.RedisAdapter;
import io.angularpay.identity.domain.Role;
import io.angularpay.identity.domain.UserIdentity;
import io.angularpay.identity.exceptions.CommandException;
import io.angularpay.identity.exceptions.ErrorObject;
import io.angularpay.identity.models.LogoutUserCommandRequest;
import io.angularpay.identity.validation.DefaultConstraintValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static io.angularpay.identity.exceptions.ErrorCode.UNRECOGNIZED_DEVICE_ERROR;
import static io.angularpay.identity.helpers.CommandHelper.getRequestByUsernameOrThrow;

@Service
public class LogoutUserCommand extends AbstractCommand<LogoutUserCommandRequest, UserIdentity>
        implements AuthActivityPublisherCommand<UserIdentity> {

    private final DefaultConstraintValidator validator;
    private final MongoAdapter mongoAdapter;
    private final RedisAdapter redisAdapter;

    public LogoutUserCommand(
            ObjectMapper mapper,
            DefaultConstraintValidator validator,
            MongoAdapter mongoAdapter,
            RedisAdapter redisAdapter) {
        super("LogoutUserCommand", mapper);
        this.validator = validator;
        this.mongoAdapter = mongoAdapter;
        this.redisAdapter = redisAdapter;
    }

    @Override
    protected String getResourceOwner(LogoutUserCommandRequest request) {
        return request.getAuthenticatedUser().getUserReference();
    }

    @Override
    protected UserIdentity handle(LogoutUserCommandRequest request) {
        UserIdentity found = getRequestByUsernameOrThrow(this.mongoAdapter, request.getAuthenticatedUser().getUsername());
        if (found.getDevices().stream().noneMatch(x -> x.equalsIgnoreCase(request.getAuthenticatedUser().getDeviceId()))) {
            throw CommandException.builder()
                    .status(HttpStatus.FORBIDDEN)
                    .errorCode(UNRECOGNIZED_DEVICE_ERROR)
                    .message(UNRECOGNIZED_DEVICE_ERROR.getDefaultMessage())
                    .build();
        }
        return found;
    }

    @Override
    protected List<ErrorObject> validate(LogoutUserCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Arrays.asList(Role.ROLE_IDENTITY_ADMIN, Role.ROLE_SUPER_ADMIN);
    }

    @Override
    public UserIdentity getUserIdentity(UserIdentity userIdentity) {
        return userIdentity;
    }

    @Override
    public void publishRevokedTokens(Map<String, String> revokedTokens) {
        this.redisAdapter.publishRevokedTokens(revokedTokens);
    }

}
