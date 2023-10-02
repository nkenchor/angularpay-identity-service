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
import io.angularpay.identity.helpers.CommandHelper;
import io.angularpay.identity.models.IsSamePasswordParameter;
import io.angularpay.identity.models.ChangePasswordCommandRequest;
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
public class ChangePasswordCommand extends AbstractCommand<ChangePasswordCommandRequest, Void>
        implements SensitiveDataCommand<ChangePasswordCommandRequest> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;
    private final CommandHelper commandHelper;
    private final PasswordUtil passwordUtil;

    public ChangePasswordCommand(
            ObjectMapper mapper,
            MongoAdapter mongoAdapter,
            DefaultConstraintValidator validator,
            CommandHelper commandHelper,
            PasswordUtil passwordUtil) {
        super("ChangePasswordCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
        this.commandHelper = commandHelper;
        this.passwordUtil = passwordUtil;
    }

    @Override
    protected String getResourceOwner(ChangePasswordCommandRequest request) {
        return this.commandHelper.getRequestOwner(request.getUserReference());
    }

    @Override
    protected Void handle(ChangePasswordCommandRequest request) {
        UserIdentity found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getUserReference());
        validUserStatusOrThrow(found);
        this.commandHelper.isSamePasswordOrThrow(IsSamePasswordParameter.builder()
                .hashedPassword(found.getPassword())
                .plaintextPassword(request.getChangePasswordApiModel().getOldPassword())
                .build());
        String hashedPassword = this.passwordUtil.toHashedPassword(request.getChangePasswordApiModel().getNewPassword());

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
    protected List<ErrorObject> validate(ChangePasswordCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Collections.emptyList();
    }

    @Override
    public ChangePasswordCommandRequest mask(ChangePasswordCommandRequest raw) {
        try {
            JsonNode node = mapper.convertValue(raw, JsonNode.class);
            JsonNode userLoginApiModel = node.get("changePasswordApiModel");
            ((ObjectNode) userLoginApiModel).put("old_password", "*****");
            ((ObjectNode) userLoginApiModel).put("new_password", "*****");
            return mapper.treeToValue(node, ChangePasswordCommandRequest.class);
        } catch (JsonProcessingException exception) {
            return raw;
        }
    }
}
