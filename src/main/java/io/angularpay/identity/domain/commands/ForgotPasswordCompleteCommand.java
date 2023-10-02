package io.angularpay.identity.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.angularpay.identity.adapters.outbound.MongoAdapter;
import io.angularpay.identity.domain.PasswordReset;
import io.angularpay.identity.domain.Role;
import io.angularpay.identity.domain.UserIdentity;
import io.angularpay.identity.exceptions.CommandException;
import io.angularpay.identity.exceptions.ErrorObject;
import io.angularpay.identity.models.ForgotPasswordCompleteCommandRequest;
import io.angularpay.identity.util.PasswordUtil;
import io.angularpay.identity.validation.DefaultConstraintValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static io.angularpay.identity.domain.PasswordResetStatus.COMPLETED;
import static io.angularpay.identity.domain.PasswordResetStatus.CONFIRMED;
import static io.angularpay.identity.domain.PasswordResetType.FORGOT_PASSWORD;
import static io.angularpay.identity.exceptions.ErrorCode.ILLEGAL_REQUEST_ERROR;
import static io.angularpay.identity.helpers.CommandHelper.getRequestByReferenceOrThrow;
import static io.angularpay.identity.helpers.CommandHelper.validUserStatusOrThrow;

@Service
public class ForgotPasswordCompleteCommand extends AbstractCommand<ForgotPasswordCompleteCommandRequest, Void>
        implements SensitiveDataCommand<ForgotPasswordCompleteCommandRequest> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;
    private final PasswordUtil passwordUtil;

    public ForgotPasswordCompleteCommand(
            ObjectMapper mapper,
            MongoAdapter mongoAdapter,
            DefaultConstraintValidator validator,
            PasswordUtil passwordUtil) {
        super("ForgotPasswordCompleteCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
        this.passwordUtil = passwordUtil;
    }

    @Override
    protected String getResourceOwner(ForgotPasswordCompleteCommandRequest request) {
        return request.getAuthenticatedUser().getDeviceId();
    }

    @Override
    protected Void handle(ForgotPasswordCompleteCommandRequest request) {
        UserIdentity found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getUserReference());
        validUserStatusOrThrow(found);

        if (found.getPasswordReset().getStatus() != CONFIRMED) {
            throw CommandException.builder()
                    .status(HttpStatus.FORBIDDEN)
                    .errorCode(ILLEGAL_REQUEST_ERROR)
                    .message(ILLEGAL_REQUEST_ERROR.getDefaultMessage())
                    .build();
        }

        String hashedPassword = this.passwordUtil.toHashedPassword(request.getForgotPasswordCompleteApiModel().getNewPassword());

        found.setPassword(hashedPassword);
        found.setPasswordReset(PasswordReset.builder()
                .type(FORGOT_PASSWORD)
                .lastModified(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString())
                .status(COMPLETED)
                .build());

        this.mongoAdapter.updateUser(found);
        return null;
    }

    @Override
    protected List<ErrorObject> validate(ForgotPasswordCompleteCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Collections.emptyList();
    }

    @Override
    public ForgotPasswordCompleteCommandRequest mask(ForgotPasswordCompleteCommandRequest raw) {
        try {
            JsonNode node = mapper.convertValue(raw, JsonNode.class);
            JsonNode userLoginApiModel = node.get("forgotPasswordCompleteApiModel");
            ((ObjectNode) userLoginApiModel).put("new_password", "*****");
            return mapper.treeToValue(node, ForgotPasswordCompleteCommandRequest.class);
        } catch (JsonProcessingException exception) {
            return raw;
        }
    }
}
