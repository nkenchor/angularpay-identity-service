package io.angularpay.identity.adapters.outbound.mocks;

import io.angularpay.identity.models.UserProfileResponseModel;
import io.angularpay.identity.ports.outbound.UserConfigurationServicePort;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class UserConfigurationServiceAdapterMock implements UserConfigurationServicePort {

    @Override
    public Optional<UserProfileResponseModel> getUserProfile(String userReference, Map<String, String> headers) {
        UserProfileResponseModel userProfileResponseModel = new UserProfileResponseModel();
        userProfileResponseModel.setFirstname("dornu");
        userProfileResponseModel.setLastname("ngbor");
        userProfileResponseModel.setEmail("dornu@dornu.com");
        userProfileResponseModel.setPhone("971555555555");
        return Optional.of(userProfileResponseModel);
    }
}
