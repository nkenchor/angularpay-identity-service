package io.angularpay.identity.domain.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.identity.adapters.outbound.MongoAdapter;
import io.angularpay.identity.domain.Role;
import io.angularpay.identity.domain.UserIdentity;
import io.angularpay.identity.exceptions.ErrorObject;
import io.angularpay.identity.helpers.CommandHelper;
import io.angularpay.identity.models.AssociatedReferenceResponse;
import io.angularpay.identity.models.EnableUserCommandRequest;
import io.angularpay.identity.validation.DefaultConstraintValidator;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static io.angularpay.identity.helpers.CommandHelper.getRequestByReferenceOrThrow;
import static io.angularpay.identity.helpers.CommandHelper.nonDeletedStatusOrThrow;

@Service
public class EnableUserCommand extends AbstractCommand<EnableUserCommandRequest, AssociatedReferenceResponse> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;
    private final CommandHelper commandHelper;

    public EnableUserCommand(
            ObjectMapper mapper,
            MongoAdapter mongoAdapter,
            DefaultConstraintValidator validator,
            CommandHelper commandHelper) {
        super("EnableUserCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
        this.commandHelper = commandHelper;
    }

    @Override
    protected String getResourceOwner(EnableUserCommandRequest request) {
        return "";
    }

    @Override
    protected AssociatedReferenceResponse handle(EnableUserCommandRequest request) {
        UserIdentity found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getUserReference());
        nonDeletedStatusOrThrow(found);
        return this.commandHelper.updateProperty(found, request::isEnable, found::setEnabled);
    }

    @Override
    protected List<ErrorObject> validate(EnableUserCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Collections.singletonList(Role.ROLE_SUPER_ADMIN);
    }

}
