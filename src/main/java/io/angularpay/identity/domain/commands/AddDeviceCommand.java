package io.angularpay.identity.domain.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.identity.adapters.outbound.MongoAdapter;
import io.angularpay.identity.domain.Role;
import io.angularpay.identity.domain.UserIdentity;
import io.angularpay.identity.exceptions.ErrorObject;
import io.angularpay.identity.helpers.CommandHelper;
import io.angularpay.identity.models.AddDeviceCommandRequest;
import io.angularpay.identity.models.AssociatedReferenceResponse;
import io.angularpay.identity.validation.DefaultConstraintValidator;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import static io.angularpay.identity.helpers.CommandHelper.getRequestByReferenceOrThrow;
import static io.angularpay.identity.helpers.CommandHelper.validUserStatusOrThrow;

@Service
public class AddDeviceCommand extends AbstractCommand<AddDeviceCommandRequest, AssociatedReferenceResponse> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;
    private final CommandHelper commandHelper;

    public AddDeviceCommand(
            ObjectMapper mapper,
            MongoAdapter mongoAdapter,
            DefaultConstraintValidator validator,
            CommandHelper commandHelper) {
        super("AddDeviceCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
        this.commandHelper = commandHelper;
    }

    @Override
    protected String getResourceOwner(AddDeviceCommandRequest request) {
        return this.commandHelper.getRequestOwner(request.getUserReference());
    }

    @Override
    protected AssociatedReferenceResponse handle(AddDeviceCommandRequest request) {
        UserIdentity found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getUserReference());
        validUserStatusOrThrow(found);
        this.commandHelper.addItemToCollection(found, request.getAddDeviceApiModel().getDevice(), found::getDevices, found::setDevices);
        return AssociatedReferenceResponse.builder().identityReference(found.getUserReference()).build();
    }

    @Override
    protected List<ErrorObject> validate(AddDeviceCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Arrays.asList(Role.ROLE_IDENTITY_ADMIN, Role.ROLE_SUPER_ADMIN);
    }
}
