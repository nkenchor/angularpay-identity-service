package io.angularpay.identity.domain.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.identity.adapters.outbound.MongoAdapter;
import io.angularpay.identity.domain.Role;
import io.angularpay.identity.domain.UserIdentity;
import io.angularpay.identity.exceptions.ErrorObject;
import io.angularpay.identity.helpers.CommandHelper;
import io.angularpay.identity.models.AssociatedReferenceResponse;
import io.angularpay.identity.models.UpdateUserCommandRequest;
import io.angularpay.identity.validation.DefaultConstraintValidator;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static io.angularpay.identity.helpers.CommandHelper.getRequestByReferenceOrThrow;
import static io.angularpay.identity.helpers.CommandHelper.validUserStatusOrThrow;

@Service
public class UpdateUserCommand extends AbstractCommand<UpdateUserCommandRequest, AssociatedReferenceResponse> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;
    private final CommandHelper commandHelper;

    public UpdateUserCommand(
            ObjectMapper mapper,
            MongoAdapter mongoAdapter,
            DefaultConstraintValidator validator,
            CommandHelper commandHelper) {
        super("UpdateUserCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
        this.commandHelper = commandHelper;
    }

    @Override
    protected String getResourceOwner(UpdateUserCommandRequest request) {
        return this.commandHelper.getRequestOwner(request.getUserReference());
    }

    @Override
    protected AssociatedReferenceResponse handle(UpdateUserCommandRequest request) {
        UserIdentity found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getUserReference());
        validUserStatusOrThrow(found);
        found.setFirstname(request.getUpdateUserApiModel().getFirstname());
        found.setLastname(request.getUpdateUserApiModel().getLastname());
        UserIdentity response = this.mongoAdapter.updateUser(found);
        return AssociatedReferenceResponse.builder().identityReference(response.getUserReference()).build();
    }

    @Override
    protected List<ErrorObject> validate(UpdateUserCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Collections.emptyList();
    }

}
