package io.angularpay.identity.ports.outbound;

import io.angularpay.identity.models.UserProfileResponseModel;

import java.util.Map;
import java.util.Optional;

public interface UserConfigurationServicePort {
    Optional<UserProfileResponseModel> getUserProfile(String userReference, Map<String, String> headers);
}
