package io.angularpay.identity.domain.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.identity.domain.Role;
import io.angularpay.identity.exceptions.ErrorObject;
import io.angularpay.identity.models.GetDefinedRolesApiResponse;
import io.angularpay.identity.models.GetDefinedRolesCommandRequest;
import io.angularpay.identity.validation.DefaultConstraintValidator;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.angularpay.identity.helpers.Helper.toTitleCase;

@Service
public class GetAssignableRolesCommand extends AbstractCommand<GetDefinedRolesCommandRequest, List<GetDefinedRolesApiResponse>> {

    private final DefaultConstraintValidator validator;

    public GetAssignableRolesCommand(ObjectMapper mapper, DefaultConstraintValidator validator) {
        super("GetAssignableRolesCommand", mapper);
        this.validator = validator;
    }

    @Override
    protected String getResourceOwner(GetDefinedRolesCommandRequest request) {
        return "";
    }

    @Override
    protected List<GetDefinedRolesApiResponse> handle(GetDefinedRolesCommandRequest request) {
        return Role.assignableUserRoles().stream()
                .map(x -> {
                    String displayName = toTitleCase(
                            x.name().replace("ROLE_", "")
                                    .replace("_", " ")
                                    .toLowerCase()
                    );
                    return GetDefinedRolesApiResponse.builder()
                            .role(x)
                            .name(displayName)
                            .build();
                }).collect(Collectors.toList());
    }

    @Override
    protected List<ErrorObject> validate(GetDefinedRolesCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Arrays.asList(Role.ROLE_IDENTITY_ADMIN, Role.ROLE_SUPER_ADMIN);
    }

}
