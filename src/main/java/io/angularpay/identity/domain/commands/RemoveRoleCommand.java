package io.angularpay.identity.domain.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.identity.adapters.outbound.MongoAdapter;
import io.angularpay.identity.domain.Role;
import io.angularpay.identity.domain.UserIdentity;
import io.angularpay.identity.exceptions.ErrorObject;
import io.angularpay.identity.models.AssociatedReferenceResponse;
import io.angularpay.identity.models.RemoveRoleCommandRequest;
import io.angularpay.identity.validation.DefaultConstraintValidator;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import static io.angularpay.identity.helpers.CommandHelper.getRequestByReferenceOrThrow;
import static io.angularpay.identity.helpers.CommandHelper.validUserStatusOrThrow;

@Service
public class RemoveRoleCommand extends AbstractCommand<RemoveRoleCommandRequest, AssociatedReferenceResponse> {
    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;

    public RemoveRoleCommand(
            ObjectMapper mapper,
            MongoAdapter mongoAdapter,
            DefaultConstraintValidator validator) {
        super("RemoveRoleCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
    }

    @Override
    protected String getResourceOwner(RemoveRoleCommandRequest request) {
        return "";
    }

    @Override
    protected AssociatedReferenceResponse handle(RemoveRoleCommandRequest request) {
        UserIdentity found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getUserReference());
        validUserStatusOrThrow(found);
        found.getRoles().removeIf(role -> request.getRole() == role);
        UserIdentity response = this.mongoAdapter.updateUser(found);
        return AssociatedReferenceResponse.builder().identityReference(response.getUserReference()).build();
    }

    @Override
    protected List<ErrorObject> validate(RemoveRoleCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Arrays.asList(Role.ROLE_IDENTITY_ADMIN, Role.ROLE_SUPER_ADMIN);
    }

}
