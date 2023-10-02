package io.angularpay.identity.domain.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.identity.adapters.outbound.MongoAdapter;
import io.angularpay.identity.domain.Role;
import io.angularpay.identity.domain.UserIdentity;
import io.angularpay.identity.exceptions.ErrorObject;
import io.angularpay.identity.models.AssociatedReferenceResponse;
import io.angularpay.identity.models.OnboardUserCommandRequest;
import io.angularpay.identity.models.GenericReferenceResponse;
import io.angularpay.identity.models.ResourceReferenceResponse;
import io.angularpay.identity.util.PasswordUtil;
import io.angularpay.identity.validation.DefaultConstraintValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.angularpay.identity.domain.Role.ROLE_UNVERIFIED_USER;
import static io.angularpay.identity.helpers.CommandHelper.validateNotExistByReferenceOrThrow;
import static io.angularpay.identity.helpers.CommandHelper.validateNotExistOrThrow;
import static io.angularpay.identity.helpers.ObjectFactory.userIdentityWithDefaults;

@Slf4j
@Service
public class OnboardUserCommand extends AbstractCommand<OnboardUserCommandRequest, GenericReferenceResponse>
        implements ResourceReferenceCommand<AssociatedReferenceResponse, ResourceReferenceResponse> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;
    private final PasswordUtil passwordUtil;

    public OnboardUserCommand(
            ObjectMapper mapper,
            MongoAdapter mongoAdapter,
            DefaultConstraintValidator validator,
            PasswordUtil passwordUtil) {
        super("OnboardUserCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
        this.passwordUtil = passwordUtil;
    }

    @Override
    protected String getResourceOwner(OnboardUserCommandRequest request) {
        return "";
    }

    @Override
    protected AssociatedReferenceResponse handle(OnboardUserCommandRequest request) {
        validateNotExistOrThrow(this.mongoAdapter, request.getOnboardUserApiModel().getUsername());
        validateNotExistByReferenceOrThrow(this.mongoAdapter, request.getOnboardUserApiModel().getUserReference());
        UserIdentity userIdentityWithDefaults = userIdentityWithDefaults();
        String hashedPassword = this.passwordUtil.toHashedPassword(request.getOnboardUserApiModel().getPassword());
        UserIdentity withOtherDetails = userIdentityWithDefaults.toBuilder()
                .userReference(request.getOnboardUserApiModel().getUserReference())
                .username(request.getOnboardUserApiModel().getUsername().toLowerCase())
                .password(hashedPassword)
                .email(request.getOnboardUserApiModel().getEmail().toLowerCase())
                .firstname(request.getOnboardUserApiModel().getFirstname())
                .lastname(request.getOnboardUserApiModel().getLastname())
                .devices(Collections.singleton(request.getOnboardUserApiModel().getDevice()))
                .roles(Collections.singleton(ROLE_UNVERIFIED_USER))
                .build();
        UserIdentity response = this.mongoAdapter.createUser(withOtherDetails);
        return AssociatedReferenceResponse.builder().identityReference(response.getUserReference()).build();
    }

    @Override
    protected List<ErrorObject> validate(OnboardUserCommandRequest request) {
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
