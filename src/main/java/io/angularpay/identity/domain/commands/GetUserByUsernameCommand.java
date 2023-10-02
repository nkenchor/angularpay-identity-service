package io.angularpay.identity.domain.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.identity.adapters.outbound.MongoAdapter;
import io.angularpay.identity.domain.Role;
import io.angularpay.identity.domain.UserIdentity;
import io.angularpay.identity.exceptions.ErrorObject;
import io.angularpay.identity.models.GetByUsernameCommandRequest;
import io.angularpay.identity.validation.DefaultConstraintValidator;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import static io.angularpay.identity.helpers.CommandHelper.getRequestByUsernameOrThrow;

@Service
public class GetUserByUsernameCommand extends AbstractCommand<GetByUsernameCommandRequest, UserIdentity> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;

    public GetUserByUsernameCommand(
            ObjectMapper mapper,
            MongoAdapter mongoAdapter,
            DefaultConstraintValidator validator) {
        super("GetUserByUsernameCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
    }

    @Override
    protected String getResourceOwner(GetByUsernameCommandRequest request) {
        return "";
    }

    @Override
    protected UserIdentity handle(GetByUsernameCommandRequest request) {
        return getRequestByUsernameOrThrow(this.mongoAdapter, request.getUsername());
    }

    @Override
    protected List<ErrorObject> validate(GetByUsernameCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Arrays.asList(Role.ROLE_IDENTITY_ADMIN, Role.ROLE_SUPER_ADMIN);
    }
}
