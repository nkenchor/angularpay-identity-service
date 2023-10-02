package io.angularpay.identity.ports.inbound;

import io.angularpay.identity.models.*;

import java.util.Map;

public interface AuthenticationRestApiPort {
    AuthenticationResponse authenticate(GenericUserLoginApiModel userLoginApiModel, Map<String, String> headers);
    void logout(Map<String, String> headers);
    VerifyTokenResponse verifyToken(VerifyTokenApiModel verifyTokenApiModel, Map<String, String> headers);
    void revokeTokens(RevokeTokensApiModel revokeTokensApiModel, Map<String, String> headers);
    AuthenticationResponse refreshToken(RefreshTokensApiModel refreshTokensApiModel, Map<String, String> headers);
}
