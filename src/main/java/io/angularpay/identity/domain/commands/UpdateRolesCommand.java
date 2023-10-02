package io.angularpay.identity.domain.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.identity.adapters.outbound.MongoAdapter;
import io.angularpay.identity.domain.Role;
import io.angularpay.identity.domain.UserIdentity;
import io.angularpay.identity.exceptions.CommandException;
import io.angularpay.identity.exceptions.ErrorObject;
import io.angularpay.identity.helpers.CommandHelper;
import io.angularpay.identity.models.UpdateRolesCommandRequest;
import io.angularpay.identity.validation.DefaultConstraintValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.angularpay.identity.exceptions.ErrorCode.ILLEGAL_SELF_ROLE_ASSIGNMENT_ERROR;
import static io.angularpay.identity.exceptions.ErrorCode.RESTRICTED_ROLE_ERROR;
import static io.angularpay.identity.helpers.CommandHelper.getRequestByReferenceOrThrow;
import static io.angularpay.identity.helpers.CommandHelper.validUserStatusOrThrow;

@Service
public class UpdateRolesCommand extends AbstractCommand<UpdateRolesCommandRequest, Void> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;
    private final CommandHelper commandHelper;

    public UpdateRolesCommand(
            ObjectMapper mapper,
            MongoAdapter mongoAdapter,
            DefaultConstraintValidator validator,
            CommandHelper commandHelper) {
        super("UpdateRolesCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
        this.commandHelper = commandHelper;
    }

    @Override
    protected String getResourceOwner(UpdateRolesCommandRequest request) {
        return "";
    }

    @Override
    protected Void handle(UpdateRolesCommandRequest request) {
        if(request.getUserReference().equalsIgnoreCase(request.getAuthenticatedUser().getUserReference())) {
            throw CommandException.builder()
                    .status(HttpStatus.FORBIDDEN)
                    .errorCode(ILLEGAL_SELF_ROLE_ASSIGNMENT_ERROR)
                    .message(ILLEGAL_SELF_ROLE_ASSIGNMENT_ERROR.getDefaultMessage())
                    .build();
        }

        UserIdentity found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getUserReference());
        validUserStatusOrThrow(found);

        if (request.getRoles().stream().anyMatch(x -> Role.nonAssignableUserRoles().contains(x))) {
            throw CommandException.builder()
                    .status(HttpStatus.FORBIDDEN)
                    .errorCode(RESTRICTED_ROLE_ERROR)
                    .message(RESTRICTED_ROLE_ERROR.getDefaultMessage())
                    .build();
        }
        Set<Role> newRoles;
        if (CollectionUtils.isEmpty(found.getRoles())) {
            newRoles = new HashSet<>(request.getRoles());
        } else {
            Set<Role> nonAssignableUserRoles = found.getRoles().stream()
                    .filter(x -> Role.nonAssignableUserRoles().contains(x))
                    .collect(Collectors.toSet());
            nonAssignableUserRoles.addAll(request.getRoles());
            newRoles = nonAssignableUserRoles;
        }
        this.commandHelper.updateCollection(found, newRoles, found::getRoles, found::setRoles);
        return null;
    }

    @Override
    protected List<ErrorObject> validate(UpdateRolesCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Arrays.asList(Role.ROLE_IDENTITY_ADMIN, Role.ROLE_SUPER_ADMIN);
    }

}
