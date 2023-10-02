package io.angularpay.identity.domain.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.identity.adapters.outbound.MongoAdapter;
import io.angularpay.identity.domain.Role;
import io.angularpay.identity.domain.UserIdentity;
import io.angularpay.identity.exceptions.ErrorObject;
import io.angularpay.identity.models.AssociatedReferenceResponse;
import io.angularpay.identity.models.CreateUserCommandRequest;
import io.angularpay.identity.models.GenericReferenceResponse;
import io.angularpay.identity.models.ResourceReferenceResponse;
import io.angularpay.identity.util.PasswordUtil;
import io.angularpay.identity.validation.DefaultConstraintValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static io.angularpay.identity.domain.Role.ROLE_PLATFORM_USER;
import static io.angularpay.identity.helpers.CommandHelper.validateNotExistOrThrow;
import static io.angularpay.identity.helpers.ObjectFactory.userIdentityWithDefaults;

@Slf4j
@Service
public class CreateUserCommand extends AbstractCommand<CreateUserCommandRequest, GenericReferenceResponse>
        implements ResourceReferenceCommand<AssociatedReferenceResponse, ResourceReferenceResponse> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;
    private final PasswordUtil passwordUtil;

    public CreateUserCommand(
            ObjectMapper mapper,
            MongoAdapter mongoAdapter,
            DefaultConstraintValidator validator,
            PasswordUtil passwordUtil) {
        super("CreateUserCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
        this.passwordUtil = passwordUtil;
    }

    @Override
    protected String getResourceOwner(CreateUserCommandRequest request) {
        return "";
    }

    @Override
    protected AssociatedReferenceResponse handle(CreateUserCommandRequest request) {
        validateNotExistOrThrow(this.mongoAdapter, request.getCreateUserApiModel().getUsername());
        UserIdentity userIdentityWithDefaults = userIdentityWithDefaults();
        String hashedPassword = this.passwordUtil.toHashedPassword(request.getCreateUserApiModel().getPassword());
        UserIdentity withOtherDetails = userIdentityWithDefaults.toBuilder()
                .userReference(UUID.randomUUID().toString())
                .username(request.getCreateUserApiModel().getUsername().toLowerCase())
                .password(hashedPassword)
                .email(request.getCreateUserApiModel().getEmail().toLowerCase())
                .firstname(request.getCreateUserApiModel().getFirstname())
                .lastname(request.getCreateUserApiModel().getLastname())
                .devices(Collections.singleton(request.getCreateUserApiModel().getDevice()))
                .roles(Collections.singleton(ROLE_PLATFORM_USER))
                .build();
        UserIdentity response = this.mongoAdapter.createUser(withOtherDetails);
        return AssociatedReferenceResponse.builder().identityReference(response.getUserReference()).build();
    }

    @Override
    protected List<ErrorObject> validate(CreateUserCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Arrays.asList(Role.ROLE_IDENTITY_ADMIN, Role.ROLE_SUPER_ADMIN);
    }

    @Override
    public ResourceReferenceResponse map(AssociatedReferenceResponse associatedReferenceResponse) {
        return new ResourceReferenceResponse(associatedReferenceResponse.getIdentityReference());
    }
}
