package io.angularpay.identity.adapters.inbound;

import io.angularpay.identity.domain.commands.*;
import io.angularpay.identity.models.*;
import io.angularpay.identity.ports.inbound.AuthenticationRestApiPort;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static io.angularpay.identity.helpers.Helper.fromHeaders;
import static io.angularpay.identity.helpers.Helper.parseAuthenticationMode;

@RestController
@RequestMapping("/identity/auth")
@RequiredArgsConstructor
public class AuthenticationRestApiPortAdapter implements AuthenticationRestApiPort {

    private final AuthenticateUserCommand authenticateUserCommand;
    private final AuthenticateOnboardingUserCommand authenticateOnboardingUserCommand;
    private final AuthenticatePlatformAdminUserCommand authenticatePlatformAdminUserCommand;
    private final AuthenticateForgotPasswordUserCommand authenticateForgotPasswordUserCommand;
    private final VerifyTokenCommand verifyTokenCommand;
    private final LogoutUserCommand logoutUserCommand;
    private final RevokeTokenCommand revokeTokenCommand;
    private final RefreshTokenCommand refreshTokenCommand;

    @PostMapping("/login")
    @ResponseBody
    @Override
    public AuthenticationResponse authenticate(
            @RequestBody GenericUserLoginApiModel userLoginApiModel,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        authenticatedUser.setUserReference(userLoginApiModel.getUsername()); // for resource owner check

        GenericAuthenticateUserCommandRequest authenticateUserCommandRequest = GenericAuthenticateUserCommandRequest.builder()
                .userLoginApiModel(userLoginApiModel)
                .authenticatedUser(authenticatedUser)
                .build();

        AuthenticationMode authenticationMode = parseAuthenticationMode(authenticatedUser.getAuthenticationMode());
        switch (authenticationMode) {
            case MOBILE_USER_ONBOARDING:
                return this.authenticateOnboardingUserCommand.execute(authenticateUserCommandRequest);
            case WEB_PLATFORM_ADMIN_LOGIN:
                return this.authenticatePlatformAdminUserCommand.execute(authenticateUserCommandRequest);
            case MOBILE_FORGOT_PASSWORD:
                return this.authenticateForgotPasswordUserCommand.execute(authenticateUserCommandRequest);
            case MOBILE_USER_LOGIN:
            default:
                return this.authenticateUserCommand.execute(authenticateUserCommandRequest);
        }
    }

    @PostMapping("/logout")
    @Override
    public void logout(@RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        LogoutUserCommandRequest logoutUserCommandRequest = LogoutUserCommandRequest.builder()
                .authenticatedUser(authenticatedUser)
                .build();
        this.logoutUserCommand.execute(logoutUserCommandRequest);
    }

    @PostMapping("/token/verify")
    @ResponseBody
    @Override
    public VerifyTokenResponse verifyToken(
            @RequestBody VerifyTokenApiModel verifyTokenApiModel,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        VerifyTokenCommandRequest verifyTokenCommandRequest = VerifyTokenCommandRequest.builder()
                .verifyTokenApiModel(verifyTokenApiModel)
                .authenticatedUser(authenticatedUser)
                .build();
        return this.verifyTokenCommand.execute(verifyTokenCommandRequest);
    }

    @DeleteMapping("/token/revoke")
    @Override
    public void revokeTokens(
            @RequestBody RevokeTokensApiModel revokeTokensApiModel,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        RevokeTokenCommandRequest revokeTokenCommandRequest = RevokeTokenCommandRequest.builder()
                .revokeTokensApiModel(revokeTokensApiModel)
                .authenticatedUser(authenticatedUser)
                .build();
        this.revokeTokenCommand.execute(revokeTokenCommandRequest);
    }

    @PostMapping("/token/refresh")
    @Override
    public AuthenticationResponse refreshToken(
            @RequestBody RefreshTokensApiModel refreshTokensApiModel,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        RefreshTokenCommandRequest refreshTokenCommandRequest = RefreshTokenCommandRequest.builder()
                .refreshTokensApiModel(refreshTokensApiModel)
                .authenticatedUser(authenticatedUser)
                .build();
        return this.refreshTokenCommand.execute(refreshTokenCommandRequest);
    }
}
