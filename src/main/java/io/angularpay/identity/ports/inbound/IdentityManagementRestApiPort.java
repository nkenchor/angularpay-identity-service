package io.angularpay.identity.ports.inbound;

import io.angularpay.identity.domain.Role;
import io.angularpay.identity.domain.UserIdentity;
import io.angularpay.identity.models.*;

import java.util.List;
import java.util.Map;

public interface IdentityManagementRestApiPort {
    GenericReferenceResponse createUser(CreateUserApiModel request, Map<String, String> headers);
    GenericReferenceResponse onboardUser(OnboardUserApiModel request, Map<String, String> headers);
    void updateUser(String requestReference, UpdateUserApiModel updateUserApiModel, Map<String, String> headers);
    void resetPassword(String userReference, ResetPasswordApiModel resetPasswordApiModel, Map<String, String> headers);
    void changePassword(String userReference, ChangePasswordApiModel changePasswordApiModel, Map<String, String> headers);
    GenericReferenceResponse forgotPasswordStart(ForgotPasswordStartApiModel forgotPasswordStartApiModel, Map<String, String> headers);
    void forgotPasswordConfirm(String userReference, ForgotPasswordCofrimApiModel forgotPasswordCofrimApiModel, Map<String, String> headers);
    void forgotPasswordComplete(String userReference, ForgotPasswordCompleteApiModel forgotPasswordCompleteApiModel, Map<String, String> headers);
    void addDevice(String userReference, AddDeviceApiModel addDeviceApiModel, Map<String, String> headers);
    void removeDevice(String userReference, String deviceId, Map<String, String> headers);
    void updateRoles(String userReference, List<Role> roles, Map<String, String> headers);
    void removeRole(String userReference, Role role, Map<String, String> headers);
    void enableUser(String userReference, boolean enable, Map<String, String> headers);
    void deleteUser(String userReference, Map<String, String> headers);
    UserIdentity getUserByReference(String userReference, Map<String, String> headers);
    UserIdentity getUserByUsername(String username, Map<String, String> headers);
    List<UserIdentity> getUserList(int page, Map<String, String> headers);
    List<GetDefinedRolesApiResponse> getDefinedRoles(Map<String, String> headers);
}
