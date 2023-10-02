package io.angularpay.identity.domain.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.identity.domain.Role;
import io.angularpay.identity.exceptions.ErrorObject;
import io.angularpay.identity.models.VerifyTokenCommandRequest;
import io.angularpay.identity.models.VerifyTokenResponse;
import io.angularpay.identity.util.AccessTokenUtil;
import io.angularpay.identity.validation.DefaultConstraintValidator;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class VerifyTokenCommand extends AbstractCommand<VerifyTokenCommandRequest, VerifyTokenResponse> {

    private final DefaultConstraintValidator validator;
    private final AccessTokenUtil accessTokenUtil;

    public VerifyTokenCommand(
            ObjectMapper mapper,
            DefaultConstraintValidator validator,
            AccessTokenUtil accessTokenUtil) {
        super("VerifyTokenCommand", mapper);
        this.validator = validator;
        this.accessTokenUtil = accessTokenUtil;
    }

    @Override
    protected String getResourceOwner(VerifyTokenCommandRequest request) {
        return request.getAuthenticatedUser().getUserReference();
    }

    @Override
    protected VerifyTokenResponse handle(VerifyTokenCommandRequest request) {
        boolean isValid = this.accessTokenUtil.verifyToken(request.getVerifyTokenApiModel().getAccessToken());
        return new VerifyTokenResponse(isValid);
    }

    @Override
    protected List<ErrorObject> validate(VerifyTokenCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Collections.emptyList();
    }

}
