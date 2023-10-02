package io.angularpay.identity.domain.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.identity.adapters.outbound.MongoAdapter;
import io.angularpay.identity.domain.Role;
import io.angularpay.identity.domain.UserIdentity;
import io.angularpay.identity.exceptions.ErrorObject;
import io.angularpay.identity.models.GetSignupRequestListCommandRequest;
import io.angularpay.identity.validation.DefaultConstraintValidator;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class GetUserListCommand extends AbstractCommand<GetSignupRequestListCommandRequest, List<UserIdentity>> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;

    public GetUserListCommand(ObjectMapper mapper, MongoAdapter mongoAdapter, DefaultConstraintValidator validator) {
        super("GetUserListCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
    }

    @Override
    protected String getResourceOwner(GetSignupRequestListCommandRequest request) {
        return "";
    }

    @Override
    protected List<UserIdentity> handle(GetSignupRequestListCommandRequest request) {
        Pageable pageable = PageRequest.of(request.getPaging().getIndex(), request.getPaging().getSize());
        return this.mongoAdapter.listUsers(pageable).getContent();
    }

    @Override
    protected List<ErrorObject> validate(GetSignupRequestListCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Arrays.asList(Role.ROLE_IDENTITY_ADMIN, Role.ROLE_SUPER_ADMIN);
    }
}
