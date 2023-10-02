package io.angularpay.identity.ports.outbound;

import io.angularpay.identity.models.CreateOtpRequestApiModel;
import io.angularpay.identity.models.CreateOtpResponseApiModel;
import io.angularpay.identity.models.ValidateOtpRequestApiModel;
import io.angularpay.identity.models.ValidateOtpResponseApiModel;

import java.util.Map;
import java.util.Optional;

public interface OtpServicePort {
    Optional<CreateOtpResponseApiModel> createOtp(
            CreateOtpRequestApiModel createOtpRequestApiModel,
            Map<String, String> headers);
    Optional<ValidateOtpResponseApiModel> validateOtp(
            ValidateOtpRequestApiModel validateOtpRequestApiModel,
            Map<String, String> headers);
}
