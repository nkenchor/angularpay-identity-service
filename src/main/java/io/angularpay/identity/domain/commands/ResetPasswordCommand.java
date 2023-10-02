package io.angularpay.identity.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.angularpay.identity.adapters.outbound.MongoAdapter;
import io.angularpay.identity.domain.PasswordReset;
import io.angularpay.identity.domain.Role;
import io.angularpay.identity.domain.UserIdentity;
import io.angularpay.identity.exceptions.ErrorObject;
import io.angularpay.identity.models.ResetPasswordCommandRequest;
import io.angularpay.identity.util.PasswordUtil;
import io.angularpay.identity.validation.DefaultConstraintValidator;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static io.angularpay.identity.domain.PasswordResetStatus.COMPLETED;
import static io.angularpay.identity.domain.PasswordResetType.RESET_PASSWORD;
import static io.angularpay.identity.helpers.CommandHelper.getRequestByReferenceOrThrow;
import static io.angularpay.identity.helpers.CommandHelper.validUserStatusOrThrow;

@Service
public class ResetPasswordCommand extends AbstractCommand<ResetPasswordCommandRequest, Void>
        implements SensitiveDataCommand<ResetPasswordCommandRequest> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;
    private final PasswordUtil passwordUtil;

    public ResetPasswordCommand(
            ObjectMapper mapper,
            MongoAdapter mongoAdapter,
            DefaultConstraintValidator validator,
            PasswordUtil passwordUtil) {
        super("ResetPasswordCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
        this.passwordUtil = passwordUtil;
    }

    @Override
    protected String getResourceOwner(ResetPasswordCommandRequest request) {
        return "";
    }

    @Override
    protected Void handle(ResetPasswordCommandRequest request) {
        UserIdentity found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getUserReference());
        validUserStatusOrThrow(found);
        String hashedPassword = this.passwordUtil.toHashedPassword(request.getResetPasswordApiModel().getNewPassword());
        found.setPassword(hashedPassword);
        found.setPasswordReset(PasswordReset.builder()
                .type(RESET_PASSWORD)
                .lastModified(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString())
                .status(COMPLETED)
                .build());

        this.mongoAdapter.updateUser(found);
        return null;
    }

    @Override
    protected List<ErrorObject> validate(ResetPasswordCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Collections.singletonList(Role.ROLE_SUPER_ADMIN);
    }

    @Override
    public ResetPasswordCommandRequest mask(ResetPasswordCommandRequest raw) {
        try {
            JsonNode node = mapper.convertValue(raw, JsonNode.class);
            JsonNode userLoginApiModel = node.get("resetPasswordApiModel");
            ((ObjectNode) userLoginApiModel).put("new_password", "*****");
            return mapper.treeToValue(node, ResetPasswordCommandRequest.class);
        } catch (JsonProcessingException exception) {
            return raw;
        }
    }
}
