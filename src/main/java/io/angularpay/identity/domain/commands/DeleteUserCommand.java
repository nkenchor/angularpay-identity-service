package io.angularpay.identity.domain.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.identity.adapters.outbound.MongoAdapter;
import io.angularpay.identity.domain.Role;
import io.angularpay.identity.domain.UserIdentity;
import io.angularpay.identity.exceptions.ErrorObject;
import io.angularpay.identity.helpers.CommandHelper;
import io.angularpay.identity.models.AssociatedReferenceResponse;
import io.angularpay.identity.models.DeleteUserCommandRequest;
import io.angularpay.identity.validation.DefaultConstraintValidator;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import static io.angularpay.identity.helpers.CommandHelper.getRequestByReferenceOrThrow;

@Service
public class DeleteUserCommand extends AbstractCommand<DeleteUserCommandRequest, AssociatedReferenceResponse> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;
    private final CommandHelper commandHelper;

    public DeleteUserCommand(
            ObjectMapper mapper,
            MongoAdapter mongoAdapter,
            DefaultConstraintValidator validator,
            CommandHelper commandHelper) {
        super("DeleteUserCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
        this.commandHelper = commandHelper;
    }

    @Override
    protected String getResourceOwner(DeleteUserCommandRequest request) {
        return this.commandHelper.getRequestOwner(request.getUserReference());
    }

    @Override
    protected AssociatedReferenceResponse handle(DeleteUserCommandRequest request) {
        UserIdentity found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getUserReference());
        found.setDeleted(true);
        found.setEnabled(false);
        UserIdentity response = this.mongoAdapter.updateUser(found);
        return AssociatedReferenceResponse.builder().identityReference(response.getUserReference()).build();
    }

    @Override
    protected List<ErrorObject> validate(DeleteUserCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Arrays.asList(Role.ROLE_IDENTITY_ADMIN, Role.ROLE_SUPER_ADMIN);
    }

}
